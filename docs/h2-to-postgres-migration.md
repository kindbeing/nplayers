# H2 to PostgreSQL Migration Guide

## Overview
Migrate the Player Service from H2 in-memory database to PostgreSQL for production readiness. This involves updating dependencies, configuration, schema, and adding proper database migration tools.

## Current H2 Setup Analysis

### Issues with Current H2 Implementation
- **In-memory storage**: Data lost on application restart
- **Single connection**: Not suitable for concurrent users
- **File-based CSV import**: Brittle data loading mechanism
- **Development-only**: Not production-ready
- **Limited SQL features**: Missing advanced PostgreSQL features

## Migration Plan

### Files to Add

#### 1. `player-service-backend/src/main/resources/application-postgres.yml`
```yaml
spring:
  datasource:
    driverClassName: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/playerdb
    username: ${DB_USERNAME:playeruser}
    password: ${DB_PASSWORD:playerpass}
  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway for schema management
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          lob:
            non_contextual_creation: true
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

server:
  port: 8080
```

#### 2. `player-service-backend/src/main/resources/application.yml` (update for profiles)
```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:h2}  # Default to h2 for development

---
spring:
  config:
    activate:
      on-profile: h2
  datasource:
    driverClassName: org.h2.Driver
    username: sa
    password:
    url: jdbc:h2:mem:playerdb
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  h2:
    console:
      enabled: true

---
spring:
  config:
    activate:
      on-profile: postgres
  datasource:
    driverClassName: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/playerdb
    username: ${DB_USERNAME:playeruser}
    password: ${DB_PASSWORD:playerpass}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

#### 3. `player-service-backend/src/main/resources/db/migration/V1__Initial_schema.sql`
```sql
-- Create players table
CREATE TABLE IF NOT EXISTS players (
    playerid VARCHAR(255) PRIMARY KEY,
    birthyear VARCHAR(4),
    birthmonth VARCHAR(2),
    birthday VARCHAR(2),
    birthcountry VARCHAR(255),
    birthstate VARCHAR(255),
    birthcity VARCHAR(255),
    deathyear VARCHAR(4),
    deathmonth VARCHAR(2),
    deathday VARCHAR(2),
    deathcountry VARCHAR(255),
    deathstate VARCHAR(255),
    deathcity VARCHAR(255),
    namefirst VARCHAR(255),
    namelast VARCHAR(255),
    namegiven VARCHAR(255),
    weight VARCHAR(3),
    height VARCHAR(3),
    bats VARCHAR(1),
    throws VARCHAR(1),
    debut DATE,
    finalgame DATE,
    retroId VARCHAR(255),
    bbrefId VARCHAR(255)
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
   userid VARCHAR(36) PRIMARY KEY,
   email VARCHAR(50) UNIQUE,
   fullname VARCHAR(255) NOT NULL,
   address VARCHAR(500),
   age INT
);
```

#### 4. `player-service-backend/src/main/resources/db/migration/V2__Load_player_data.sql`
```sql
-- Load player data from CSV
-- Note: This assumes Player.csv is available in the classpath
-- In production, consider using a proper ETL process

-- For development/demo purposes, you can load from CSV if available
-- COPY players FROM '/path/to/Player.csv' WITH CSV HEADER;

-- Alternatively, insert sample data for testing
INSERT INTO players (playerid, namefirst, namelast, birthcountry)
VALUES
    ('player001', 'John', 'Doe', 'USA'),
    ('player002', 'Jane', 'Smith', 'CAN')
ON CONFLICT (playerid) DO NOTHING;
```

#### 5. `docker-compose.yml` (at project root)
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: playerdb
      POSTGRES_USER: playeruser
      POSTGRES_PASSWORD: playerpass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./player-service-backend/src/main/resources/db/migration:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U playeruser -d playerdb"]
      interval: 10s
      timeout: 5s
      retries: 5

  pgadmin:
    image: dpage/pgadmin4
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@player.com
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "8081:80"
    depends_on:
      postgres:
        condition: service_healthy

volumes:
  postgres_data:
```

#### 6. `player-service-backend/src/test/resources/application-test-postgres.yml`
```yaml
spring:
  datasource:
    driverClassName: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/testdb
    username: testuser
    password: testpass
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: false  # Disable for tests
```

#### 7. `scripts/setup-postgres.sh`
```bash
#!/bin/bash
# Setup script for PostgreSQL migration

echo "Setting up PostgreSQL database..."

# Start PostgreSQL container
docker-compose up -d postgres

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to start..."
sleep 10

# Run Flyway migrations
cd player-service-backend
mvn flyway:migrate -Dspring.profiles.active=postgres

echo "PostgreSQL setup complete!"
echo "Database: playerdb"
echo "Username: playeruser"
echo "Password: playerpass"
echo "Port: 5432"
```

### Files to Update

