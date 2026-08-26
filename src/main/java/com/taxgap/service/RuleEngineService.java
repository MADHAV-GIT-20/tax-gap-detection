package com.taxgap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.ExceptionRecord;
import com.taxgap.domain.Rule;
import com.taxgap.domain.Transaction;
import com.taxgap.domain.enums.RuleType;
import com.taxgap.domain.enums.TransactionType;
import com.taxgap.repository.RuleRepository;
import com.taxgap.repository.TransactionRepository;
import com.taxgap.service.rules.RuleEvaluationContext;
import com.taxgap.service.rules.RuleEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads all active DB rules and executes each one against a transaction,
 * producing (unsaved) exception records and an audit entry per rule run.
 */
@Service
@Slf4j
public class RuleEngineService {

    private final RuleRepository ruleRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final Map<RuleType, RuleEvaluator> evaluators = new EnumMap<>(RuleType.class);

    public RuleEngineService(RuleRepository ruleRepository,
                             TransactionRepository transactionRepository,
                             AuditService auditService,
                             ObjectMapper objectMapper,
                             List<RuleEvaluator> evaluatorBeans) {
        this.ruleRepository = ruleRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        for (RuleEvaluator e : evaluatorBeans) {
            evaluators.put(e.supportedType(), e);
        }
    }

    /**
     * Run every enabled rule against the transaction.
     *
     * @return list of exception records to persist (not yet saved).
     */
    public List<ExceptionRecord> runRules(Transaction txn) {
        List<Rule> activeRules = ruleRepository.findByEnabledTrue();
        RuleEvaluationContext context = buildContext(txn);
        List<ExceptionRecord> exceptions = new ArrayList<>();

        for (Rule rule : activeRules) {
            RuleEvaluator evaluator = evaluators.get(rule.getRuleType());
            if (evaluator == null) {
                log.warn("No evaluator registered for rule type {}", rule.getRuleType());
                continue;
            }
            Optional<String> violation = evaluator.evaluate(txn, rule, context);
            auditService.logRuleExecution(txn.getTransactionId(), rule.getRuleName(), violation.isPresent());

            violation.ifPresent(message -> exceptions.add(ExceptionRecord.builder()
                    .transactionId(txn.getTransactionId())
                    .customerId(txn.getCustomerId())
                    .ruleName(rule.getRuleName())
                    .severity(rule.getSeverity())
                    .message(message)
                    .build()));
        }
        return exceptions;
    }

    private RuleEvaluationContext buildContext(Transaction txn) {
        BigDecimal totalSales = transactionRepository.findByCustomerId(txn.getCustomerId()).stream()
                .filter(t -> t.getTransactionType() == TransactionType.SALE)
                .map(Transaction::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RuleEvaluationContext(totalSales);
    }
}
