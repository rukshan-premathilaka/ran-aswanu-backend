# 🚀 Spring Boot REST API

A production-ready RESTful API built with **Spring Boot**, following clean architecture principles with a well-organized layered package structure.

---

## 📋 Table of Contents

- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Architecture Overview](#-architecture-overview)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [API Endpoints](#-api-endpoints)
- [Error Handling](#-error-handling)
- [Running Tests](#-running-tests)
- [Contributing](#-contributing)

---

## 🛠 Tech Stack

| Technology        | Purpose                          |
|-------------------|----------------------------------|
| Java 17+          | Programming language             |
| Spring Boot 3.x   | Application framework            |
| Spring Data JPA   | Database access (ORM)            |
| Spring Security   | Authentication & Authorization   |
| MySQL / PostgreSQL| Relational database              |
| Lombok            | Reduce boilerplate code          |
| MapStruct         | Entity ↔ DTO mapping             |
| Maven / Gradle    | Build tool                       |
| Swagger / OpenAPI | API documentation                |

---

## 📁 Project Structure

```
com.yourcompany.projectname/
│
├── ProjectNameApplication.java        ← Main entry point
│
├── config/                            ← Configuration classes
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── SwaggerConfig.java
│
├── controller/                        ← REST controllers (HTTP layer)
│   ├── UserController.java
│   └── ProductController.java
│
├── service/                           ← Business logic layer
│   ├── UserService.java               ← Interface
│   ├── ProductService.java
│   └── impl/                         ← Implementations
│       ├── UserServiceImpl.java
│       └── ProductServiceImpl.java
│
├── repository/                        ← Database access layer (JPA)
│   ├── UserRepository.java
│   └── ProductRepository.java
│
├── model/                             ← Entity classes (DB tables)
│   ├── User.java
│   └── Product.java
│
├── dto/                               ← Data Transfer Objects
│   ├── request/
│   │   ├── CreateUserRequest.java
│   │   └── UpdateUserRequest.java
│   └── response/
│       ├── UserResponse.java
│       └── ApiResponse.java
│
├── exception/                         ← Custom exceptions & global handler
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── GlobalExceptionHandler.java
│
├── mapper/                            ← Entity ↔ DTO conversion
│   └── UserMapper.java
│
├── security/                          ← Auth / JWT logic
│   ├── JwtTokenProvider.java
│   └── JwtAuthenticationFilter.java
│
└── util/                              ← Utility / helper classes
    └── DateUtils.java
```

---

## 🏗 Architecture Overview

This project follows a **3-layer architecture**:

```
[ Client / Postman ]
        │
        ▼
┌─────────────────┐
│   Controller    │  ← Receives HTTP requests, validates input
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Service      │  ← Business logic, rules, calculations
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Repository    │  ← Database queries via Spring Data JPA
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Database     │  ← MySQL / PostgreSQL
└─────────────────┘
```

> **Rule:** Controllers never talk directly to repositories. All data flows through the service layer.

---

## ⚡ Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+ or Gradle 7+
- MySQL or PostgreSQL running locally

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/your-project-name.git
cd your-project-name
```

### 2. Configure the Database

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_db_name
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 3. Build the Project

```bash
# Maven
mvn clean install

# Gradle
./gradlew build
```

### 4. Run the Application

```bash
# Maven
mvn spring-boot:run

# Gradle
./gradlew bootRun
```

The server will start at: **`http://localhost:8080`**

---

## 🔐 Environment Variables

It is recommended to use environment variables or a `.env` file for sensitive config in production.

| Variable              | Description                   | Example                    |
|-----------------------|-------------------------------|----------------------------|
| `DB_URL`              | Database connection URL       | `jdbc:mysql://localhost/db`|
| `DB_USERNAME`         | Database username             | `root`                     |
| `DB_PASSWORD`         | Database password             | `secret`                   |
| `JWT_SECRET`          | Secret key for JWT signing    | `mySecretKey123`           |
| `JWT_EXPIRATION_MS`   | JWT expiry in milliseconds    | `86400000`                 |

---

## 📡 API Endpoints

> Full interactive documentation available at: `http://localhost:8080/swagger-ui.html`

### 👤 User Endpoints

| Method | Endpoint              | Description          | Auth Required |
|--------|-----------------------|----------------------|---------------|
| GET    | `/api/v1/users`       | Get all users        | ✅ Yes        |
| GET    | `/api/v1/users/{id}`  | Get user by ID       | ✅ Yes        |
| POST   | `/api/v1/users`       | Create a new user    | ✅ Yes        |
| PUT    | `/api/v1/users/{id}`  | Update a user        | ✅ Yes        |
| DELETE | `/api/v1/users/{id}`  | Delete a user        | ✅ Yes        |

### 🔑 Auth Endpoints

| Method | Endpoint              | Description          | Auth Required |
|--------|-----------------------|----------------------|---------------|
| POST   | `/api/v1/auth/login`  | Login & get JWT      | ❌ No         |
| POST   | `/api/v1/auth/register` | Register new user  | ❌ No         |

### Example Request — Create User

```json
POST /api/v1/users
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securepassword"
}
```

### Example Response

```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

## ⚠️ Error Handling

All errors are handled globally via `GlobalExceptionHandler` using `@ControllerAdvice`.

### Standard Error Response Format

```json
{
  "success": false,
  "status": 404,
  "message": "User not found with id: 5",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Common HTTP Status Codes

| Status Code | Meaning                          |
|-------------|----------------------------------|
| `200`       | OK — Request successful          |
| `201`       | Created — Resource created       |
| `400`       | Bad Request — Invalid input      |
| `401`       | Unauthorized — Token missing     |
| `403`       | Forbidden — No permission        |
| `404`       | Not Found — Resource not found   |
| `500`       | Internal Server Error            |

---

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=UserServiceTest

# Generate test coverage report (JaCoCo)
mvn verify
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a new branch: `git checkout -b feature/your-feature-name`
3. Make your changes and commit: `git commit -m "Add your feature"`
4. Push to your branch: `git push origin feature/your-feature-name`
5. Open a **Pull Request**

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

> Built with ❤️ using Spring Boot

---

## Create database

```bash
# Quarry
USE master;
ALTER DATABASE runaswanna SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE runaswanna;
CREATE DATABASE runaswanna;
```