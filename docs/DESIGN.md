# Design & Architecture

## 1. Overview

The Tax Gap Detection & Compliance Validation Service is a plain Spring Boot
backend. Auditors upload batches of financial transactions; the service
validates them, recomputes the expected tax to find the "tax gap", runs
database-driven compliance rules, records exceptions, keeps an audit trail, and
exposes summary reports.

It is a standard, single-module Spring Boot project: no Lombok, no extra
patterns or abstractions — just entities, repositories, `@Service` classes and
`@RestController`s, so it is easy to read top to bottom.

## 2. Layers

```
Controller  (@RestController)   REST endpoints, request/response
     |
Service     (@Service)          business logic
     |
Repository  (Spring Data JPA)   database access
     |
Entity      (@Entity)           tables
                                   |
                                MySQL
```

Spring Security's filter chain wraps every request (HTTP Basic against the
`app_users` table).

## 3. Packages

```
com.taxgap
├── entity       JPA entities (Transaction, Rule, ExceptionRecord, AuditLog, AppUser)
├── enums         status/type enums
├── repository    Spring Data JPA repositories
├── dto           request/response objects
├── service       business logic
├── config        security, user details, startup data seeder
└── controller    REST endpoints + error handler
```

## 4. Upload pipeline

For each transaction in a batch, `TransactionService.process(...)` does:

```
audit ingestion
   -> validate
   -> (if invalid) save as FAILURE and move on
   -> compute tax gap  (audit tax computation)
   -> save the transaction
   -> run rules -> save any exceptions
```

Each transaction is saved on its own, so one bad row never stops the rest of the
batch.

**Validation** checks required fields, `amount > 0`, a valid `yyyy-MM-dd` date,
and a valid transaction type. `reportedTax` is optional — when it is missing the
compliance status becomes `NON_COMPLIANT`.

**Tax gap** (`BigDecimal`, scale 4):
```
expectedTax = amount * taxRate
taxGap      = expectedTax - reportedTax

|taxGap| <= 1  -> COMPLIANT
 taxGap  >  1  -> UNDERPAID
 taxGap  < -1  -> OVERPAID
 reportedTax missing -> NON_COMPLIANT
```

## 5. Database-driven rules

Rules live in the `rules` table. Each row has a `ruleType`, a `severity`, an
`enabled` flag and a JSON `configJson` (for example `{"threshold": 100000}`).

`RuleService.runRules(...)` loads all enabled rules and checks each one with a
simple `switch` on `ruleType`:

- **HIGH_VALUE** – amount above the configured threshold.
- **REFUND_VALIDATION** – a refund must not exceed the customer's total sales.
- **GST_SLAB_VIOLATION** – amount over a slab threshold but tax rate below the
  required rate.

Because rules are rows in a table, they can be enabled/disabled at runtime
(`PATCH /api/rules/{id}`) and their thresholds changed without touching code.

## 6. Audit trail

`AuditService` writes an `audit_logs` row for every INGESTION, TAX_COMPUTATION
and RULE_EXECUTION event, storing a small JSON snapshot in `detailJson`.

## 7. Reporting

Reports are aggregated by the database, not in Java memory:

- **Customer summary** – a single JPQL `GROUP BY customerId` query fills a
  `CustomerSummaryDto`; the service then finishes the
  `complianceScore = 100 - (nonCompliant / total * 100)`.
- **Exception summary** – total count, counts by severity, and counts by customer.

## 8. Security

Stateless HTTP Basic. Users are seeded into `app_users` with BCrypt-hashed
passwords. `AUDITOR` can upload and read; `ADMIN` can also enable/disable rules.

## 9. Testing

Plain JUnit 5 (with AssertJ) and small hand-written in-memory fake repositories
— no Mockito. Tests cover validation, all tax-gap branches, each rule, the
compliance-score formula and exception aggregation. Measured JaCoCo coverage for
the `service` package is about **83%** (requirement: 40–50%).
