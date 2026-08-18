# OrderFlow API

Java 21 | Spring Boot 4.1 | MySQL | Maven

OrderFlow is a backend REST API for an e-commerce order workflow. It manages
categories, products, users, addresses, carts, checkout, orders, stock updates,
and simulated payments using a layered Spring Boot architecture.

This repository is currently an active development project. The core commerce
flow exists, but authentication, production payment integration, Docker support,
stronger automated testing, and deployment hardening are still pending.

## Project Status

Functional core:

- Category and product management.
- User signup and plaintext login check.
- Address creation/update/delete.
- One cart per user with item add/update/remove/clear operations.
- Checkout flow that snapshots address/product data into an order.
- Stock decrement during checkout and stock restoration during cancellation.
- Simulated payment record creation.
- Centralized domain exception handling.

Under active development:

- Authentication and authorization.
- Password hashing.
- Production-ready payment handling.
- Successful checkout behavior through the exposed API.
- Broader validation, tests, and deployment configuration.

Important current checkout note:

The current working-tree implementation sends `simulatedPayment=false` during
checkout. Because the payment service treats that flag as a failed payment, the
public checkout endpoint currently creates a failed payment, cancels the order,
restores stock, and leaves the cart cleared. This is useful for testing the
failure path, but it means checkout does not currently complete as a successful
confirmed order through the API.

For a full handoff-level explanation of the current implementation, see
`PROJECT_HANDOFF.md`.

## Why This Project?

OrderFlow was built to practice and demonstrate Spring Boot backend development
around a realistic commerce domain. The project focuses on layered architecture,
DTO-based API boundaries, JPA entity modeling, inventory checks, order lifecycle
rules, payment-state handling, centralized exceptions, and MySQL-backed
persistence.

## Features

- Category CRUD with duplicate-name checks and delete protection when products
  reference a category.
- Product CRUD with generated immutable SKUs, category assignment, stock fields,
  low-stock threshold storage, and soft delete using `active=false`.
- User signup, login check, profile update, and deletion.
- Address management with ownership checks during update.
- Persistent cart per user ID.
- Cart item uniqueness per product and automatic cart total recalculation.
- Stock validation when adding/updating cart items and again during checkout.
- Order creation from cart contents.
- Shipping address snapshot stored directly on each order.
- Product snapshot stored on each order item, including product ID, name, SKU,
  price at order, quantity, and subtotal.
- Order lookup by ID, order number, user ID, and status.
- Controlled order status transitions.
- Cancellation for pending/confirmed orders with inventory restoration.
- Simulated payment creation with status, amount, payment mode, and transaction
  reference.
- Domain-specific exceptions mapped to HTTP 400, 404, and 409 responses.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Web | Spring MVC |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL |
| Runtime test DB dependency | H2, declared but not currently configured as the main runtime |
| API docs dependency | Springdoc OpenAPI WebMVC UI 3.0.3 |
| Utilities | Lombok |
| Build tool | Maven / Maven Wrapper |
| Testing | Spring Boot Test, currently only a context-load smoke test |

## Architecture

### Layer and Request Flow

```text
HTTP Request
    |
    v
Controller Layer
    - owns route mapping
    - validates request DTOs where annotations exist
    - delegates to service interfaces
    |
    v
Service Layer
    - applies business rules
    - validates domain state
    - coordinates repositories and mappers
    |
    v
Repository Layer
    - Spring Data JPA interfaces
    - database reads/writes
    |
    v
MySQL Database
```

### Directory Structure

