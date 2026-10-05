# OrderFlow — Project Handoff and Current-State Summary

## 1. Purpose and current maturity

**OrderFlow** is a backend-only e-commerce/order-management REST API. It is built as a modular monolith: one Spring Boot application with separate inventory, user/address, cart/order, payment, common-exception, and configuration packages. There is no frontend, authentication implementation, external payment gateway, message broker, or deployment configuration in the repository.

The project already implements the main commerce flow:

1. Maintain categories and products.
2. Register users and maintain their addresses.
3. Create one cart per user and add, update, remove, or clear cart items.
4. Check out a cart into an order, snapshot its product and shipping-address details, and decrement stock.
5. Simulate a payment and maintain order status, including stock restoration on cancellation.

This is an in-progress development repository, not production-ready software. The working tree contains uncommitted changes that improve exception classification/HTTP responses and change checkout to use a failing simulated payment. This document describes the **current working-tree implementation**, including those changes.

## 2. Technology and how to run it

| Area | Current implementation |
| --- | --- |
| Language/runtime | Java 21 |
| Framework | Spring Boot 4.1.0, Spring MVC, Spring Data JPA |
| Persistence | MySQL is the configured runtime database; H2 is declared only as a runtime dependency |
| Build | Maven Wrapper (`mvnw` / `mvnw.cmd`) |
| Boilerplate reduction | Lombok |
| API documentation dependency | Springdoc OpenAPI WebMVC UI 3.0.3; no custom OpenAPI configuration is supplied |
| Tests | One Spring context-load test only |

The service is configured in `src/main/resources/application.properties` to connect to:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/OrderFlow
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always
```

Before starting it, create and run a MySQL instance with an `OrderFlow` database and credentials matching the properties, or change those properties to fit the local environment. Hibernate creates/updates the schema and then `data.sql` runs on every startup. SQL and web request logging are enabled at verbose/debug levels.

Typical commands are:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```

### Build verification performed for this handoff

The tests could not be executed in this workspace because the Maven Wrapper failed before Maven started (`Cannot start maven from wrapper`, caused by a PowerShell wrapper error), and no system `mvn` command is installed. This is an environment/tooling blocker, not a test result. The repository's only test is a context-load smoke test, so business flows currently have no automated coverage regardless.

## 3. Repository layout

```text
OrderFlow/
├── pom.xml                                  Maven dependencies/build configuration
├── src/main/resources/
│   ├── application.properties                MySQL, JPA, logging, data-initialization settings
│   └── data.sql                              Repeatable development seed/update script
└── src/main/java/com/example/OrderFlow/
    ├── OrderFlowApplication.java             Spring Boot entry point
    ├── Common/
    │   ├── Entity/AuditableBase.java          createdAt/updatedAt lifecycle base class
    │   └── Exception/                         Domain exceptions and REST exception handler
    ├── Config/SecurityConfig.java             Empty configuration placeholder
    ├── InventoryService/                     Categories/products, DTOs, repositories, services, controllers
    ├── UserService/                          Users/addresses, DTOs, repositories, services, controllers
    ├── OrderService/                         Carts, cart items, orders, order items, checkout/status logic
    └── PaymentService/                       Payment entity, simulation service, currently no controller
```

Each functional module broadly follows: **Controller → Service interface → Service implementation → Repository → JPA entity**, with request/response DTOs and mapper components at the API boundary.

## 4. Domain model and persistence design

### Core entities

| Entity/table | Important fields | Relationships and role |
| --- | --- | --- |
| `Category` / `categories` | `id`, unique `cat_name`, `description`, audit timestamps | One category is referenced by many products. |
| `Product` / `products` | immutable unique `sku`, `name`, `description`, `price`, `stock_qty`, `low_stock_qty`, `active`, audit timestamps | Many-to-one to `Category`. Product deletion is soft deletion (`active=false`). |
| `User` / `users` | `id`, `name`, unique `email`, `password`, audit timestamps | No role is persisted despite a `Role` enum existing. Password is stored as plaintext. |
| `Address` / `addresses` | address fields | Many-to-one to `User`; addresses are used to create an order-time shipping snapshot. It has no audit timestamps. |
| `Cart` / `carts` | unique scalar `user_id`, `total_amount`, audit timestamps | One-to-many to `CartItem`, cascade all with orphan removal. `user_id` is a scalar, not a JPA relationship or foreign key to `User`. |
| `CartItem` / `cart_items` | product, `quantity`, `sub_total`, audit timestamps | Many-to-one to `Cart` and `Product`; unique constraint on `(cart_id, product_id)` permits only one line per product per cart. |
| `Order` / `orders` | unique `order_number`, scalar `user_id`, embedded shipping address, `total_amount`, `status`, `payment_status`, audit timestamps | One-to-many to `OrderItem`, cascade all with orphan removal. The user is recorded as an ID only. |
| `OrderItem` / `order_items` | product ID/name/SKU, price at order, quantity, subtotal | Stores a product snapshot, so historical order data survives later product edits/deactivation. It has no timestamps and no direct JPA product relation. |
| `Payment` / `payments` | unique scalar `order_id`, status, amount, mode, unique transaction reference, audit timestamps | One payment per order is enforced by a unique column, but there is no JPA relationship/foreign key to `Order`. |

