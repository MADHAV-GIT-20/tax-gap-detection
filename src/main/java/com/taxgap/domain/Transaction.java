package com.taxgap.domain;

import com.taxgap.domain.enums.ComplianceStatus;
import com.taxgap.domain.enums.TransactionType;
import com.taxgap.domain.enums.ValidationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_txn_customer", columnList = "customerId"),
        @Index(name = "idx_txn_business_id", columnList = "transactionId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Business identifier supplied in the upload payload. */
    @Column(nullable = false)
    private String transactionId;

    private LocalDate date;

    @Column(nullable = false)
    private String customerId;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(precision = 9, scale = 4)
    private BigDecimal taxRate;

    @Column(precision = 19, scale = 4)
    private BigDecimal reportedTax;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    // ---- validation outcome ----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationStatus validationStatus;

    @Column(length = 1000)
    private String failureReasons;

    // ---- raw payload for audit ----
    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawPayload;

    // ---- tax gap results (populated only for valid transactions) ----
    @Column(precision = 19, scale = 4)
    private BigDecimal expectedTax;

    @Column(precision = 19, scale = 4)
    private BigDecimal taxGap;

    @Enumerated(EnumType.STRING)
    private ComplianceStatus complianceStatus;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