```text
src/main/java/com/example/OrderFlow/
|-- OrderFlowApplication.java
|-- Common/
|   |-- Entity/
|   |   `-- AuditableBase.java
|   `-- Exception/
|       |-- GlobalExceptionHandler.java
|       |-- ResourceNotFoundException.java
|       |-- DuplicateResourceException.java
|       |-- InsufficientStockException.java
|       `-- InvalidOrderStateException.java
|-- Config/
|   `-- SecurityConfig.java
|-- InventoryService/
|   |-- Controller/
|   |-- DTO/
|   |-- Entity/
|   |-- Mapper/
|   |-- Repository/
|   `-- Service/
|-- UserService/
|   |-- Controller/
|   |-- DTO/
|   |-- Entity/
|   |-- Mapper/
|   |-- Repository/
|   `-- Service/
|-- OrderService/
|   |-- Controller/
|   |-- DTO/
|   |-- Entity/
|   |-- Mapper/
|   |-- Repository/
|   `-- Service/
`-- PaymentService/
    |-- Controller/
    |-- DTO/
    |-- Entity/
    |-- Repository/
    `-- Service/
```

## Design Decisions

### Why DTOs?

DTOs keep the public API contract separate from the internal JPA entity model.
That makes it easier to hide sensitive/internal fields, prevent accidental
serialization of relationships, and evolve request/response payloads without
directly exposing database mappings.

### Why Snapshot Order Data?

Orders store copies of product and shipping-address details at checkout time.
This protects historical order records from later changes to product names,
prices, SKUs, or user addresses.

### Why Soft Delete Products?

Product deletion marks `active=false` instead of deleting the row. This helps
preserve order history and prevents old order items from pointing at missing
product data. Cart and checkout logic reject inactive products.

### Why a Separate Payment Service?

Payment handling is isolated behind `PaymentService`, even though it is still a
simulation today. This gives the project a natural place to add gateway
integration, payment retries, idempotency, webhooks, refunds, and reconciliation
later.

### Current Security Position

`SecurityConfig` exists as a placeholder, but there is no real authentication or
authorization yet. Passwords are currently stored and compared in plaintext, and
API callers pass `userId` values directly in path/query parameters.

## Database Schema and Relationships

```text
Category 1 --- * Product

User 1 --- * Address

Cart 1 --- * CartItem * --- 1 Product

Order 1 --- * OrderItem

Order 1 --- 1 Payment
```

Note: Some relationships are modeled as scalar IDs instead of full JPA
relationships. For example, `Cart.userId`, `Order.userId`, and
`Payment.orderId` are stored as IDs rather than entity references.

### Table Overview

```text
categories
|-- id
|-- cat_name unique
|-- description
|-- created_at
`-- updated_at

products
|-- id
|-- sku unique
|-- name
|-- description
|-- price
|-- stock_qty
|-- low_stock_qty
|-- active
|-- category_id
|-- created_at
`-- updated_at

users
|-- id
|-- name
|-- email unique
|-- password
|-- created_at
`-- updated_at

addresses
|-- id
|-- user_id
|-- street
|-- city
|-- state
|-- zip_code
`-- country

carts
|-- id
|-- user_id unique
|-- total_amount
|-- created_at
`-- updated_at

cart_items
|-- id
|-- cart_id
|-- product_id
|-- quantity
|-- sub_total
|-- created_at
`-- updated_at

orders
|-- id
|-- order_number unique
|-- user_id
|-- shipping street/city/state/zip/country
|-- total_amount
|-- status
|-- payment_status
|-- created_at
`-- updated_at

order_items
|-- id
|-- order_id
|-- product_id
|-- product_name
|-- sku
|-- price_at_order
|-- quantity
`-- sub_total

payments
|-- id
|-- order_id unique
|-- payment_status
|-- amount
|-- payment_mode
|-- transaction_reference unique
|-- created_at
`-- updated_at
```

## Getting Started

### Prerequisites

- Java 21+
- MySQL 8+
- Maven 3.6+ or a working Maven Wrapper

### 1. Clone the Repository

```powershell
git clone <repository-url>
cd OrderFlow
```

### 2. Create the Database

```sql
CREATE DATABASE OrderFlow;
```

### 3. Configure Database Access

The current local configuration is stored in:

```text
src/main/resources/application.properties
```

