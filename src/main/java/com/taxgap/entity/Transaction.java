package com.taxgap.entity;

import com.taxgap.enums.ComplianceStatus;
import com.taxgap.enums.TransactionType;
import com.taxgap.enums.ValidationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    private LocalDate date;

    private String customerId;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(precision = 9, scale = 4)
    private BigDecimal taxRate;

    @Column(precision = 19, scale = 4)
    private BigDecimal reportedTax;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    // validation outcome
    @Enumerated(EnumType.STRING)
    private ValidationStatus validationStatus;

    @Column(length = 1000)
    private String failureReasons;

    // tax gap results
    @Column(precision = 19, scale = 4)
    private BigDecimal expectedTax;

    @Column(precision = 19, scale = 4)
    private BigDecimal taxGap;

    @Enumerated(EnumType.STRING)
    private ComplianceStatus complianceStatus;

    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getReportedTax() {
        return reportedTax;
    }

    public void setReportedTax(BigDecimal reportedTax) {
        this.reportedTax = reportedTax;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getFailureReasons() {
        return failureReasons;
    }

    public void setFailureReasons(String failureReasons) {
        this.failureReasons = failureReasons;
    }

    public BigDecimal getExpectedTax() {
        return expectedTax;
    }

    public void setExpectedTax(BigDecimal expectedTax) {
        this.expectedTax = expectedTax;
    }

    public BigDecimal getTaxGap() {
        return taxGap;
    }

    public void setTaxGap(BigDecimal taxGap) {
        this.taxGap = taxGap;
    }

    public ComplianceStatus getComplianceStatus() {
        return complianceStatus;
    }

    public void setComplianceStatus(ComplianceStatus complianceStatus) {
        this.complianceStatus = complianceStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
