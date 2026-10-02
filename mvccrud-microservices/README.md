# MVC CRUD Microservices (Spring Boot + MySQL)

A mini bookstore project built with a **microservices architecture**. Three CRUD services sit behind a single API Gateway, and all of them use the same MySQL database (`mvccrud`).

## Architecture

```
                         ┌──────────────────────┐
   Postman / Client ───► │  api-gateway :8080   │
                         └─────────┬────────────┘
        /api/books/**              │              /api/orders/**
   ┌───────────────────────────────┼────────────────────────────┐
   ▼                               ▼ /api/users/**              ▼
┌──────────────┐          ┌──────────────┐            ┌──────────────┐
│ book-service │          │ user-service │ ◄───────── │ order-service│
│    :8081     │ ◄─────── │    :8082     │  validates │    :8083     │
└──────┬───────┘  REST    └──────┬───────┘   user &   └──────┬───────┘
       │                         │           book           │
       └─────────────────────────┴──────────────────────────┘
                         MySQL  (mvccrud)
                 tables: books · users · orders
```

| Module          | Port | Responsibility                                                    |
|-----------------|------|-------------------------------------------------------------------|
| `api-gateway`   | 8080 | Spring Cloud Gateway - single entry point, routes by URL path     |
| `book-service`  | 8081 | CRUD for books                                                    |
| `user-service`  | 8082 | CRUD for users                                                    |
| `order-service` | 8083 | CRUD for orders; checks the user and book exist and computes total |

## Tech stack

- Java 17, Maven
- Spring Boot 3.2.5 (Web, Data JPA, Validation)
- Spring Cloud Gateway 2023.0.1
- MySQL 8 + Hibernate
- Lombok

## Prerequisites

- JDK 17+
- Maven 3.8+
- MySQL running on `localhost:3306` with user `root` / password `system`
- Lombok plugin enabled in your IDE (IntelliJ / Eclipse / STS)

## Database setup

Create the database once (tables are created automatically by Hibernate):

```sql
CREATE DATABASE IF NOT EXISTS mvccrud;
```

Each service uses this configuration (`src/main/resources/application.properties`):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mvccrud
spring.datasource.username=root
spring.datasource.password=system
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

## Project structure

```
mvccrud-microservices/
├── pom.xml                  # parent / aggregator
├── .gitignore
├── README.md
├── postman/
│   └── MVC-CRUD-Microservices.postman_collection.json
├── book-service/            # :8081
├── user-service/            # :8082
├── order-service/           # :8083
└── api-gateway/             # :8080
```

Each CRUD service follows the same layers: `model → repository → service → controller`, plus a `GlobalExceptionHandler` that returns clean JSON errors (400 validation, 404 not found, 409 duplicate, 503 downstream service down).

## Build

From the project root:

```bash
mvn clean install -DskipTests
```

## Run

Start the services in this order (each in its own terminal), from the project root:

```bash
mvn -pl book-service  spring-boot:run     # 8081
mvn -pl user-service  spring-boot:run     # 8082
mvn -pl order-service spring-boot:run     # 8083
mvn -pl api-gateway   spring-boot:run     # 8080
```

Or run the jars:

```bash
java -jar book-service/target/book-service-1.0.0.jar
java -jar user-service/target/user-service-1.0.0.jar
java -jar order-service/target/order-service-1.0.0.jar
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

## API endpoints

Use the gateway (`http://localhost:8080`) for everything.

### Books - `/api/books`

| Method | URL               | Description     |
|--------|-------------------|-----------------|
| POST   | `/api/books`      | Create a book   |
| GET    | `/api/books`      | List all books  |
| GET    | `/api/books/{id}` | Get one book    |
| PUT    | `/api/books/{id}` | Update a book   |
| DELETE | `/api/books/{id}` | Delete a book   |

```json
{ "title": "Clean Code", "author": "Robert C. Martin", "isbn": "9780132350884", "price": 499.00, "stock": 25 }
```

### Users - `/api/users`

| Method | URL               | Description     |
|--------|-------------------|-----------------|
| POST   | `/api/users`      | Create a user   |
| GET    | `/api/users`      | List all users  |
| GET    | `/api/users/{id}` | Get one user    |
| PUT    | `/api/users/{id}` | Update a user   |
| DELETE | `/api/users/{id}` | Delete a user   |

```json
{ "name": "Ravi Kumar", "email": "ravi@example.com", "phone": "9876543210", "address": "Hyderabad, Telangana" }
```

### Orders - `/api/orders`

| Method | URL                       | Description                |
|--------|---------------------------|----------------------------|
| POST   | `/api/orders`             | Place an order             |
| GET    | `/api/orders`             | List all orders            |
| GET    | `/api/orders/{id}`        | Get one order              |
| GET    | `/api/orders/user/{userId}` | List a user's orders     |
| PUT    | `/api/orders/{id}`        | Update an order            |
| DELETE | `/api/orders/{id}`        | Delete an order            |

```json
{ "userId": 1, "bookId": 1, "quantity": 2 }
```

`status` is optional (`PLACED` by default; also `SHIPPED`, `DELIVERED`, `CANCELLED`). `totalPrice` is calculated by the service as `book.price × quantity`. Creating an order for a user or book that doesn't exist returns **404**.

## Testing with Postman

1. Open Postman → **Import** → select `postman/MVC-CRUD-Microservices.postman_collection.json`.
2. The collection variable `baseUrl` is `http://localhost:8080` (the gateway).
3. Run in this order: **Create User → Create Book → Create Order**, then the rest.

The examples use id `1`; change the ids in the URLs if your records got different ones.

## Quick curl check

```bash
curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" \
  -d '{"name":"Ravi Kumar","email":"ravi@example.com"}'

curl -X POST http://localhost:8080/api/books -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","price":499,"stock":25}'

curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" \
  -d '{"userId":1,"bookId":1,"quantity":2}'
```

## Notes

- All services share one database for simplicity, as requested. In production, each microservice normally owns its own schema.
- `order-service` calls `book-service` and `user-service` directly using `RestTemplate` (URLs are in its `application.properties`). It does not use a database foreign key across services.
- Change the DB credentials in each service's `application.properties` if yours differ.