Default values:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/OrderFlow
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always
```

Update these values if your local MySQL username, password, host, port, or
database name is different.

### 4. Build and Run

```powershell
.\mvnw.cmd spring-boot:run
```

If the Maven Wrapper fails on your machine, use a local Maven installation:

```powershell
mvn spring-boot:run
```

The server starts by default at:

```text
http://localhost:8080
```

### 5. Run Tests

```powershell
.\mvnw.cmd test
```

Current note: in this workspace the Windows Maven Wrapper failed before Maven
started, and `mvn` was not installed globally. The repository also currently has
only a context-load smoke test, so business-flow test coverage still needs to be
added.

## Seed Data

`src/main/resources/data.sql` runs on startup because
`spring.sql.init.mode=always`.

Seeded data includes:

| Area | Records |
| --- | --- |
| Categories | Electronics, Groceries |
| Users | Rahul Sharma, Priya Verma |
| Addresses | One address for Rahul, one for Priya |
| Products | Samsung Galaxy M14, Boat Rockerz 450, Basmati Rice 5kg |
| Carts | Pre-filled carts for Rahul and Priya |
| Orders/payments | None |

The seed script is helpful for demos, but it updates the same seeded records on
every startup. Do not use the current initialization mode against a shared or
production database.

## API Reference

The API currently returns DTOs or plain success/error messages depending on the
controller. A consistent response wrapper is not implemented yet.

### Category Endpoints

Base path:

```text
/api/v1/categories
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/v1/categories` | Create category |
| PUT | `/api/v1/categories/{id}` | Update category |
| GET | `/api/v1/categories/{id}` | Get category by ID |
| GET | `/api/v1/categories` | List all categories |
| DELETE | `/api/v1/categories/{id}` | Delete category if unused |

Example create request:

```json
{
  "name": "Electronics",
  "description": "Phones, laptops, and accessories"
}
```

### Product Endpoints

Base path:

```text
/api/v1/products
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/v1/products` | Create product |
| PUT | `/api/v1/products/{id}` | Update product |
| GET | `/api/v1/products/{id}` | Get product by ID |
| GET | `/api/v1/products/sku/{sku}` | Get product by SKU |
| GET | `/api/v1/products` | List all products |
| GET | `/api/v1/products/category/{categoryId}` | List products by category |
| DELETE | `/api/v1/products/{id}` | Soft delete product |

Example create request:

```json
{
  "name": "Samsung Galaxy M14",
  "description": "5G Android phone",
  "price": 12999.00,
  "stockQty": 25,
  "lowStockThreshold": 5,
  "categoryId": 1
}
```

### User Endpoints

Base path:

```text
/api/users
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/users/signup` | Register user |
| POST | `/api/users/login` | Plaintext login check |
| PUT | `/api/users/{id}` | Update name/email |
| DELETE | `/api/users/{id}` | Delete user |

Example signup request:

```json
{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "password": "rahul123"
}
```

Example login request:

```json
{
  "email": "rahul@example.com",
  "password": "rahul123"
}
```

### Address Endpoints

Base path:

```text
/api/address
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/address/{userId}` | Create address for user |
| PUT | `/api/address/{addressId}?userId={userId}` | Update address after ownership check |
| DELETE | `/api/address/{addressId}` | Delete address |

Example create request:

```json
{
  "street": "221 MG Road",
  "city": "Bengaluru",
  "state": "Karnataka",
  "zipCode": "560001",
  "country": "India"
}
```

### Cart Endpoints

Base path:

```text
/api/v1/cart
```

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/v1/cart?userId={userId}` | Get or create cart |
| POST | `/api/v1/cart/items?userId={userId}` | Add item to cart |
| PUT | `/api/v1/cart/items?userId={userId}` | Update cart item quantity |
| DELETE | `/api/v1/cart/items?userId={userId}&productId={productId}` | Remove item from cart |
| DELETE | `/api/v1/cart?userId={userId}` | Clear cart |

Example cart item request:

```json
{
  "productId": 1,
  "quantity": 2
}
```

### Order Endpoints

Base path:

