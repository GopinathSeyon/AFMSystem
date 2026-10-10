# AFMSystem – Agriculture Farm Management System

Spring Boot 3.5.5 · Java 21 · MySQL · Spring Security · JWT (+ rotating refresh tokens)

## Run
1. Create the database (optional, the URL also has `createDatabaseIfNotExist=true`):
   `CREATE DATABASE agriculture_farm_db;`
2. Set environment variables:
   `DB_PASSWORD`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` (min 8 chars), optionally `JWT_SECRET` (Base64, >= 256 bits)
3. `mvn clean spring-boot:run`  → http://localhost:8080
   Swagger UI: http://localhost:8080/swagger-ui.html (click **Authorize**, paste only the token)

Production: `mvn spring-boot:run -Dspring-boot.run.profiles=prod`
(requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`; Swagger is disabled).

## Test
`mvn test` (in-memory H2, no MySQL needed): JwtServiceTest, ProfitServiceTest, FarmApiIntegrationTest.

## Flow
register/login → farm → field → crop → activities/expenses → harvest → reports
(`/api/expenses/report`, `/api/harvests/report`, `/api/reports/profit`).

Packages (`com.agriculture.farm`): auth, config, controller, service, repository, entity, dto, mapper, exception, util.