`AuditableBase` sets `createdAt` and `updatedAt` with JPA `@PrePersist`/`@PreUpdate`; it is inherited by category, product, user, cart, cart item, order, and payment.

### Enumerations

| Enumeration | Values | Used by |
| --- | --- | --- |
| `OrderStatus` | `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED` | `Order.status` |
| `PaymentStatus` | `PENDING`, `SUCCESS`, `FAILED`, `REFUNDED` | `Order.paymentStatus`, `Payment.paymentStatus` |
| `PaymentMode` | `CARD`, `UPI` | `Payment.paymentMode` |
| `Role` | `USER`, `ADMIN` | Defined only; not wired into entity, API, or security. |

## 5. API surface

All request and response bodies are JSON unless a controller returns a plain success string. No route is authenticated or role-protected. Callers pass `userId` explicitly where needed.

### Inventory — categories

Base path: `/api/v1/categories`

| Method/path | Body/query | Behavior |
| --- | --- | --- |
| `POST /` | `{ "name", "description" }` | Creates a category; name must be nonblank and unique. Returns `201`. |
| `PUT /{id}` | Same request body | Updates name and description. Returns `200`. **Current defect:** supplying the category's existing name is rejected as a duplicate, so a description-only update using the current name fails. |
| `GET /{id}` | — | Gets one category. |
| `GET /` | — | Lists all categories. |
| `DELETE /{id}` | — | Deletes a category only if no product refers to it. Returns a success string. |

### Inventory — products

Base path: `/api/v1/products`

| Method/path | Body/query | Behavior |
| --- | --- | --- |
| `POST /` | `{ "name", "description", "price", "stockQty", "lowStockThreshold", "categoryId" }` | Creates a product in an existing category, assigns a randomly generated immutable `PRD-XXXXXXXX` SKU, and returns `201`. |
| `PUT /{id}` | Same body | Updates mutable product fields and category; SKU remains unchanged. Controller validation means all non-null request fields are effectively required on this route. |
| `GET /{id}` | — | Gets a product by ID. |
| `GET /sku/{sku}` | — | Gets a product by SKU. |
| `GET /` | — | Lists all products, including inactive (soft-deleted) products. |
| `GET /category/{categoryId}` | — | Lists products for that category; also includes inactive products. |
| `DELETE /{id}` | — | Soft deletes by setting `active=false`; returns `204`. |

`ProductRepository` has a low-stock query (`stockQuantity <= lowStockThreshold`), but no service method or HTTP route exposes it yet.

### Users and addresses

User base path: `/api/users`

| Method/path | Body/query | Behavior |
| --- | --- | --- |
| `POST /signup` | `{ "name", "email", "password" }` | Creates a user. Name is required, email must have email format, password minimum length is six. Returns a response without password and HTTP `201`. |
| `POST /login` | `{ "email", "password" }` | Compares plaintext password and returns the user response on success. It creates no session/JWT/token. |
| `PUT /{id}` | User registration-shaped body | Updates name/email only; password is not updated. |
| `DELETE /{id}` | — | Deletes the user record. Related addresses/carts/orders are not comprehensively handled. |

Address base path: `/api/address`

| Method/path | Body/query | Behavior |
| --- | --- | --- |
| `POST /{userId}` | `{ "street", "city", "state", "zipCode", "country" }` | Creates an address for an existing user. Returns `201`. |
| `PUT /{addressId}?userId={userId}` | Partial address object | Updates non-null address fields after checking the supplied user ID owns the address. |
| `DELETE /{addressId}` | — | Deletes an address with no ownership/authentication check. |

There is no endpoint to retrieve a user's addresses, even though the repository supports the query and `UserAddressDTO` exists.

### Cart

Base path: `/api/v1/cart`