```text
/api/v1/orders
```

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/v1/orders/checkout?userId={userId}&shippingAddressId={addressId}` | Checkout cart |
| GET | `/api/v1/orders/{id}` | Get order by ID |
| GET | `/api/v1/orders/number/{orderNumber}` | Get order by order number |
| GET | `/api/v1/orders/user/{userId}` | List orders for user |
| GET | `/api/v1/orders/status/{status}` | List orders by status |
| PATCH | `/api/v1/orders/{id}/status?status={status}` | Change order status |
| PATCH | `/api/v1/orders/{id}/cancel` | Cancel pending/confirmed order |

Status transition rules:

| Current status | Allowed normal transition | Dedicated cancel allowed |
| --- | --- | --- |
| PENDING | CONFIRMED | Yes |
| CONFIRMED | SHIPPED | Yes |
| SHIPPED | DELIVERED | No |
| DELIVERED | None | No |
| CANCELLED | None | No |

### Payment Endpoints

There is currently no public payment API. `PaymentController` exists as an empty
placeholder, and `PaymentService` is called internally by checkout.

## Quick Start with cURL

Create a category:

```bash
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics","description":"Phones and accessories"}'
```

Create a product:

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Samsung Galaxy M14","description":"5G Android phone","price":12999.00,"stockQty":25,"lowStockThreshold":5,"categoryId":1}'
```

Register a user:

```bash
curl -X POST http://localhost:8080/api/users/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Rahul Sharma","email":"rahul@example.com","password":"rahul123"}'
```

Create an address:

```bash
curl -X POST http://localhost:8080/api/address/1 \
  -H "Content-Type: application/json" \
  -d '{"street":"221 MG Road","city":"Bengaluru","state":"Karnataka","zipCode":"560001","country":"India"}'
```

Add an item to cart:

```bash
curl -X POST "http://localhost:8080/api/v1/cart/items?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":1}'
```

Checkout:

```bash
curl -X POST "http://localhost:8080/api/v1/orders/checkout?userId=1&shippingAddressId=1"
```

Current checkout warning: this will currently produce a failed payment and a
cancelled order because checkout is hard-coded to simulate payment failure.

## Error Handling

Current exception mapping:

| Exception | HTTP status |
| --- | --- |
| `ResourceNotFoundException` | 404 Not Found |
| `DuplicateResourceException` | 409 Conflict |
| `InsufficientStockException` | 409 Conflict |
| `InvalidOrderStateException` | 409 Conflict |
| `IllegalArgumentException` | 400 Bad Request |
| Other exceptions | 500 Internal Server Error |

Error responses are currently plain text. A structured JSON error response is a
recommended next improvement.

## Roadmap

Implemented:

- Category/product/user/address/cart/order modules.
- Product stock checks and soft deletion.
- Checkout-to-order conversion.
- Order item and shipping-address snapshots.
- Simulated payment records.
- Cancellation with stock restoration.
- Central exception handler.

Planned:

- JWT authentication and role-based authorization.
- BCrypt password hashing.
- Replace direct `userId` trust with authenticated user identity.
- Successful and failed payment flows controlled by a request/gateway result.
- Payment retry, refund, webhook, and idempotency support.
- User/address read endpoints.
- Low-stock API or notifications.
- Pagination, filtering, and sorting.
- Structured API response and error schema.
- Database migrations with Flyway or Liquibase.
- Docker/Docker Compose support.
- Unit, integration, and end-to-end API tests.
- CI pipeline and deployment configuration.

## Known Limitations

- No authentication or authorization is currently enforced.
- Passwords are stored in plaintext.
- Payment is simulated and not exposed as a public API.
- Checkout currently follows the failed-payment path.
- Seed data runs on every startup and can overwrite local demo state.
- Business-flow automated tests are missing.
- Some routes use `/api/v1`, while users and addresses use non-versioned paths.
- Order reads may need transaction/fetch adjustments because order items are lazy.

## Author

Rajveer

## License

No license file is currently present in this repository.
