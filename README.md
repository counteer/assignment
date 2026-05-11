## **Design Decisions**

Adopted **API-First Approach** by creating an OpenAPI specification to generate the API contract (Controllers and Models). Enforced
**strict separation between the layers** (Web, Business and Data). Since there was no mention in the assignment about the pricing rules, 
introduced a **PricingService interface**, and a Default implementation, so this can be implemented later easily regarding to the exact 
specifications of the client. **Introduced version-controlled database migration**, guaranteeing safe database deployments instead 
of the automatic Hibernate generation. Utilized **Apache Kafka** to handle bank transfer updates asynchronously. Configured Spring's 
**@Scheduled annotation** to implement the daily task to cancel overdue unpaid reservations.

Applied **defensive programming**, business rules are validated before any database logic occurs. Implemented a **global exception handler** 
to generalize the error messages, and to exempt the other layers of handling HTTP error cases. Developed fast running **unit tests with mocked 
dependencies**, alongside **integration tests** with 'real' connections to database and message queue. Added a basic **slf4j logger**, to handle 
application error and info events.

## **Planned (or possible) enhancements**
- Implement **database auditing** with historical tables or Hibernate Envers, to maintain a historical log of all reservation state changes.
- **Event-Driven contracts**, like AsyncApi for the Kafka payloads.
- Integrate **Spring Boot Actuator** to show health endpoints.
- Introduce more **Spring Profiles** (local, dev, prod) to separate environment specific configurations. 
- Implement **Containerization**, creating a multi-stage Dockerfile for deployment, and a docker-compose.yml to bootstrap the local developer 
environment with dependencies (Kafka broker, PostgreSQL).
- Utilize **boilerplate reduction** using Lombok, cleaning the code of manual getters, setters and logger instantiations.
- Expanding the **Event-driven architecture** by sending outbound messages via Kafka regarding cancelled bookings, successful bookings and detected 
overpayments.
- Introducing **extended validations** for reservations, such as handling bookings in the past or double-booking the same room for the same dates.
- Introducing **Identity and Access Management** to enable role-based authorizations.
