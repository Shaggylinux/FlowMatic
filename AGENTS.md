# Agent Guidelines for Gestion Repository

This document provides essential guidance for agentic coding agents working in the Gestion repository.

## Project Overview

- **Type**: Spring Boot Backend Application
- **Language**: Java 21
- **Build Tool**: Maven
- **Framework**: Spring Boot 4.0.4
- **Database**: PostgreSQL with Spring Data JPA
- **Key Features**: User registration, authentication (Spring Security), Thymeleaf templates

## Build & Test Commands

### Build Commands
```bash
# Clean and build the project
mvn clean install

# Build without running tests
mvn clean package -DskipTests

# Build and run the application
mvn spring-boot:run

# Compile only
mvn compile
```

### Test Commands
```bash
# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=BackApplicationTests

# Run a specific test method
mvn test -Dtest=BackApplicationTests#contextLoads

# Run tests with verbose output
mvn test -X
```

### Code Quality
```bash
# Check for compilation errors
mvn clean compile

# Verify project structure
mvn verify
```

## Code Style Guidelines

### Imports
- Group imports in the following order:
  1. Standard Java libraries (`java.*`)
  2. Jakarta/JEE packages (`jakarta.*`)
  3. Spring Framework (`org.springframework.*`)
  4. Third-party libraries (Lombok, validators, etc.)
  5. Project-specific imports (`com.back.*`)
- Use explicit imports; avoid wildcard imports
- Remove unused imports

### Naming Conventions
- **Classes**: PascalCase (e.g., `UsuarioService`, `RegistroController`)
- **Methods**: camelCase (e.g., `registrarUsuario`, `procesarRegistro`)
- **Variables**: camelCase (e.g., `usuarioRepository`, `encoder`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MIN_PASSWORD_LENGTH`)
- **Package**: lowercase, hierarchical (e.g., `com.back.service`, `com.back.model`)

### Type Annotations & Formatting
- Use Jakarta EE annotations (`jakarta.*`), not `javax.*`
- Lombok annotations: Use `@Data` for automatic getters/setters/equals/hashCode/toString
- Spring annotations: `@Service`, `@Controller`, `@Entity`, `@Repository` for component scanning
- **Tab width**: 4 spaces for indentation
- **Brace style**: Opening brace on same line (Java convention)
- **Line length**: Aim for 100-120 characters

### Entity & Validation Pattern
```java
@Data
@Entity
@Table(name = "table_name")
public class EntityName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Field is required")
    @Column(nullable = false)
    private String fieldName;
}
```

### Service Layer Pattern
- Use `@Service` annotation for service classes
- Inject repositories via `@Autowired`
- Return meaningful status strings or custom response objects
- Handle business logic exceptions appropriately

### Controller Layer Pattern
- Use `@Controller` for web request handlers
- Use `@RequestMapping` for class-level path prefixes
- Use `@GetMapping`, `@PostMapping` for specific HTTP methods
- Validate input with `@Valid` and `BindingResult`
- Return view names or redirects with `redirect:` prefix

### Error Handling
- Validate input using Jakarta validation annotations (`@NotBlank`, `@Email`, `@Size`)
- Check binding results in controllers before processing
- Return meaningful error messages to users
- Log exceptions appropriately (add logging as needed)
- Use exception handling patterns (try-catch for external operations)

### Password & Security
- Use `BCryptPasswordEncoder` for password encryption
- Never store plaintext passwords
- Always encode before persisting to database
- Validate password constraints before processing

### Database Conventions
- Use PostgreSQL-specific constraints
- Set `nullable = false` for required fields
- Use `unique = true` for fields like email
- Use Long for entity IDs (auto-generated)
- Table names in lowercase (e.g., `usuarios`)

### Dependency Injection
- Prefer `@Autowired` constructor injection over field injection when possible
- Use Spring's component scanning via `@Service`, `@Repository`, `@Controller`

### Java 21 Features
- Use records for immutable data transfer objects where appropriate
- Use modern stream APIs for collections
- Use var for local variables when type is obvious

## Key Dependencies
- Spring Boot Starter Web (REST/MVC)
- Spring Data JPA (database access)
- Spring Security (authentication)
- Thymeleaf (template engine)
- Lombok (boilerplate reduction)
- PostgreSQL Driver
- Jakarta Persistence & Validation

## Project Structure
```
src/main/java/com/back/
  ├── BackApplication.java (main entry point)
  ├── controller/ (HTTP request handlers)
  ├── service/ (business logic)
  ├── model/ (JPA entities)
  └── repository/ (data access layer)
src/test/java/ (unit tests)
```

## Testing Guidelines
- Use JUnit 5 (Jupiter)
- Use `@SpringBootTest` for integration tests
- Test services separately from controllers when possible
- Use meaningful test method names