| Method/path | Body/query | Behavior |
| --- | --- | --- |
| `GET /?userId={id}` | — | Returns a cart; creates an empty cart if none exists. It does not verify that the user ID exists. |
| `POST /items?userId={id}` | `{ "productId", "quantity" }` | Adds quantity to a cart. New/existing quantity must be positive and no greater than current stock; inactive products are rejected. If the product already exists in the cart, its quantity is increased. |
| `PUT /items?userId={id}` | `{ "productId", "quantity" }` | Sets an existing cart-line quantity. Quantity `0` removes the line; a positive quantity is checked against current stock. |
| `DELETE /items?userId={id}&productId={id}` | — | Removes one line. |
| `DELETE /?userId={id}` | — | Clears all lines and resets the total; the cart record remains. |

Cart totals and line subtotals are recalculated in entity/service logic from the current product price. Adding a product does **not** reserve/decrement stock; availability is checked again at checkout.

### Orders

Base path: `/api/v1/orders`

| Method/path | Body/query | Behavior |
| --- | --- | --- |
| `POST /checkout?userId={id}&shippingAddressId={id}` | — | Converts a nonempty cart into an order and attempts a simulated payment; full behavior is described in section 6. The controller marks `shippingAddressId` optional, but the service requires it in practice. |
| `GET /{id}` | — | Gets an order by numeric ID. |
| `GET /number/{orderNumber}` | — | Gets an order by its `ORD-XXXXXXXX-XXXXXXXX` number. |
| `GET /user/{userId}` | — | Lists orders for a user ID. |
| `GET /status/{status}` | — | Lists orders by enum status. |
| `PATCH /{id}/status?status={status}` | — | Applies a permitted non-cancellation status transition. |
| `PATCH /{id}/cancel` | — | Cancels a pending/confirmed order and restores stock. |

Typical order response shape:

```json
{
  "id": 1,
  "orderNumber": "ORD-ABCD1234-EFGH5678",
  "userId": 1,
  "shippingAddress": {
    "street": "221 MG Road",
    "city": "Bengaluru",
    "state": "Karnataka",
    "zipCode": "560001",
    "country": "India"
  },
  "orderStatus": "CANCELLED",
  "paymentStatus": "FAILED",
  "totalAmount": 14598.00,
  "items": [
    {
      "productId": 1,
      "sku": "SKU-MOB-001",
      "productName": "Samsung Galaxy M14",
      "priceAtOrder": 12999.00,
      "quantity": 1,
      "subTotal": 12999.00
    }
  ]
}
```

### Payments

There is **no `PaymentController` endpoint**. `PaymentService.processPayment(...)` is currently internal-only and is called by checkout. The `PaymentController` class is an empty placeholder.

## 6. Checkout, payment, stock, and order-state behavior

### Checkout sequence as currently coded

```text
Cart for user
  │
  ├─ must contain at least one item
  ├─ selected address must belong to user
  ├─ each product must still be active and have enough stock
  │
  ▼
Create PENDING order
  ├─ copy shipping address into embedded order snapshot
  ├─ copy product identity/name/SKU/current price into order-item snapshots
  ├─ decrement product stock
  └─ calculate total
  │
  ▼
Save order and clear cart
  │
  ▼
Call simulated CARD payment
  │
  ├─ SUCCESS → order becomes CONFIRMED, payment status SUCCESS
  └─ FAILED  → order payment status FAILED; cancellation restores stock;
               order becomes CANCELLED and cart remains empty
```

### Critical current behavior: every checkout fails payment

`OrderServiceImpl.checkout` currently constructs the payment request with `simulatedPayment=false`. `PaymentServiceImpl` maps that flag to `PaymentStatus.FAILED`; only `simulatedPayment=true` produces `SUCCESS`. Therefore, using the exposed checkout endpoint currently produces:

- a persisted `Payment` row with status `FAILED`;
- a persisted order with status `CANCELLED` and payment status `FAILED`;
- stock first decremented then restored by `cancelOrder`;
- a cleared cart (items are not restored).

This behavior comes from uncommitted working-tree changes. In the committed baseline checkout used `simulatedPayment=true`, so it confirmed successful orders. A real payment gateway or explicit development-mode payment result needs to replace this hard-coded simulation before a meaningful checkout workflow can succeed.

### Status transition rules

The normal `PATCH /{id}/status` route prohibits setting `CANCELLED`; clients must use the dedicated cancel endpoint.

| Current status | Allowed normal transition | Cancellation allowed? |
| --- | --- | --- |
| `PENDING` | `CONFIRMED` | Yes |
| `CONFIRMED` | `SHIPPED` | Yes |
| `SHIPPED` | `DELIVERED` | No |
| `DELIVERED` | None | No |
| `CANCELLED` | None | No |