#### 1. `player-service-backend/pom.xml`
```xml
<!-- Remove H2 dependency -->
<!-- <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency> -->

<!-- Add PostgreSQL driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Add Flyway for database migrations -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

<!-- Add Flyway Maven plugin -->
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>9.22.3</version>
    <configuration>
        <url>jdbc:postgresql://localhost:5432/playerdb</url>
        <user>playeruser</user>
        <password>playerpass</password>
        <locations>
            <location>filesystem:src/main/resources/db/migration</location>
        </locations>
    </configuration>
</plugin>
```

#### 2. `player-service-backend/src/main/resources/schema.sql` (deprecate/remove)
```sql
-- This file is deprecated in favor of Flyway migrations
-- Remove or keep for H2 development only

-- For production PostgreSQL, use:
-- src/main/resources/db/migration/V1__Initial_schema.sql
```

#### 3. `player-service-backend/src/test/java/com/app/playerservicejava/controller/UserControllerTest.java`
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-h2")  // Use H2 for unit tests, PostgreSQL for integration tests
public class UserControllerTest {
    // Keep existing tests but add profile-specific configuration
}
```

### Files to Remove (Optional)

#### 1. `player-service-backend/src/main/resources/schema.sql` (after migration)
- Move contents to Flyway migration files
- Keep for H2 development if needed

## Migration Steps

### Phase 1: Development Environment Setup

1. **Install Docker and Docker Compose**
   ```bash
   # Ensure Docker is running
   docker --version
   docker-compose --version
   ```

2. **Update Dependencies**
   ```bash
   cd player-service-backend
   mvn clean install
   ```

3. **Start PostgreSQL**
   ```bash
   docker-compose up -d postgres
   ```

4. **Run Initial Migration**
   ```bash
   mvn flyway:migrate -Dspring.profiles.active=postgres
   ```

5. **Test Application**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=postgres
   ```

### Phase 2: Code Migration

1. **Update Configuration Files**
   - Add profile-based configuration
   - Update application.yml with PostgreSQL settings

2. **Migrate Schema**
   - Convert H2-specific SQL to PostgreSQL-compatible SQL
   - Create Flyway migration files
   - Handle CSV data loading properly

3. **Update Tests**
   - Add PostgreSQL-specific test configurations
   - Create integration tests with real PostgreSQL
   - Keep H2 for fast unit tests

### Phase 3: Data Migration

1. **Export H2 Data** (if needed)
   ```sql
   -- Connect to H2 console and export data
   SCRIPT TO 'backup.sql';
   ```

2. **Import to PostgreSQL**
   ```bash
   # Use pg_restore or custom import script
   psql -h localhost -U playeruser -d playerdb -f backup.sql
   ```

3. **Validate Data Integrity**
   - Compare row counts
   - Verify key data points
   - Test application functionality

### Phase 4: Production Deployment

1. **Environment Variables**
   ```bash
   export DB_USERNAME=prod_user
   export DB_PASSWORD=secure_password
   export SPRING_PROFILES_ACTIVE=postgres
   ```

2. **Update Deployment Scripts**
   - Add PostgreSQL to infrastructure
   - Update Docker configurations
   - Configure connection pooling

3. **Monitoring and Backup**
   - Set up database monitoring
   - Configure automated backups
   - Add health checks

## Testing Strategy

### Unit Tests (H2)
- Keep using H2 for fast, isolated unit tests
- Test business logic without database concerns

### Integration Tests (PostgreSQL)
```java
@SpringBootTest
@ActiveProfiles("test-postgres")
@Testcontainers
public class PlayerRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // Integration tests here
}
```

### Performance Tests
- Compare query performance between H2 and PostgreSQL
- Test concurrent user scenarios
- Validate pagination performance

## Rollback Plan

1. **Keep H2 Profile Active**
   - H2 remains default profile
   - Easy rollback by changing `spring.profiles.active`

2. **Database Backup**
   ```bash
   # PostgreSQL backup
   pg_dump playerdb > backup.sql

   # H2 backup (if needed)
   # Use H2 console to export
   ```

3. **Configuration Rollback**
   - Switch back to H2 profile
   - Revert application.yml changes

## Benefits of PostgreSQL Migration

- **Production Ready**: ACID compliance, concurrent access, persistence
- **Scalability**: Handle multiple connections and large datasets
- **Advanced Features**: JSON support, advanced indexing, stored procedures
- **Ecosystem**: Better tooling, monitoring, and community support
- **Data Integrity**: Foreign keys, constraints, transactions
- **Backup/Recovery**: Mature backup and replication solutions

## Common Issues and Solutions

### Connection Issues
```yaml
# Add to application.yml
spring:
  datasource:
    hikari:
      connection-timeout: 60000
      maximum-pool-size: 10
```

### Migration Conflicts
- Use `flyway.baseline-on-migrate=true` for existing databases
- Manually baseline existing schema

### Data Type Differences
- VARCHAR vs TEXT in PostgreSQL
- Date handling differences
- Case sensitivity in string comparisons

### Performance Considerations
- Add appropriate indexes for query patterns
- Configure connection pooling
- Monitor slow queries
