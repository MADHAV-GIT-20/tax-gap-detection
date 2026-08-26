# Design & Architecture

## 1. Overview

The Tax Gap Detection & Compliance Validation Service is a Spring Boot backend
that lets tax auditors upload batches of financial transactions, automatically
recomputes the tax that *should* have been paid, detects the gap against what
was reported, runs configurable compliance rules, records exceptions, keeps a
full audit trail, and exposes summary reports.

## 2. Layered architecture

The code follows a strict controller → service → repository → domain layering.

```
Controller  (REST, DTO mapping, validation, HTTP concerns)
    │
Service     (business logic, orchestration, transactions)
    │
Repository  (Spring Data JPA, aggregate queries)
    │
Domain      (JPA entities + enums)  ──►  MySQL
```

Spring Security's filter chain wraps every request (HTTP Basic against the
`app_users` table). DTOs isolate the API contract from the persistence model so
entities never leak over the wire.

## 3. Ingestion pipeline

A batch upload is processed one transaction at a time, each in **its own
database transaction** (`TransactionItemProcessor`) so a single bad record can
never roll back the rest of the batch:

```
ingest ─► validate ─► tax gap ─► rule engine ─► persist
   │          │           │           │
 audit      store       audit    audit per rule
        (FAILURE short-circuits here)
```

* **Validation** (`ValidationService`) — required fields, `amount > 0`, ISO date
  format, valid enum. Failures are *recorded* (status `FAILURE` + reasons),
  never thrown, so the batch stays resilient. `reportedTax` is intentionally
  optional (its absence drives the `NON_COMPLIANT` status downstream).
* **Tax gap** (`TaxGapService`) — `expectedTax = amount × taxRate`,
  `taxGap = expectedTax − reportedTax`, classified with a ±1 tolerance into
  COMPLIANT / UNDERPAID / OVERPAID, or NON_COMPLIANT when `reportedTax` is
  missing. All money math uses `BigDecimal` with scale 4, `HALF_UP`.
* **Rule engine** — see below.

## 4. Configurable rule engine (the core design decision)

Rules are **data, not code**. Each row in the `rules` table has a `rule_type`, a
`severity`, an `enabled` flag, and a JSON `config_json` blob. The engine
(`RuleEngineService`) loads all enabled rules and dispatches each to a
**Strategy** implementation keyed by `RuleType`:

```
RuleEvaluator (interface)
 ├─ HighValueEvaluator        config: {"threshold": 100000}
 ├─ RefundValidationEvaluator config: {}  (refund ≤ customer's total sales)
 └─ GstSlabViolationEvaluator config: {"slabThreshold": 50000, "requiredRate": 0.18}
```

Consequences of this design:

* A rule can be enabled/disabled at runtime (`PATCH /api/rules/{id}`) with no
  redeploy.
* Thresholds live in the DB, so tuning a rule is a data change.
* Adding a brand-new rule type is one new `RuleEvaluator` bean; the engine
  discovers it automatically (Spring injects `List<RuleEvaluator>`).

Each rule execution emits an audit entry and, on violation, an
`ExceptionRecord` carrying the rule's severity.

## 5. Audit trail

`AuditService` writes an `audit_logs` row for every INGESTION, TAX_COMPUTATION,
and RULE_EXECUTION event, with a JSON `detail_json` snapshot (amounts, computed
values, rule outcome). Logs are immutable historical facts, referenced by
business id rather than FK.

## 6. Reporting

Reports aggregate in **SQL**, not in memory (a hard requirement):

* **Customer tax summary** — a `GROUP BY customerId` query returns totals and
  the non-compliant count; only the ratio-based
  `complianceScore = 100 − (nonCompliant / total × 100)` is finalised in Java.
* **Exception summary** — total, counts by severity, counts by customer.

## 7. Security

Stateless HTTP Basic. Users are seeded into `app_users` with BCrypt-hashed
passwords. Two roles: `AUDITOR` (upload + read) and `ADMIN` (everything,
including toggling rules — enforced by a method matcher in `SecurityConfig`).

## 8. Technology choices

| Concern            | Choice                          | Why |
|--------------------|---------------------------------|-----|
| Framework          | Spring Boot 3.3                  | Required; mature REST/JPA/Security stack |
| Language           | Java 17 (built on JDK 25)       | Required target level |
| Persistence        | Spring Data JPA / Hibernate     | Declarative repositories + aggregate JPQL |
| Database           | MySQL 8                         | Real RDBMS for SQL-side reporting |
| Money              | `BigDecimal`                    | Exact decimal arithmetic for tax |
| Rules              | DB rows + Strategy pattern      | Runtime-configurable, extensible |
| Tests              | JUnit 5 + Mockito + JaCoCo      | Service/rule-engine unit coverage |

## 9. Testing

Unit tests concentrate on the service layer and rule engine (the assignment's
coverage target). They cover every compliance branch, all validation rules,
each rule evaluator (violation + pass paths), the rule engine dispatch, the
compliance-score formula, and exception aggregation. Measured JaCoCo coverage
for `service` + `service.rules` is **~62%**, above the 40–50% requirement.
