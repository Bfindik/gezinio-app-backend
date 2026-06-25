# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run application (port 8080)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AuthServiceTest

# Run a single test method
./mvnw test -Dtest=AuthServiceTest#methodName
```

Manual API testing script: `test-api.sh` (uses curl + jq against localhost:8080).

## Architecture

Spring Boot 3 / Java 17 REST API for a tourism/travel agency platform. Uses PostgreSQL with Spring Data JPA (`ddl-auto=update` — schema evolves automatically). JWT-based stateless auth with refresh token rotation.

**Package root:** `com.example.gezinio`

**Modules (each with controller → service → repository layers):**

| Module | Responsibility |
|---|---|
| `auth` | Registration, login, JWT + refresh tokens, RBAC (roles/permissions), account lockout after 5 failed attempts |
| `excursion` | Tour, Hotel, Room, Transfer catalog management; tour status workflow DRAFT → ACTIVE |
| `reservation` | Single and group reservations; lifecycle PENDING → CONFIRMED → COMPLETED; publishes domain events |
| `payment` | Multi-currency payments, refunds, duplicate transaction prevention |
| `notification` | Async email via Spring Events — listeners fire on RESERVATION_CREATED, USER_REGISTERED, PAYMENT_COMPLETED, etc.; HTML templates via Thymeleaf |
| `profile` | User tour reviews/ratings and favourite tours/hotels |
| `admin` | Dashboard stats, user management, filtered reservation queries (JPA Specifications) |
| `common` | Shared DTOs used across modules |

**Cross-cutting patterns:**
- Domain events: `ReservationService` and other services publish `AppNotificationEvent` via `ApplicationEventPublisher`; `NotificationService` listens and dispatches emails asynchronously.
- Security: `@EnableMethodSecurity(prePostEnabled = true)` — endpoint-level `@PreAuthorize` guards; `JwtAuthFilter` validates tokens on every request.
- DTOs are separate from JPA entities. Request/response objects live alongside their module (e.g., `ReservationCreateRequest`, `ReservationDTO`).
- Enums model all status/type fields (e.g., `TourStatus`, `ReservationStatus`, `PaymentStatus`, `RefundReason`).

**Key files:**
- `auth/config/SecurityConfig.java` — CORS, JWT filter wiring, endpoint permit rules
- `auth/service/AuthService.java` — JWT issuance, refresh token rotation
- `reservation/service/ReservationService.java` — largest service; group pricing, capacity checks, event publishing
- `payment/service/PaymentService.java` — currency conversion, overpayment prevention, refund logic
- `admin/specification/ReservationSpecification.java` — dynamic JPA filtering

## Configuration

`src/main/resources/application.properties` contains database credentials, JWT secret, and SMTP credentials. Do not commit secrets — use environment variables or a vault in production.