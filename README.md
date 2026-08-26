# Tax Gap Detection & Compliance Validation Service

A Spring Boot backend for tax auditors to verify the accuracy of customer
financial transactions. It ingests transaction batches, validates them,
recomputes expected tax to detect **tax gaps**, runs a **database-driven,
configurable rule engine**, records **exceptions**, keeps a full **audit trail**,
and exposes **summary reports** — all secured with Spring Security over a
prefilled user table.

- **Design & architecture:** [docs/DESIGN.md](docs/DESIGN.md)
- **Database schema diagram:** [docs/schema.md](docs/schema.md)
- **Sample upload payload:** [samples/transaction-upload.json](samples/transaction-upload.json)

---

## Tech stack

| | |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.x (Web, Data JPA, Security, Validation) |
| Database | MySQL 8 (real RDBMS — reports aggregate in SQL) |
| Build | Maven |
| Auth | HTTP Basic against `app_users` table (roles: `AUDITOR`, `ADMIN`) |
| Tests | JUnit 5 + Mockito, coverage via JaCoCo |

---

## 1. Prerequisites

- JDK 17+ (developed and verified on JDK 25)
- Maven 3.9+
- MySQL Server 8.x running on `localhost:3306`

## 2. Database setup

Run the one-time setup script (in MySQL Workbench, or the CLI) as a privileged
user. It creates the `taxgap` schema and a dedicated application user so you
never expose your root password:

```bash
mysql -u root -p < db/init.sql
```

This creates:
- database `taxgap`
- user `taxgap_user` / password `taxgap_pass` with rights on `taxgap`

Tables are created automatically by Hibernate on first start, and the prefilled
users + three mandatory rules are inserted by `DataSeeder`.

> **Using root or different credentials instead?** Override at runtime without
> editing any file:
> ```bash
> DB_USERNAME=root DB_PASSWORD=yourpassword mvn spring-boot:run
> ```

## 3. Run the app

```bash
mvn spring-boot:run
```

The service starts on **http://localhost:8080**.

### Prefilled users

| Username | Password    | Role    | Can do |
|----------|-------------|---------|--------|
| `admin`  | `admin123`  | ADMIN   | Everything, incl. enable/disable rules |
| `auditor`| `auditor123`| AUDITOR | Upload + all reads |

## 4. Run the tests + coverage report

```bash
mvn clean test
```

The JaCoCo HTML report is generated at:

```
target/site/jacoco/index.html
```

Measured coverage for the **service + rule engine** packages is **~62%**
(requirement: 40–50%).

---

## 5. API reference

All endpoints require HTTP Basic auth. Examples below use the `auditor` user;
rule toggling requires `admin`.

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/transactions/batch` | AUDITOR | Upload & validate a batch |
| GET  | `/api/transactions` | AUDITOR | List transactions (`?customerId=`) |
| GET  | `/api/transactions/{id}` | AUDITOR | Get one transaction |
| GET  | `/api/exceptions` | AUDITOR | All exceptions; filter `?customerId=&severity=&ruleName=` |
| GET  | `/api/reports/customer-summary` | AUDITOR | Customer tax summary report |
| GET  | `/api/reports/exception-summary` | AUDITOR | Exception summary report |
| GET  | `/api/rules` | AUDITOR | List configured rules |
| PATCH| `/api/rules/{id}` | ADMIN | Enable/disable a rule |
| GET  | `/api/audit-logs` | AUDITOR | Audit trail (`?transactionId=&eventType=`) |

### Compliance logic

```
expectedTax = amount * taxRate
taxGap      = expectedTax - reportedTax

|taxGap| <= 1  -> COMPLIANT
 taxGap  >  1  -> UNDERPAID
 taxGap  < -1  -> OVERPAID
 reportedTax missing -> NON_COMPLIANT
```

### Mandatory rules (seeded, DB-configurable)

| Rule | Type | Config | Severity |
|------|------|--------|----------|
| HIGH_VALUE_TRANSACTION | HIGH_VALUE | `{"threshold": 100000}` | HIGH |
| REFUND_EXCEEDS_SALES | REFUND_VALIDATION | `{}` | MEDIUM |
| GST_SLAB_VIOLATION | GST_SLAB_VIOLATION | `{"slabThreshold": 50000, "requiredRate": 0.18}` | HIGH |

---

## 6. Sample requests (curl)

**Upload a batch** (the sample file exercises compliant, underpaid,
non-compliant, high-value, GST-slab, refund, and fully-invalid rows):

```bash
curl -u auditor:auditor123 -X POST http://localhost:8080/api/transactions/batch \
  -H "Content-Type: application/json" \
  --data @samples/transaction-upload.json
```

**List transactions for a customer:**

```bash
curl -u auditor:auditor123 "http://localhost:8080/api/transactions?customerId=CUST-02"
```

**Filter exceptions by severity:**

```bash
curl -u auditor:auditor123 "http://localhost:8080/api/exceptions?severity=HIGH"
```

**Customer tax summary report:**

```bash
curl -u auditor:auditor123 http://localhost:8080/api/reports/customer-summary
```

**Exception summary report:**

```bash
curl -u auditor:auditor123 http://localhost:8080/api/reports/exception-summary
```

**Audit trail for a transaction:**

```bash
curl -u auditor:auditor123 "http://localhost:8080/api/audit-logs?transactionId=TXN-1004"
```

**Disable a rule (admin only):**

```bash
curl -u admin:admin123 -X PATCH http://localhost:8080/api/rules/1 \
  -H "Content-Type: application/json" \
  -d '{"enabled": false}'
```

---

## 7. Project structure

```
src/main/java/com/taxgap
├── controller      REST endpoints + global exception handler
├── service         business logic + orchestration
│   └── rules       rule engine strategies (one per RuleType)
├── repository      Spring Data JPA repositories + projections
├── domain          JPA entities
│   └── enums       status/type enums
├── dto             request/response objects
├── security        Spring Security config + UserDetailsService
└── config          startup data seeder
```

---

## 8. Local build note (Jio network machines only)

If your global Maven `settings.xml` mirrors all repositories to the internal
Jio Artifactory, use a settings file that resolves public artifacts. Any of:

- Use the reachable virtual repo `mvn_all` as a `<mirror>` of `*`, **or**
- point `<mirror>` at `https://repo.maven.apache.org/maven2`.

Then build with `mvn -s your-settings.xml ...`. On a normal machine the plain
`mvn` commands above work as-is. (Maven **3.9.x** is recommended; the 4.0.0-rc
build on this machine had an unrelated transport issue.)
