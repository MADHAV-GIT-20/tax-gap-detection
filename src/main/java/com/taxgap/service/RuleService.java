package com.taxgap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.entity.ExceptionRecord;
import com.taxgap.entity.Rule;
import com.taxgap.entity.Transaction;
import com.taxgap.enums.TransactionType;
import com.taxgap.repository.RuleRepository;
import com.taxgap.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Runs the database-driven compliance rules against a transaction, and also
 * lets rules be listed and enabled/disabled.
 */
@Service
public class RuleService {

    private final RuleRepository ruleRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RuleService(RuleRepository ruleRepository,
                       TransactionRepository transactionRepository,
                       AuditService auditService) {
        this.ruleRepository = ruleRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    // ---- rule management ----

    public List<Rule> findAll() {
        return ruleRepository.findAll();
    }

    public Rule setEnabled(Long id, boolean enabled) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rule not found: " + id));
        rule.setEnabled(enabled);
        return ruleRepository.save(rule);
    }

    // ---- rule execution ----

    /**
     * Runs every enabled rule against the transaction and returns the exception
     * records that should be saved (one per violated rule).
     */
    public List<ExceptionRecord> runRules(Transaction txn) {
        List<ExceptionRecord> exceptions = new ArrayList<>();

        for (Rule rule : ruleRepository.findByEnabledTrue()) {
            String violationMessage = evaluate(rule, txn);
            boolean violated = violationMessage != null;

            auditService.logRuleExecution(txn.getTransactionId(), rule.getRuleName(), violated);

            if (violated) {
                ExceptionRecord record = new ExceptionRecord();
                record.setTransactionId(txn.getTransactionId());
                record.setCustomerId(txn.getCustomerId());
                record.setRuleName(rule.getRuleName());
                record.setSeverity(rule.getSeverity());
                record.setMessage(violationMessage);
                exceptions.add(record);
            }
        }
        return exceptions;
    }

    /** Returns a violation message if the rule is broken, otherwise null. */
    private String evaluate(Rule rule, Transaction txn) {
        switch (rule.getRuleType()) {
            case HIGH_VALUE:
                return checkHighValue(rule, txn);
            case REFUND_VALIDATION:
                return checkRefund(txn);
            case GST_SLAB_VIOLATION:
                return checkGstSlab(rule, txn);
            default:
                return null;
        }
    }

    // (A) High-Value rule: amount above the configured threshold.
    private String checkHighValue(Rule rule, Transaction txn) {
        BigDecimal threshold = readNumber(rule.getConfigJson(), "threshold");
        if (txn.getAmount() == null || threshold == null) {
            return null;
        }
        if (txn.getAmount().compareTo(threshold) > 0) {
            return "High-value transaction: amount " + txn.getAmount()
                    + " exceeds threshold " + threshold;
        }
        return null;
    }

    // (B) Refund rule: a refund must not exceed the customer's total sales.
    private String checkRefund(Transaction txn) {
        if (txn.getTransactionType() != TransactionType.REFUND || txn.getAmount() == null) {
            return null;
        }
        BigDecimal totalSales = totalSalesForCustomer(txn.getCustomerId());
        if (txn.getAmount().compareTo(totalSales) > 0) {
            return "Refund amount " + txn.getAmount()
                    + " exceeds customer's total sales " + totalSales;
        }
        return null;
    }

    // (C) GST slab rule: amount over the slab threshold but tax rate below the required rate.
    private String checkGstSlab(Rule rule, Transaction txn) {
        BigDecimal slabThreshold = readNumber(rule.getConfigJson(), "slabThreshold");
        BigDecimal requiredRate = readNumber(rule.getConfigJson(), "requiredRate");
        if (txn.getAmount() == null || txn.getTaxRate() == null
                || slabThreshold == null || requiredRate == null) {
            return null;
        }
        if (txn.getAmount().compareTo(slabThreshold) > 0
                && txn.getTaxRate().compareTo(requiredRate) < 0) {
            return "GST slab violation: amount " + txn.getAmount()
                    + " exceeds slab " + slabThreshold + " but tax rate " + txn.getTaxRate()
                    + " is below required " + requiredRate;
        }
        return null;
    }

    private BigDecimal totalSalesForCustomer(String customerId) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : transactionRepository.findByCustomerId(customerId)) {
            if (t.getTransactionType() == TransactionType.SALE && t.getAmount() != null) {
                total = total.add(t.getAmount());
            }
        }
        return total;
    }

    /** Reads a numeric field from a rule's JSON config, or null if missing/invalid. */
    private BigDecimal readNumber(String configJson, String field) {
        if (configJson == null || configJson.isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(configJson).get(field);
            return (node == null || node.isNull()) ? null : node.decimalValue();
        } catch (Exception e) {
            return null;
        }
    }
}
