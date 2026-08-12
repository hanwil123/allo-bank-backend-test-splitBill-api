# Split Bill API

REST API for splitting shared expenses within a group and figuring out who
owes who at the end. Built for the Allo Bank backend take-home assignment.

## Stack

- Java 17+ (built and tested on Java 24)
- Spring Boot 3.5
- Maven
- H2, in-memory — nothing to install or configure separately
- JUnit 5 + Mockito

## Running it

**Locally with Maven**

```bash
mvn clean install
mvn spring-boot:run
```

API comes up on `http://localhost:8080`. There's an H2 console at
`http://localhost:8080/h2-console` if you want to poke at the data directly
(JDBC URL `jdbc:h2:mem:splitbill`, user `sa`, no password).

**With Docker**

```bash
docker build -t split-bill .
docker run -p 8080:8080 split-bill
```

**Tests**

```bash
mvn test
```

The settlement math is what I spent the most time getting right, so that's
where most of the test coverage lives:

- `SettlementServiceTest` — balance netting, debt simplification, service charge
- `ExpenseServiceTest` — split calculation and rounding behaviour
- `ServiceChargeCalculatorTest` — the personalization formula

## Endpoints

| Method | Path                                  | What it does                        |
|--------|----------------------------------------|--------------------------------------|
| POST   | `/api/groups`                          | Create a group with participants     |
| GET    | `/api/groups`                          | List groups                          |
| GET    | `/api/groups/{groupId}`                | Get one group                        |
| POST   | `/api/groups/{groupId}/expenses`       | Add an expense                       |
| GET    | `/api/groups/{groupId}/expenses`       | List expenses in a group             |
| GET    | `/api/groups/{groupId}/settlement`     | Get the settlement summary           |
| POST   | `/api/groups/{groupId}/payments`       | Record a payment between two people  |
| GET    | `/api/groups/{groupId}/payments`       | List recorded payments               |
| GET    | `/api/groups/{groupId}/audit`          | Full timeline of expenses + payments |

## Trying it out with curl

**Create a group**

```bash
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bali Trip",
    "participants": ["Alice", "Bob", "Charlie"]
  }'
```

Hang on to the `id` fields in the response — you'll need the group id and
each participant's id for everything below.

**Add an expense, split equally across the whole group**

```bash
curl -X POST http://localhost:8080/api/groups/{groupId}/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "paidBy": "aaa...",
    "amount": 90.00,
    "description": "Dinner",
    "category": "FOOD",
    "splitType": "EQUAL"
  }'
```

**Same thing, but only splitting between two of the three people**

```bash
curl -X POST http://localhost:8080/api/groups/{groupId}/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "paidBy": "aaa...",
    "amount": 40.00,
    "description": "Taxi (just Alice and Bob)",
    "category": "TRANSPORT",
    "splitType": "EQUAL",
    "splitAmong": [
      { "participantId": "aaa..." },
      { "participantId": "bbb..." }
    ]
  }'
```

**Exact amounts per person**

```bash
curl -X POST http://localhost:8080/api/groups/{groupId}/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "paidBy": "bbb...",
    "amount": 50.00,
    "description": "Groceries",
    "category": "FOOD",
    "splitType": "EXACT",
    "splitAmong": [
      { "participantId": "aaa...", "amount": 20.00 },
      { "participantId": "bbb...", "amount": 20.00 },
      { "participantId": "ccc...", "amount": 10.00 }
    ]
  }'
```

The amounts have to add up to the total or the request gets rejected.

**Percentage split**

```bash
curl -X POST http://localhost:8080/api/groups/{groupId}/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "paidBy": "ccc...",
    "amount": 200.00,
    "description": "Hotel",
    "category": "ACCOMMODATION",
    "splitType": "PERCENTAGE",
    "splitAmong": [
      { "participantId": "aaa...", "percentage": 50 },
      { "participantId": "bbb...", "percentage": 25 },
      { "participantId": "ccc...", "percentage": 25 }
    ]
  }'
```

Percentages need to add up to 100.

**Settlement summary**

```bash
curl http://localhost:8080/api/groups/{groupId}/settlement
```

```json
{
  "groupId": "b1f2c3d4-...",
  "groupName": "Bali Trip",
  "totalExpenses": 90.00,
  "balances": [
    { "participantId": "aaa...", "participantName": "Alice", "netBalance": 60.00 },
    { "participantId": "bbb...", "participantName": "Bob", "netBalance": -30.00 },
    { "participantId": "ccc...", "participantName": "Charlie", "netBalance": -30.00 }
  ],
  "transactions": [
    { "fromId": "bbb...", "fromName": "Bob", "toId": "aaa...", "toName": "Alice", "amount": 30.00 },
    { "fromId": "ccc...", "fromName": "Charlie", "toId": "aaa...", "toName": "Alice", "amount": 30.00 }
  ],
  "service_charge_pct": 3,
  "service_charge_amount": 2.70
}
```

`transactions` is already the minimal set of transfers needed to settle up —
it's not just a raw list of every debt that ever existed.

**Record a payment**

```bash
curl -X POST http://localhost:8080/api/groups/{groupId}/payments \
  -H "Content-Type: application/json" \
  -d '{
    "from": "bbb...",
    "to": "aaa...",
    "amount": 30.00
  }'
```

Calling settlement again afterwards reflects the payment in everyone's balance.

**Audit trail**

```bash
curl http://localhost:8080/api/groups/{groupId}/audit
```

Every expense and payment for the group, in the order they happened.

## Beyond the minimum

A few things I added on top of what was strictly required:

- Three split strategies (equal, exact, percentage) instead of just one.
- Debt simplification on the settlement endpoint — the API works out the
  smallest possible number of transfers to settle the group instead of
  listing every pairwise debt.
- Payment recording, which nets directly into the balance calculation the
  same way an expense does, just in the opposite direction.
- Expense categories.
- An audit endpoint that merges expenses and payments into one timeline.
- Careful handling of rounding — every split (equal or percentage) rounds
  each share down first, then hands out the leftover cents one at a time so
  the total always reconciles exactly, no matter how the amount divides.

## Personalization

`service_charge_pct` is derived from a GitHub username at runtime — lowercase
it, sum the ASCII value of each character, take that mod 10. See
`ServiceChargeCalculator` for the implementation; the username itself comes
from `app.github-username` in `application.properties`, nothing is hardcoded.

**GitHub username:** `hanwil123`
**Calculated `service_charge_pct`:** `3` (ASCII sum of "hanwil123" is 793, 793 % 10 = 3)

## Submission question

**What was the hardest design decision you made while building this, and what trade-off did you accept?**

Getting the three split types to share the same rounding behaviour without
losing or gaining a cent was the part I spent the most time on. Splitting
90.00 three ways works out cleanly to 30.00 each, but 100.00 doesn't —
33.33 × 3 only comes to 99.99. I round every share down first and then hand
out the leftover cents one at a time to participants in order, rather than
rounding each share independently, because independent rounding can push
the total past the original amount, which isn't acceptable when the numbers
represent real money. The trade-off is that whoever's first in the
participant list always absorbs the extra cent when there's a tie, which is
a bit arbitrary rather than random or rotated. That's fine for this
assignment, but if this were going into production I'd rotate the starting
participant per expense instead so the same person isn't always the one
picking up the difference.