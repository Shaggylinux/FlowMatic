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

**Packages (actual):** module-based — `auth`, `candidatos` (incl. `candidatos.drive`), `calendario`, `drive`, `notificaciones`, `admin`, `seguridad`, `registro`, `shared` (events only), `web` (presentation layer: `CandidatoHomeController`, `DashboardController`, `AtributosUsuarioAdvice`). `ArchitectureTest` (ArchUnit) and `ModulithTest` enforce isolation: `auth` must not depend on business modules, `shared` only on java/jakarta/lombok/spring/jackson, and `calendario` services must not use `candidatos` (controllers may cross modules).

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
- CSRF is **enabled** with `CookieCsrfTokenRepository.withHttpOnlyFalse()` + `CsrfCookieFilter`; `static/js/csrf.js` injects the token into fetch/XHR/POST forms from the `XSRF-TOKEN` cookie
- The `tokenactivacion` field in `Usuario` is reused for both account activation and password reset flows

**Role access (actual SecurityConfig):**
- `/registro/**`, `/login`, `/error`, `/forgot-password`, `/reset-password`, `/home`, `/`, `/css/**`, `/js/**`, `/videos/**`, `/api/seed`, `/actuator/health` — public
- `/candidato/**` — `ROLE_CANDIDATO` only
- `/calendario/**` — `ROLE_RRHH` or `ROLE_CANDIDATO`
- `/gestion-candidatos/**` — `ROLE_RRHH` or `ROLE_ADMINISTRADOR`
- `/admin/**` — `ROLE_ADMINISTRADOR`
- `/drive/**`, `/rrhh/**`, `/subir-archivo`, `/crear-carpeta`, `/eliminar`, `/descargar`, `/drive/ver-archivo/**` — `ROLE_RRHH` or `ROLE_CANDIDATO`
- **Controller-level checks (no Spring Security rule):** `/drive/actualizar-estado` (RRHH only), `/calendario/candidato/{id}/eventos` (RRHH or the candidate itself), calendar KPIs are only added to the model for RRHH/ADMIN

## File Storage

Two distinct upload flows both write to `superfolder/` at the project root:

1. **`FilesServices.guardarArchivoPorEtapa()`** — used by RRHH upload flow, organizes as `superfolder/{etapa}/{owner_email}/filename`
2. **`DriveController`** — general drive UI, stores as `superfolder/{folderDestino}/filename` (flat, folder chosen by user)

File metadata is persisted in the `archivos` table. Max upload size: 30MB.

## Email

Gmail SMTP (`smtp.gmail.com:587`). The app password and base URL (`http://localhost:8080`) are hardcoded in `application.properties`. Both must be updated for production deployment.

`EmailService` sends styled HTML emails for: account activation (`email-verificacion`, link expires in 24h), password recovery, interview notifications, account lockout (`emails/email-bloqueo`), and candidate actions on interviews (`emails/email-accion-candidato` — confirmation/reprogramming requests to RRHH).

## Non-Obvious Behaviors

- **File visibility:** `ArchivosRepository.buscarArchivosVisiblesPara()` uses `LOWER()` for case-insensitive matching — a user sees files where they are either `propietario` or `destinario`. `DriveController` queries by both `username` and `email` and merges results via a `HashSet` to deduplicate.
- **Stage-based filtering:** RRHH users in `DriveController` see files filtered by folder path; candidates see only files they own or that are shared with them.
- **Token reuse:** generating a new activation/reset token overwrites `tokenactivacion`, invalidating any previous token regardless of purpose.
- **Login lockout:** after 5 failed attempts the account is blocked 15 minutes; the 5th failure redirects to `/login?bloqueado` and `LoginAttemptService` publishes `CuentaBloqueadaEvent` (in `com.back.shared.event`, must stay there — Spring Modulith enforces module boundaries) which triggers the lockout email.
- **Reenviar activación:** `GET/POST /registro/candidato/reenviar-activacion` renders `caduco.html` with an email form. `UsuarioService.reenviarActivacionPorEmail()` deletes old `ACTIVACION` tokens, issues a new one (24h TTL) and re-publishes `UsuarioRegistradoEvent`. Messages: "Revisa tu correo para activar tu cuenta." / "Este correo no está registrado en el sistema".
- **Reprogramación de entrevistas:** candidates use `POST /calendario/solicitar-reprogramacion/{id}` (motivo + nuevaFecha + nuevaHora). It validates ownership, requires motivo ≤500 chars, checks duplicate date/time, sets estado `REPROGRAMADO`, stores the reason in `observaciones` and publishes `AccionCandidatoEntrevistaEvent` (email to RRHH). Candidates can only set `CONFIRMADO`/`CANCELADO` via `/cambiar-estado`; confirming sends a confirmation email to RRHH.
- **Calendar KPIs** (`totalHoy`, `totalPendientes`, etc.) are computed only for RRHH/ADMIN; candidates see the calendar without global stats.
- **Notifications** (`notificaciones` schema) are filtered by `candidatoId` for candidates (sidebar, `/candidato/home`, `/candidato/perfil`, `GET /notificaciones`); RRHH/ADMIN see global ones.
- **Excel export:** `ExcelService` uses Apache POI to export the user list as XLSX, available at `/admin/exportar`.
- **`RegistroRRHHController`** at `/registro/rrhh` sets `ROLE_ADMIN` (not `ROLE_RRHH`) — likely a bug; verify before relying on it.
- **REST registration endpoint:** `POST /registro/candidato/api` accepts JSON and is used for modal-based registration from the drive UI.
- **`candidato.html` stepper:** rejection states (`Rechazad`, `No aceptado`, `No seleccionado`, `Descartado`) render as "Proceso finalizado" instead of a misleading percentage (`estadoRechazado` flag from `LoginController`).
- **Candidate portal:** `/candidato/home` (CandidatoHomeController) and `/candidato/perfil` (PerfilCandidatoController) are mapped under `/candidato/**`; the former lives in the `web` presentation layer (aggregates candidatos + calendario + drive + notificaciones), the latter in `candidatos`.
