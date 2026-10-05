# OrderFlow API

Java 21 | Spring Boot 3.4.1 | Spring Security + JWT | MySQL | Kafka / Event-Driven

OrderFlow is an enterprise-grade backend REST API for an e-commerce order management workflow. It manages categories, products, user authentication with JWT, address management, cart operations, order checkout, stock updates, simulated payments, and event-driven order notifications using a clean, layered Spring Boot architecture.

---

## Project Status

### Implemented & Functional Core
- **JWT Authentication & Security**: Stateless JWT-based authentication using Spring Security (`/api/v1/auth/signup`, `/api/v1/auth/login`). Passwords are secured using `BCryptPasswordEncoder`.
- **User & Address Management**: User profile update/deletion and address CRUD operations tied to authenticated user contexts.
- **Inventory Service**: Category management and product CRUD with SKU generation, low-stock tracking, category assignment, and soft deletion (`active=false`).
- **Cart Service**: Persistent cart per user with item addition, quantity updates, item removal, and total recalculations.
- **Order Service & Checkout**: Full checkout pipeline snapshotting shipping addresses and item prices. Automatically decrements inventory stock upon order creation and restores stock upon cancellation.
- **Payment Simulation**: Internal payment processing setting order status to `CONFIRMED` upon successful simulated payment.
- **Event-Driven Architecture**: Integrated `OrderEventProducer` publishing `OrderStatusChangedEvent` payloads to Kafka for decoupled notification processing.
- **Notification Service**: Separate consumer module (`Notification-service`) to consume order status events and handle notification dispatch.
- **Centralized Exception Handling**: `GlobalExceptionHandler` mapping domain exceptions (`ResourceNotFoundException`, `InsufficientStockException`, etc.) to HTTP status codes.

---

## Tech Stack

| Layer | Technology |
| --- | --- |
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.4.1 |
| **Security** | Spring Security 6 with JWT (JSON Web Tokens) & BCrypt |
| **Messaging / Events** | Apache Kafka |
| **ORM / Database** | Spring Data JPA / Hibernate & MySQL |
| **Documentation** | Swagger / OpenAPI 3.0 UI (`springdoc-openapi-starter-webmvc-ui`) |
| **Utilities** | Lombok |
| **Build Tool** | Maven (`mvnw` / `mvnw.cmd`) |

---

## Architecture

### Request Flow with JWT Authentication

```text
Client Request (Bearer JWT Header)
    │
    ▼
JwtAuthenticationFilter ──► SecurityContextHolder (CustomUserDetails)
    │
    ▼
Controller Layer (@AuthenticationPrincipal)
    │
    ▼
Service Layer (Business Logic & Transactions)
    ├──► Repositories (Spring Data JPA) ──► MySQL Database
    └──► OrderEventProducer ────────────► Kafka Topic ──► Notification Service
```

### Directory Structure

```text
OrderFlow/
├── src/main/java/com/example/OrderFlow/
│   ├── OrderFlowApplication.java
│   ├── Common/
│   │   ├── Entity/AuditableBase.java
│   │   └── Exception/GlobalExceptionHandler.java
│   ├── Config/Security/
│   │   ├── SecurityConfig.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtUtils.java
│   │   └── CustomUserDetails.java
│   ├── InventoryService/
│   │   ├── Controller/
│   │   ├── DTO/
│   │   ├── Models/
│   │   ├── Repository/
│   │   └── Service/
│   ├── UserService/
│   │   ├── Controller/
│   │   ├── DTO/
│   │   ├── Model/
│   │   ├── Repository/
│   │   └── Service/
│   ├── OrderService/
│   │   ├── Controller/
│   │   ├── DTO/
│   │   ├── Event/
│   │   ├── Model/
│   │   ├── Repository/
│   │   └── Service/
│   └── PaymentService/
│       ├── Controller/
│       ├── DTO/
│       ├── Model/
│       ├── Repository/
│       └── Service/
├── Notification-service/           # Microservice for consuming order notifications
├── docker-compose.yml
├── PROJECT_HANDOFF.md
└── README.md
```

---

## Security & Authentication

All protected endpoints require an `Authorization` header with a valid JWT Bearer token:

```http
Authorization: Bearer <your_jwt_token>
```

### Authentication Endpoints (Public)
- `POST /api/v1/auth/signup` - Register a new user account.
- `POST /api/v1/auth/login` - Authenticate and receive a JWT access token.

---

## Database Schema & Relationships

```text
Category (1) ──── (N) Product
User (1) ──────── (N) Address
User (1) ──────── (1) Cart (1) ──── (N) CartItem (N) ──── (1) Product
User (1) ──────── (N) Order (1) ─── (N) OrderItem
Order (1) ─────── (1) Payment
```

---

## API Reference

### 1. Authentication Endpoints

| Method | Path | Auth Required | Description |
| --- | --- | --- | --- |
| `POST` | `/api/v1/auth/signup` | No | Register a new user |
| `POST` | `/api/v1/auth/login` | No | Authenticate user and get JWT token |

