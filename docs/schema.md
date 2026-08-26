# Database Schema

The schema is created automatically by Hibernate (`ddl-auto=update`) on first
start. The diagram below reflects the JPA entity model.

```mermaid
erDiagram
    APP_USERS {
        BIGINT id PK
        VARCHAR username UK
        VARCHAR password "BCrypt hash"
        VARCHAR role "AUDITOR | ADMIN"
        BOOLEAN enabled
    }

    RULES {
        BIGINT id PK
        VARCHAR rule_name UK
        VARCHAR rule_type "HIGH_VALUE | REFUND_VALIDATION | GST_SLAB_VIOLATION"
        VARCHAR severity "HIGH | MEDIUM | LOW"
        BOOLEAN enabled
        TEXT config_json "JSON parameters"
        VARCHAR description
    }

    TRANSACTIONS {
        BIGINT id PK
        VARCHAR transaction_id "business id"
        DATE date
        VARCHAR customer_id
        DECIMAL amount
        DECIMAL tax_rate
        DECIMAL reported_tax
        VARCHAR transaction_type "SALE | REFUND | EXPENSE"
        VARCHAR validation_status "SUCCESS | FAILURE"
        VARCHAR failure_reasons
        TEXT raw_payload
        DECIMAL expected_tax
        DECIMAL tax_gap
        VARCHAR compliance_status "COMPLIANT | UNDERPAID | OVERPAID | NON_COMPLIANT"
        TIMESTAMP created_at
    }

    EXCEPTION_RECORDS {
        BIGINT id PK
        VARCHAR transaction_id
        VARCHAR customer_id
        VARCHAR rule_name
        VARCHAR severity "HIGH | MEDIUM | LOW"
        VARCHAR message
        TIMESTAMP timestamp
    }

    AUDIT_LOGS {
        BIGINT id PK
        VARCHAR event_type "INGESTION | RULE_EXECUTION | TAX_COMPUTATION"
        VARCHAR transaction_id
        TIMESTAMP timestamp
        TEXT detail_json "old/new values or rule info"
    }

    TRANSACTIONS ||..o{ EXCEPTION_RECORDS : "transaction_id (logical)"
    TRANSACTIONS ||..o{ AUDIT_LOGS : "transaction_id (logical)"
    RULES ||..o{ EXCEPTION_RECORDS : "rule_name (logical)"
```

## Notes on relationships

Exception records and audit logs reference transactions and rules by their
**business identifiers** (`transaction_id`, `rule_name`) rather than hard FK
constraints. This is deliberate: audit/exception rows must remain immutable
historical facts even if a transaction or rule is later changed, and the
ingestion pipeline writes them in high volume. Indexes back the common lookups
(`customer_id`, `severity`, `rule_name`, `transaction_id`, `event_type`).
