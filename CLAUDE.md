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
```

**Packages (actual):** module-based — `auth`, `candidatos` (incl. `candidatos.drive`), `calendario`, `drive`, `notificaciones`, `admin` (incl. `admin.dto`), `seguridad` (SecurityConfig, CustomUserDetailsService), `registro`, `shared` (events only), `web` (presentation layer: `CandidatoHomeController`, `DashboardController`, `AtributosUsuarioAdvice`). `ArchitectureTest` (ArchUnit) and `ModulithTest` enforce isolation: `auth` must not depend on business modules, `shared` only on java/jakarta/lombok/spring/jackson, and `calendario` services must not use `candidatos` (controllers may cross modules).

**Templates:** `src/main/resources/templates/` (Thymeleaf `.html` files)  
**Static assets:** `src/main/resources/static/` (CSS, JS, videos)  
**Config:** `src/main/resources/application.properties`

## Database

PostgreSQL on `localhost:5432/gestion` (Docker). Seed admin user: `admin@flowmatic.com` / `admin123*` (GenHash hardcodes `admin123*` — it ignores its args). `database.sql` referenced below no longer exists in the repo; JPA `ddl-auto: update` auto-migrates and the seed is applied by the Docker startup.

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

1. **`FilesServices.guardarArchivoPorEtapa()`** — organizes as `superfolder/{etapa}/{owner_email}/filename` (**dead code**, unused; see Non-Obvious Behaviors)
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
- **`RegistroRRHHController`** at `/registro/rrhh` sets `ROLE_RRHH` (role is correct; the user must still be activated before login).
- **REST registration endpoint:** `POST /registro/candidato/api` accepts JSON and is used for modal-based registration from the drive UI. Returns 409 with body `"El usuario ya existe"` for duplicates — `fragments/modal-registro.html` shows the response body verbatim in an alert (both for errors and the success message).
- **Document review states** (`drive.archivos.estado_documento`): `Pendiente` (default), `Aprobado`, `Rechazado`. `POST /drive/actualizar-estado-archivo` accepts **only** `Aprobado`/`Rechazado` and requires an `observacion` when rejecting (otherwise redirects to `/drive?estadoError=observacion|estado`). Only non-owner files can be reviewed (owner or "No aplica" → no-op). Rejection reason is stored in `observacion`; approving clears it.
- **Calendar PENDIENTE color:** `PENDIENTE` events render **blue** (`#DBEAFE`/`#2563EB`/`#1D4ED8`) in both `CalendarioController.obtenerEventos` (backend DTO) and `calendario.html` `getEstadoInfo`; the legend matches these colors. The state value stays `PENDIENTE` (it is not renamed to `AGENDADA`).
- **Calendar empty state:** when a fetched range has no events, `calendario.html` shows "No hay entrevistas agendadas para este período" (`#calSinEntrevistas` overlay toggled in the `events` callback).
- **Entrevista emails** include `modalidad` and `entrevistador` (`EntrevistaEmailDTO` record has both fields; `CalendarioController.crearEvento` passes `evento.getModalidad()`/`getEntrevistador()`; templates `email-entrevista.html` + `email-entrevista-candidato.html` render them conditionally).
- **Drive flash messages:** `POST /drive/compartir` redirects to `/drive?compartido=ok` (green banner "Archivo compartido correctamente"); the `estadoError` param renders red banners for rejected-state validation failures.
- **`candidato.html` stepper:** rejection states (`Rechazad`, `No aceptado`, `No seleccionado`, `Descartado`) render as "Proceso finalizado" instead of a misleading percentage (`estadoRechazado` flag from `LoginController`).
- **Candidate portal:** `/candidato/home` (CandidatoHomeController) and `/candidato/perfil` (PerfilCandidatoController) are mapped under `/candidato/**`; the former lives in the `web` presentation layer (aggregates candidatos + calendario + drive + notificaciones), the latter in `candidatos`.
- **Dead code:** `FilesServices.guardarArchivoPorEtapa()` (RRHH stage-based upload flow) is unused — all uploads go through `DriveController`. `.badge-available` (green) exists in `base.css` for confirmed-interview badges in `dashboard-rrhh.html`.
- **Admin portal (HU-005/006/007/008):** `/admin/dashboard` shows KPI cards (`Total Usuarios`, `RRHH Activos`, `Candidatos Registrados`, `Candidatos Pendientes`), role donut (empty state "No hay usuarios registrados") and recent-activity timeline (empty state "No hay actividad reciente"). `AdminController.panelAdmin` lists RRHH at 5/page; `pageItems` (record `PageItem(int number, boolean ellipsis)`) drives clickable page numbers + ellipsis in `rrhh-content.html`. `POST /admin/crear-rrhh` redirects `?pendiente` (ok), `?error=duplicado` and `?error=clave_corta`; `rrhh-content.html` renders those as toasts "Usuario creado. Se envió el correo de activación", "Email duplicado", "La contraseña debe tener mínimo 8 caracteres".
- **Admin REST CRUD:** `GET/PUT/DELETE /admin/api/rrhh/{id}` (live, drawer + edit modal + delete). `PUT` validates duplicate email (409 `{"error":"Email duplicado"}` via `UsuarioRepository.findByEmail`, ignoring self) and clave <8 (400 `{"error":"La contraseña debe tener mínimo 8 caracteres"}`); `DELETE` must remove the `rrhh` row first (FK `fk_rrhh_usuarios` on `admin.rrhh`, NOT `public.rrhh`). `PUT /{id}/estado` + `AdminService` + legacy fragments (`table.html`, `toolbar.html`, `modal-editar.html`, `POST /admin/editar`, `POST /admin/eliminar/{id}`, `admin.js` pagination helpers) were deleted as dead code.
- **Admin JS/CSS:** `admin.js` (admin.html) drives notifications, actividad/auditoria/configuración modals and the mobile sidebar; `admin/rrhh.js` drives the RRHH drawer/table with `mostrarToastAdmin()` (green/red toasts) and `toggleBloqueoRRHH` (`PUT /{id}/toggle-bloqueo`). i18n: `static/js/i18n.js` holds the admin dictionary (KPIs, table headers, pagination, drawer, toasts) applied via `data-i18n`. Dashboard cards use `gs-stat-icon-orange/red` in `admin/dashboard.css`.
