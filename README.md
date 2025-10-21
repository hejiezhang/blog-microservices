# blog-microservices

This demo contains 4 independent Spring Boot projects (backend only):

- user-service
- post-service
- comment-service
- api-gateway (Spring Cloud Gateway)

Each service uses PostgreSQL (DB-per-service). A `docker-compose.yml` is included to bring up 3 Postgres instances.
Run each service in IntelliJ as a normal Spring Boot application (import as existing Maven project). 

Notes:
- The services use OpenFeign for synchronous REST calls (post-service and comment-service call other services as a demo).
- Default Postgres credentials are set in docker-compose; adjust `application.properties` if needed.