Cancelling an eligible order restores each order item's quantity to the current product stock. Refund support is explicitly left as a TODO, so cancelling a successfully paid order does not change an existing payment record to `REFUNDED` or invoke a refund provider.

## 7. Exceptions and HTTP response behavior

The new `GlobalExceptionHandler` maps current domain exceptions to plain-text response bodies:

| Exception | Intended cases | HTTP status |
| --- | --- | --- |
| `ResourceNotFoundException` | Missing category/product/user/address/cart/order | `404 Not Found` |
| `DuplicateResourceException` | Duplicate category/email; uniqueness generation failure | `409 Conflict` |
| `InsufficientStockException` | Requested/cart/checkout quantity exceeds stock | `409 Conflict` |
| `InvalidOrderStateException` | Empty checkout, illegal status transition/cancellation | `409 Conflict` |
| `IllegalArgumentException` | Bad credentials, invalid quantity, unauthorized address update | `400 Bad Request` |
| Any other exception | Unhandled failure | `500 Internal Server Error`, body `Something went wrong` |

There is not yet a structured error schema, field-level validation handler, correlation ID, or logging strategy for returned errors. The broad catch-all can turn request-validation errors and database constraint violations into a generic `500` response rather than a useful client error.

## 8. Development seed data

`src/main/resources/data.sql` is designed to be idempotent for its named seed records: it inserts if absent and updates records if present. It supplies:

| Area | Seed records |
| --- | --- |
| Categories | `Electronics`, `Groceries` |
| Users | Rahul Sharma (`rahul@example.com` / plaintext `rahul123`), Priya Verma (`priya@example.com` / plaintext `priya123`) |
| Addresses | Rahul: 221 MG Road, Bengaluru; Priya: 14 Park Street, Kolkata |
| Products | `SKU-MOB-001` Samsung Galaxy M14 (₹12,999, 25 stock), `SKU-AUD-001` Boat Rockerz 450 (₹1,599, 40 stock), `SKU-GRC-001` Basmati Rice 5kg (₹799, 60 stock) |
| Carts | Rahul: phone + headphones, total ₹14,598; Priya: headphones + rice, total ₹2,398 |
| Orders/payments | None |

Because SQL initialization is set to `always`, every startup resets the named seed products, carts, and cart line values/totals to the values in this script. This is helpful for local demos but unsafe for a durable shared/production database: it can overwrite stock and cart changes. The SQL uses MySQL-specific `UPDATE ... JOIN` syntax, so switching to the included H2 dependency without replacing the script will fail.

## 9. Current implementation status

### Implemented

- JPA schema generation and MySQL-backed persistence.
- Category CRUD with category-in-use delete prevention.
- Product CRUD/listing, generated SKU, stock fields, soft deletion, and category lookup.
- User signup, plaintext login check, update (name/email), and delete.
- Address creation, partial update with supplied-user ownership check, deletion, and order-address lookup.
- One persistent cart per user ID, cart-line uniqueness, stock-aware add/update, total calculation, removal, clearing.
- Checkout snapshots, stock deduction/restoration, order lookup/listing, order numbers, and controlled lifecycle changes.
- Payment record creation with random transaction-reference generation and simulated success/failure outcome.
- Domain-specific exceptions and central response mapping (currently uncommitted).

### Placeholders or not implemented

- Authentication, authorization, roles, password hashing, token/session issuance, ownership enforcement across most resources.
- A real security configuration (`SecurityConfig` is empty).
- Payment REST endpoints, payment retrieval, payment retry, idempotency, gateway integration, webhook handling, and refunds.
- A working successful checkout path through the exposed API while `simulatedPayment=false` is hard-coded.
- User and address retrieval endpoints; password update/reset flow.
- Low-stock API/notifications, inventory reservations, and inventory audit/movement history.
- Pagination, sorting, filtering/search, rate limiting, API version consistency, and OpenAPI customization.
- Database migrations (Flyway/Liquibase), environment profiles/secrets management, containers, CI/CD, observability, and business-flow tests.

## 10. Known issues and implementation risks

These are the most important points for anyone taking over development.

