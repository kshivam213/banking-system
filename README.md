# Banking System

A small Spring Boot service exposing login, balance, and deposit APIs backed by Postgres.

## Tech stack

- Java 17, Spring Boot 3 (Web, Data JPA, Validation)
- Postgres + Liquibase migrations
- JJWT for token issuance/validation, BCrypt for PIN hashing
- Gradle wrapper

## Setup

### 1. Postgres

Start a Postgres instance matching `src/main/resources/application.yml`:

```bash
docker run --name banking-pg -e POSTGRES_USER=root -e POSTGRES_PASSWORD=secret \
  -e POSTGRES_DB=banking-system -p 5432:5432 -d postgres:16
```

### 2. Run the app

```bash
./gradlew bootRun
```

On startup, Liquibase creates the `users` and `accounts` tables and seeds a demo
user + account:

- Email: `demo@bank.local`
- PIN: `1234`
- Initial balance: `0.00 USD`

The service listens on `http://localhost:8080`.

## API overview

| Method | Path                         | Auth   | Purpose                  |
|--------|------------------------------|--------|--------------------------|
| POST   | `/api/v1/auth/login`         | none   | Exchange email+PIN → JWT |
| GET    | `/api/v1/accounts/balance`   | Bearer | Current balance          |
| POST   | `/api/v1/accounts/deposit`   | Bearer | Deposit a positive amount|

## Testing the APIs

### 1. Login

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@bank.local","pin":"1234"}' | jq -r .token)
```

### 2. Get balance

```bash
curl -s http://localhost:8080/api/v1/accounts/balance \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Deposit

```bash
curl -s -X POST http://localhost:8080/api/v1/accounts/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"amount": 100.00}'
```

### Negative cases

| Scenario                | Request                                                | Expected           |
|-------------------------|--------------------------------------------------------|--------------------|
| Wrong PIN               | login with `"pin":"0000"`                              | 401 INVALID_CREDENTIALS |
| Missing token           | `GET /balance` without `Authorization`                 | 401 UNAUTHORIZED   |
| Invalid token           | `Authorization: Bearer not-a-token`                    | 401 INVALID_TOKEN  |
| Negative / zero amount  | deposit with `{"amount": -5}` or `0`                   | 400 VALIDATION_ERROR |
| > 2 decimal places      | deposit with `{"amount": 10.123}`                      | 400 VALIDATION_ERROR |

### Concurrency check

Fires 50 parallel deposits of 1.00 and verifies the final balance increased by exactly 50.00:

```bash
BEFORE=$(curl -s http://localhost:8080/api/v1/accounts/balance \
  -H "Authorization: Bearer $TOKEN" | jq -r .balance)

seq 1 50 | xargs -n1 -P50 -I{} curl -s -o /dev/null -X POST \
  http://localhost:8080/api/v1/accounts/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"amount": 1.00}'

AFTER=$(curl -s http://localhost:8080/api/v1/accounts/balance \
  -H "Authorization: Bearer $TOKEN" | jq -r .balance)
echo "Delta: $(echo "$AFTER - $BEFORE" | bc)"  # expect 50.00
```

## Assumptions

- **Single account per user.** `accounts.user_id` has a unique constraint; the demo
  user is provisioned with one USD account at startup.
- **Authentication is by email + numeric PIN**, hashed with BCrypt. There is no
  signup endpoint — users are seeded via Liquibase.
- **JWTs are stateless** (no server-side session/blacklist). Tokens are HS256-signed
  with the secret in `banking.jwt.secret` and expire after `banking.jwt.expiration-minutes`
  (default 60).
- **Currency is fixed to USD** for the demo account. The schema supports other
  currencies but no FX/multi-currency logic is implemented.
- **Deposits are atomic** at the SQL level (`UPDATE ... SET balance = balance + ?`),
  so concurrent deposits do not lose updates and do not return 409s. The
  `@Version` column is retained for future read-modify-write flows (e.g. withdrawals).
- **Withdrawals, transfers, and transaction history are out of scope.**
- **No rate limiting or login throttling.** Suitable for local/demo use only.
- **Secrets in `application.yml`** are placeholders for local development. For any
  real deployment, override via environment variables.

## Project layout

```
src/main/java/com/banking/system
├── configuration/        # Spring beans, MVC interceptors
├── domain/
│   ├── exception/        # Domain exceptions + GlobalExceptionHandler
│   ├── providers/        # Persistence-facing interfaces (ports)
│   └── service/          # AuthService, AccountService
├── integration/
│   ├── database/         # JPA entities, repositories, provider impls
│   ├── models/           # Request/response DTOs
│   └── rest/             # Controllers
└── security/             # JWT issuing/parsing, auth interceptor
```