#### Example Signup Request (`POST /api/v1/auth/signup`):
```json
{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "password": "securepassword123"
}
```

#### Example Login Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "rahul@example.com",
  "name": "Rahul Sharma"
}
```

---

### 2. Category Endpoints

| Method | Path | Auth Required | Description |
| --- | --- | --- | --- |
| `POST` | `/api/v1/categories` | Yes | Create a category |
| `PUT` | `/api/v1/categories/{id}` | Yes | Update a category |
| `GET` | `/api/v1/categories/{id}` | No | Get category details |
| `GET` | `/api/v1/categories` | No | List all categories |
| `DELETE` | `/api/v1/categories/{id}` | Yes | Delete a category (if unreferenced) |

---

### 3. Product Endpoints

| Method | Path | Auth Required | Description |
| --- | --- | --- | --- |
| `POST` | `/api/v1/products` | Yes | Create product |
| `PUT` | `/api/v1/products/{id}` | Yes | Update product details |
| `GET` | `/api/v1/products/{id}` | No | Get product by ID |
| `GET` | `/api/v1/products/sku/{sku}` | No | Get product by SKU |
| `GET` | `/api/v1/products` | No | List all products |
| `GET` | `/api/v1/products/category/{categoryId}` | No | List products by category |
| `DELETE` | `/api/v1/products/{id}` | Yes | Soft delete product (`active=false`) |

---

### 4. User & Address Endpoints

| Method | Path | Auth Required | Description |
| --- | --- | --- | --- |
| `PUT` | `/api/users/update` | Yes | Update current user's profile |
| `DELETE` | `/api/users/delete` | Yes | Delete current user's account |
| `POST` | `/api/address` | Yes | Create shipping address |
| `PUT` | `/api/address/{addressId}` | Yes | Update address |
| `DELETE` | `/api/address/{addressId}` | Yes | Delete address |

---

### 5. Cart Endpoints

| Method | Path | Auth Required | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/cart` | Yes | Get active user cart |
| `POST` | `/api/v1/cart/items` | Yes | Add item to cart |
| `PUT` | `/api/v1/cart/items` | Yes | Update item quantity in cart |
| `DELETE` | `/api/v1/cart/items?productId={id}` | Yes | Remove product from cart |
| `DELETE` | `/api/v1/cart` | Yes | Clear entire cart |

---

### 6. Order & Checkout Endpoints

| Method | Path | Auth Required | Description |
| --- | --- | --- | --- |
| `POST` | `/api/v1/orders/checkout?shippingAddressId={id}` | Yes | Checkout cart into order |
| `GET` | `/api/v1/orders/{id}` | Yes | Get order details by ID |
| `GET` | `/api/v1/orders/number/{orderNumber}` | Yes | Get order details by Order Number |
| `GET` | `/api/v1/orders` | Yes | Get all orders for logged-in user |
| `PATCH` | `/api/v1/orders/{id}/cancel` | Yes | Cancel order & restore stock |

---

## Quick Start & cURL Walkthrough

### 1. Register User & Obtain JWT Token

```bash
# Register User
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Rahul Sharma","email":"rahul@example.com","password":"password123"}'

# Login to get Token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"rahul@example.com","password":"password123"}' | jq -r '.token')
```

### 2. Create Address

```bash
curl -X POST http://localhost:8080/api/address \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"street":"221 MG Road","city":"Bengaluru","state":"Karnataka","zipCode":"560001","country":"India"}'
```

### 3. Add Product to Cart

```bash
curl -X POST http://localhost:8080/api/v1/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'
```

### 4. Checkout Order

```bash
curl -X POST "http://localhost:8080/api/v1/orders/checkout?shippingAddressId=1" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Error Handling

Mapped domain exceptions returned via `GlobalExceptionHandler`:

| Exception | HTTP Status | Response |
| --- | --- | --- |
| `ResourceNotFoundException` | 404 Not Found | Message string |
| `DuplicateResourceException` | 409 Conflict | Message string |
| `InsufficientStockException` | 409 Conflict | Message string |
| `InvalidOrderStateException` | 409 Conflict | Message string |
| `IllegalArgumentException` | 400 Bad Request | Message string |

---

## Development Roadmap & Recommended Enhancements

- [x] JWT Authentication & Password Hashing (BCrypt).
- [x] Cart and Checkout snapshot architecture.
- [x] Event Producer for Kafka notifications.
- [ ] Optimistic / Pessimistic locking on Product inventory to prevent overselling under high concurrency.
- [ ] Role-Based Access Control (`@PreAuthorize("hasRole('ADMIN')")`) for Product and Category CRUD operations.
- [ ] Refactor `Cart.removeItem()` and `Cart.clearItems()` for strict total calculation safety.
- [ ] Global Exception Handler support for `MethodArgumentNotValidException` to return structured 400 validation error responses.

---

## Author

**Rajveer**
