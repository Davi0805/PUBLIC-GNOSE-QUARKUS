# PUBLIC-GNOSE-QUARKUS - User Microservice for ERP

## Overview

**PUBLIC-GNOSE-QUARKUS** is a prototype of a **User Microservice** for an **ERP application** with the objective of handling many companies at once. It was developed with **Java**, **Quarkus**, **Docker**, **Redis**, **PostgreSQL**, and **JWT** for authentication. The microservice is designed to manage user data, handle authentication, and interact with other ERP components.

## Architecture and Patterns

The project follows a **microservices architecture**, promoting separation of concerns, scalability, and maintainability:

- **Authentication** – JWT-based authentication for secure, stateless communication.
- **Core** – Main logic of the User service, handling user-related operations.
- **Database** – PostgreSQL for persisting user information.
- **Caching** – Redis used for caching user data and JWT tokens to improve performance.
- **Dockerization** – The project is fully containerized using Docker for consistent deployment environments.

### Design Patterns Used

- **Microservices** – Each service (like user management) is a standalone module that can scale independently.
- **JWT Authentication** – Stateless user authentication using JWT tokens.
- **Caching** – Redis is used to cache frequently accessed user data and JWT tokens to reduce database load.
- **Service-Repository Pattern** – The **Service** layer contains the business logic and communicates with the **Repository** layer for data access. The **Repository** abstracts database queries, while the **Service** handles higher-level operations, allowing easy modifications and testing.
- **Singleton Pattern** – Ensures only one instance of the authentication service runs within the system.