1. **Checkout is intentionally/accidentally failing today.** It always sends `simulatedPayment=false`, which creates failed/cancelled orders and empties carts. Decide whether the current change is a temporary failure-path test or restore success simulation before testing user-facing checkout.
2. **No security exists.** All data is accessible anonymously; arbitrary `userId` query/path parameters can act on another user's cart/orders. Passwords are stored and compared in plaintext. The empty security config does not protect anything.
3. **Order retrieval may fail due to lazy loading.** `Order.items` is lazily loaded by default. Read methods (`getOrderById`, number lookup, user/status listings) are not transactional, but their mapper reads `order.getItems()`. Depending on persistence-session behavior this can result in `LazyInitializationException`. Use transactional reads, fetch joins/entity graphs, or DTO projections and add tests.
4. **Category update rejects its own name.** The duplicate check does not exclude the category being updated.
5. **Request validation/error handling needs refinement.** Product price has no `@PositiveOrZero`; address DTOs/controllers have no validation; the broad exception handler returns generic 500 responses for many framework/database validation errors. Return a documented JSON error shape and correct 400/409 codes.
6. **Referential integrity is mixed.** Cart/order/payment retain scalar `userId`/`orderId` fields rather than JPA relationships. Deleting a user does not cascade/clean associated addresses, carts, orders, or payments and can conflict with database foreign keys on addresses.
7. **No concurrency control for inventory.** Checkout validates and decrements stock without pessimistic/optimistic locking. Concurrent checkouts can oversell products.
8. **Payment persistence and cancellation are not fully coherent.** A confirmed order cancellation restores inventory but does not refund/update a successful payment. Re-processing the same order would also collide with the unique payment `order_id` constraint.
9. **Seed data overwrites development state each startup.** Do not use the current initialization policy in a shared or production environment.
10. **API consistency needs work.** Most routes are `/api/v1`, while users and addresses are `/api/users` and `/api/address`; resource names are inconsistent (`cart` singular, `address` singular). There are no GET routes for users or addresses.
11. **Automated testing is essentially absent.** There is only a context-load test and it has not been run here because Maven tooling is unavailable/broken in this workspace.
12. **Cleanup is needed.** Several imports are unused; the payment controller and role/UserAddress DTO are placeholders; README is only a title and `HELP.md` is framework boilerplate.

## 11. Working-tree and version-control state

The latest committed change is `b057dd2` (`a major commit now most things are working orde rproduct cart payment (albeit manualt testing only) now refingin only left and then auth and toher things`). The branch is `main` tracking `origin/main`.

There are local, uncommitted changes in 17 existing source files, plus this new handoff document. Five new source files are staged (`SecurityConfig` plus four domain exception classes); additional source modifications are unstaged. The changes primarily:

- introduce 404/409/400 exception mappings and replace generic exceptions in several services;
- improve category/product/user/address/cart/order error paths;
- add a category-in-use check before deletion;
- add Cart's item list builder default;
- correct payment transaction-reference repository method naming;
- change checkout's simulated payment flag from `true` to `false`;
- add a refund TODO in cancellation.

Treat this tree as active work. Do not discard or overwrite it without first deciding whether the new failing-payment behavior is intended and whether these changes should be committed as one coherent error-handling/checkout-state commit.

## 12. Recommended takeover plan

1. Fix local build tooling, point configuration to an isolated MySQL development database, and add automated tests for every endpoint and all checkout outcomes.
2. Decide the payment simulation contract. For a demo, make it explicit/request-driven; for production, introduce a gateway abstraction, payment lifecycle, idempotency, and refund flow.
3. Implement authentication/authorization before exposing the API: hash passwords, use a real security configuration, derive user identity from authentication rather than user-supplied IDs, and model/authorize roles.
4. Stabilize persistence rules: introduce migrations, move seed data to a development profile, add FK/relationship decisions deliberately, and protect inventory with locking or reservation logic.
5. Repair API correctness: transaction-safe order reads, category self-update, validation/error responses, ownership checks, user/address read endpoints, pagination, and API documentation.
6. Add observability and delivery basics: structured logging, health checks, environment-based configuration/secrets, containerization, and CI.

## 13. Quick orientation for a new developer

Start with these files in order:

1. `src/main/resources/application.properties` and `data.sql` for local runtime/data behavior.
2. `OrderService/Service/impl/OrderServiceImpl.java` for the core checkout and order lifecycle.
3. `OrderService/Service/impl/CartServiceImpl.java` and `PaymentService/Service/impl/PaymentServiceImpl.java` for stock/cart/payment rules.
4. `InventoryService`, `UserService`, and `OrderService` controllers for the actual public API contract.
5. `Common/Exception/GlobalExceptionHandler.java` for current response behavior.

The core design principle currently implemented is: carts use live product prices and do not reserve stock; orders snapshot product and shipping details at checkout; cancellation restores stock only while an order is pending or confirmed. Everything security-, payment-gateway-, and production-operations-related remains future work.
