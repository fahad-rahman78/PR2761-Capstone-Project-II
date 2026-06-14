# Generalised &amp; Adaptive Booking Management System

Capstone 2 implementation of the Phase 1 design document. A three-tier web
application that lets users book time slots on shared resources and
**guarantees no double-bookings even under heavy concurrent load.**

- **Backend:** Java 17, Spring Boot 3, Spring Data JPA
- **Database:** PostgreSQL 15+ (H2 in-memory for tests)
- **Frontend:** plain HTML + CSS + JavaScript (served by the backend)
- **Load testing:** Apache JMeter

## How double-bookings are prevented

Three layers of defence (outer to inner):

1. **Advisory lock** – `pg_advisory_xact_lock(resourceId)` serialises every
   booking attempt for the same resource. Unlike `SELECT ... FOR UPDATE`, this
   works even when no rows exist yet, which is exactly where the classic race
   condition happens.
2. **Capacity check inside the lock** – count overlapping confirmed bookings;
   reject if already at capacity. The lock means the count can't change under us.
3. **Database safety-net** (`db/hardening.sql`, optional) – an `EXCLUDE`
   constraint that makes PostgreSQL itself refuse an overlapping insert.

## Quick start

```bash
# 1. Create the database (PostgreSQL must be running)
createdb booking_db

# 2. Run the app (tables + sample data are created automatically)
mvn spring-boot:run

# 3. Open the UI
#    http://localhost:8080
```

Full step-by-step instructions for any OS are in **SETUP_GUIDE.pdf**.
A file-by-file explanation of the codebase is in **FILE_GUIDE.pdf**.

## Run the tests

```bash
mvn test
```

Unit tests (Mockito) and integration tests (H2) run with no database setup.

## Load test (proves the guarantee)

1. Start the app with a clean database.
2. Open `jmeter/booking-stress-test.jmx` in Apache JMeter.
3. Run it. Scenario 1 fires 50 identical bookings at once.
   **Expected: exactly 1 success (201) and 49 conflicts (409).**

## API summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/resources` | list bookable resources |
| GET | `/api/resources/{id}/slots?date=YYYY-MM-DD` | available slots for a day |
| POST | `/api/bookings` | create a booking (201 / 409) |
| GET | `/api/bookings/{id}` | booking details |
| DELETE | `/api/bookings/{id}` | cancel a booking |
| GET | `/api/users/{userId}/bookings` | a user's bookings |
| GET | `/api/users` | list users (for the demo UI) |
