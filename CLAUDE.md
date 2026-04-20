# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**FLOWMATIC** — a candidate recruitment and document management system with role-based access control. Built with Spring Boot (Java 21, Maven).

## Commands

```bash
# Run the application
mvn spring-boot:run

# Build (with tests)
mvn clean install

# Build (skip tests)
mvn clean package -DskipTests

# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=BackApplicationTests
```

## Architecture

**Stack:** Spring Boot 4.0.5 · Spring Security · Spring Data JPA · Thymeleaf · PostgreSQL · Apache POI · Spring Mail

**Layer structure:**
```
src/main/java/com/back/
├── BackApplication.java
├── GenHash.java             — standalone BCrypt hash generator utility
├── controller/              — HTTP/MVC handlers (Login, Registro, RegistroRRHH, Admin, Drive, Password, Home)
├── service/                 — Business logic (UsuarioService, EmailService, FilesServices, ExcelService, CustomUserDetailsService)
├── model/                   — JPA entities: Usuario, Archivos
├── repository/              — Spring Data repos with custom JPQL queries
└── web/
    └── SecurityConfig.java
```

**Templates:** `src/main/resources/templates/` (Thymeleaf `.html` files)  
**Static assets:** `src/main/resources/static/` (CSS, JS, videos)  
**Config:** `src/main/resources/application.properties`

## Database

PostgreSQL on `localhost:5432/gestion`. Schema file: `database.sql`.  
JPA is set to `ddl-auto: update` — it will auto-migrate. Seed data (admin user: `gomez@gmail.com`) is in `database.sql`.

Key entities:
- **Usuario** — users with roles (`CANDIDATO`, `RRHH`, `ADMINISTRADOR`), email-based login, BCrypt password, activation token, and `estado` (recruitment status string set per-candidate via `/drive/actualizar-estado`)
- **Archivos** — file/folder metadata: `propietario` (owner email), `destinario` (recipient email), `etapa` (recruitment stage), `tipoDocumento`, `es_carpeta`

## Security & Authentication

`CustomUserDetailsService` loads users by email and blocks login if `activo = false` (throws `UsernameNotFoundException`, which Spring Security surfaces as "Bad credentials").

Configured in `SecurityConfig.java`:
- Login uses `email` as username parameter and `clave` as password parameter (not Spring defaults)
- Post-login routing via `/post-login` — sends each role to its dashboard
- CSRF is disabled
- The `tokenactivacion` field in `Usuario` is reused for both account activation and password reset flows

**Role access (actual SecurityConfig):**
- `/registro/**`, `/login`, `/error`, `/forgot-password`, `/reset-password`, `/home`, `/`, `/css/**`, `/js/**`, `/videos/**` — public
- `/candidato/**` — `ROLE_CANDIDATO` only
- `/rrhh/**`, `/subir-archivo`, `/crear-carpeta`, `/eliminar`, `/descargar` — `ROLE_RRHH` or `ROLE_CANDIDATO`
- **Everything else (including `/admin/**`, `/drive/**`)** — `anyRequest().authenticated()` only — no role restriction is enforced by Spring Security for these paths

## File Storage

Two distinct upload flows both write to `superfolder/` at the project root:

1. **`FilesServices.guardarArchivoPorEtapa()`** — used by RRHH upload flow, organizes as `superfolder/{etapa}/{owner_email}/filename`
2. **`DriveController`** — general drive UI, stores as `superfolder/{folderDestino}/filename` (flat, folder chosen by user)

File metadata is persisted in the `archivos` table. Max upload size: 30MB.

## Email

Gmail SMTP (`smtp.gmail.com:587`). The app password and base URL (`http://localhost:8080`) are hardcoded in `application.properties`. Both must be updated for production deployment.

`EmailService` sends styled HTML emails for account activation and password reset.

## Non-Obvious Behaviors

- **File visibility:** `ArchivosRepository.buscarArchivosVisiblesPara()` uses `LOWER()` for case-insensitive matching — a user sees files where they are either `propietario` or `destinario`. `DriveController` queries by both `username` and `email` and merges results via a `HashSet` to deduplicate.
- **Stage-based filtering:** RRHH users in `DriveController` see files filtered by folder path; candidates see only files they own or that are shared with them.
- **Token reuse:** generating a new activation/reset token overwrites `tokenactivacion`, invalidating any previous token regardless of purpose.
- **Excel export:** `ExcelService` uses Apache POI to export the user list as XLSX, available at `/admin/exportar`.
- **`RegistroRRHHController`** at `/registro/rrhh` sets `ROLE_ADMIN` (not `ROLE_RRHH`) — likely a bug; verify before relying on it.
- **REST registration endpoint:** `POST /registro/candidato/api` accepts JSON and is used for modal-based registration from the drive UI.
