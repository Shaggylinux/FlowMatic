# Manual Técnico — FLOWMATIC

Sistema de reclutamiento y gestión documental de candidatos con control de acceso basado en roles (RBAC).

---

## 1. Resumen del proyecto

**FLOWMATIC** es una aplicación web para el proceso de selección de personal: registro y activación de candidatos, gestión de perfiles, calendario de entrevistas, repositorio documental (drive) con revisión de documentos, notificaciones por correo y panel de administración con auditoría.

| Aspecto | Valor |
|---|---|
| Tipo | Aplicación web monolítica (MVC + REST) |
| Backend | Spring Boot 4.0.6, Java 21, Maven |
| Frontend | Thymeleaf (server-side) + CSS/JS estáticos |
| Persistencia | PostgreSQL 16 (Flyway), Redis 7 (tokens) |
| Seguridad | Spring Security + BCrypt + CSRF cookie-based |
| Arquitectura | Spring Modulith (módulos con esquemas DB propios) |
| Documentos | Apache POI (XLSX, DOCX) |
| Correo | Gmail SMTP (Spring Mail) |
| TZ de referencia | `America/Bogota` |

---

## 2. Arquitectura

### 2.1 Capas

```
Controller (Thymeleaf / REST)
        │
        ▼
Service (lógica de negocio + validación)
        │
        ▼
Repository (Spring Data JPA / Redis)
        │
        ▼
PostgreSQL / Redis / Filesystem (superfolder/)
```

Los módulos se comunican de forma desacoplada mediante **eventos de aplicación** (`ApplicationEventPublisher`) procesados de forma asíncrona (`@Async`) o tras el commit de la transacción (`@TransactionalEventListener`), con publicación durable vía Spring Modulith (`public.event_publication`).

### 2.2 Módulos (Spring Modulith)

| Módulo | Paquete | Esquema DB | Responsabilidad |
|---|---|---|---|
| `auth` | `com.back.auth` | `auth` | Usuarios, registro, activación, recuperación de contraseña |
| `admin` | `com.back.admin` | `admin` | Panel admin, RRHH, auditoría, configuración dinámica |
| `candidatos` | `com.back.candidatos` | `candidatos` | Perfil del candidato, gestión, filtros, match score |
| `calendario` | `com.back.calendario` | `calendario` | Entrevistas, estados, reprogramación, KPIs |
| `drive` | `com.back.drive` | `drive` | Metadatos de archivos/carpetas |
| `notificaciones` | `com.back.notificaciones` | `notificaciones` | Notificaciones web y envío de correos |
| `seguridad` | `com.back.seguridad` | `seguridad` | Intentos de login, bloqueo temporal |
| `web` | `com.back.web` | — | Presentación: dashboard RRHH, home candidato |
| `bootstrap` | `com.back.bootstrap` | — | Seeder de admin y migración de datos legacy |
| `exportacion` | `com.back.exportacion` | — | Exportación Excel (POI) y generación de CV (DOCX) |
| `shared` | `com.back.shared` | `shared` (legacy) | Interfaces `AuthApi`/`ConfiguracionApi`, DTOs, eventos, excepciones |
| `util` | `com.back.util` | — | `Sanitizer`, `ValidadorClave` |

**Reglas de arquitectura verificadas por tests** (`ArchitectureTest` + `ModulithTest`):
- `auth` no debe depender de módulos de negocio (candidatos, drive, calendario, admin, notificaciones).
- `shared` solo depende de java/jakarta/lombok/spring/jackson.
- Los servicios de `calendario` no deben usar el modelo `candidatos` (los controllers sí pueden cruzar módulos).
- No debe haber dependencias cíclicas entre módulos.

---

## 3. Modelo de datos

Las migraciones Flyway crean 9 esquemas. Detalle por tabla:

### 3.1 `auth.usuarios` (módulo auth)
| Columna | Tipo | Notas |
|---|---|---|
| `id` | bigserial PK | Se comparte como PK con los perfiles (`candidatos.candidatos`, `admin.rrhh`, `admin.administradores`) |
| `email` | varchar(255) UNIQUE | Identificador de login |
| `clave` | varchar(255) | BCrypt |
| `rol` | varchar(50) | `ROLE_CANDIDATO`, `ROLE_RRHH`, `ROLE_ADMINISTRADOR` |
| `activo` | boolean | Requerido para poder iniciar sesión |
| `bloqueado` | boolean | Bloqueo manual por administrador |

### 3.2 `drive.archivos` (módulo drive)
`nombre`, `ubicacion` (ruta física en `superfolder/`), `propietario`, `destinario` (email con quien se comparte), `es_carpeta`, `etapa`, `tipo_documento`, `estado_documento` (`Pendiente`/`Aprobado`/`Rechazado`/`No aplica`), `observacion`, `categoria_documento` (`Requerido`/`Opcional`), `fecha_subida`, `candidato_id` → FK `auth.usuarios(id)` ON DELETE CASCADE.

### 3.3 `candidatos.candidatos` (módulo candidatos)
Perfil completo del candidato (PK = id de usuario): `username`, `apellido`, `telefono`, `estado` (Registrado/En pruebas/Entrevista/Contratado/No aceptado…), `cargo`, `ciudad`, `tecnologias`, `idiomas`, `experiencia`, `disponibilidad`, `proceso_actual`, `foto_url`, `rrhh_email`, `ultima_actualizacion`, más campos del perfil extendido (V10): `nombres`, `tipo_documento`, `numero_documento`, `genero`, `estado_civil`, `fecha_nacimiento`, `nacionalidad`, `telefono_fijo`, `direccion`, `sobre_mi`, `area_profesional`, `pretension_salarial`, `modalidad_trabajo`, `formacion_json`, `experiencia_json`, `idiomas_json`.

### 3.4 `calendario.eventos` (módulo calendario)
`candidato_id` (FK candidatos), `candidato_nombre`, `fecha` (DATE), `hora` (TIME), `tipo`, `estado` (`PENDIENTE`/`CONFIRMADO`/`REPROGRAMADO`/`CANCELADO`/`REALIZADA`), `lugar` (debe ser URL), `vacante`, `modalidad`, `entrevistador`, `observaciones`, `rrhh_id` (FK admin.rrhh).

### 3.5 `notificaciones.notificaciones`
`tipo`, `mensaje`, `candidato_id` (NULL = global), `candidato_nombre`, `fecha`, `leida`, `enlace`.

### 3.6 `admin.*`
- `admin.rrhh`: `id` (PK=usuario), `username`, `apellido`, `telefono`, `foto_url`, `documento`, `cargo`, `ultimo_acceso`.
- `admin.administradores`: `id`, `username`, `apellido`.
- `admin.actividades`: auditoría — `accion`, `descripcion`, `realizado_por`, `tipo` (`SEGURIDAD`, `SISTEMA`, `USUARIO`), `fecha`.
- `admin.configuraciones`: pares `clave`/`valor` para configuración dinámica (ver §6).

### 3.7 `seguridad.login_attempts`
`email` UNIQUE, `attempts`, `blocked_until` (timestamp de desbloqueo).

### 3.8 `public.event_publication` (Spring Modulith)
Persistencia de eventos publicados entre módulos: `id`, `listener_id`, `event_type`, `serialized_event`, `publication_date`, `completion_date`.

### 3.9 `shared.historial`
Tabla de auditoría y trazabilidad de estados de candidatos: `id` (bigserial PK), `candidato_id` (FK `candidatos.candidatos(id)` ON DELETE CASCADE), `fecha` (`TIMESTAMP`), `estado_anterior` (`TEXT`), `estado_nuevo` (`TEXT`), `responsable` (`TEXT`). Gestionada mediante `HistorialService` y `HistorialRepository`.

### 3.10 Migraciones Flyway (13)
`V1__init.sql` (esquemas + tablas base) → `V2__event_publication` → `V3__rrhh_fields` → `V4__add_bloqueado_to_usuarios` → `V5__add_estado_observacion_archivos` → `V6__add_candidato_to_archivos` → `V7__fix_schema_relations_and_types` (FKs + tipo fecha historial) → `V8__add_fecha_subida_archivos` (backfill candidato_id) → `V9__add_rrhh_email_to_candidatos` → `V10__add_perfil_candidato_fields` → `V11__add_categoria_documento` → `V12__add_cascade_delete_constraints` (ON DELETE CASCADE en todas las FKs) → `V13__add_candidato_id_to_historial` (candidato_id + FK ON DELETE CASCADE e índices).

> `ddl-auto=validate` (prod y dev): la BD debe evolucionar solo vía migraciones.

---

## 4. Seguridad y autenticación

### 4.1 Configuración (`seguridad/SecurityConfig.java`)
- Login por formulario con parámetros **`email`** y **`clave`** (no los estándar `username`/`password`).
- CSRF **habilitado**: `CookieCsrfTokenRepository.withHttpOnlyFalse()` (cookie `XSRF-TOKEN`), con `CsrfCookieFilter` y `static/js/csrf.js` que inyecta el token en fetch/XHR/formularios.
- Logout limpia `JSESSIONID` y `XSRF-TOKEN`.
- `/api/seed` está excluido de CSRF.

### 4.2 Matriz de acceso (reglas reales)

| Patrón | Roles |
|---|---|
| `/registro/**`, `/login`, `/error`, `/forgot-password`, `/reset-password`, `/home`, `/`, `/css/**`, `/js/**`, `/videos/**`, `/api/seed`, `/actuator/health` | Público |
| `/candidato/**` | `ROLE_CANDIDATO` |
| `/calendario/**` | `ROLE_RRHH`, `ROLE_CANDIDATO` |
| `/gestion-candidatos/**` | `ROLE_RRHH`, `ROLE_ADMINISTRADOR` |
| `/admin/**` | `ROLE_ADMINISTRADOR` |
| `/drive/**`, `/rrhh/**`, `/subir-archivo`, `/crear-carpeta`, `/eliminar`, `/descargar`, `/drive/ver-archivo/**` | `ROLE_RRHH`, `ROLE_CANDIDATO` |
| Cualquier otra ruta | Autenticado |

**Chequeos a nivel de controller (no Spring Security):**
- `POST /drive/actualizar-estado` → solo RRHH.
- `GET /calendario/candidato/{id}/eventos` → RRHH o el propio candidato.
- KPIs del calendario solo se agregan al modelo para RRHH.
- `POST /calendario/crear`, `actualizar`, `eliminar` → solo RRHH.
- `POST /calendario/cambiar-estado` → candidato solo puede `CONFIRMADO`/`CANCELADO` y únicamente en eventos propios.
- `POST /calendario/solicitar-reprogramacion/{id}` → solo el candidato dueño del evento.

### 4.3 Flujo de login
1. `CustomUserDetailsService.loadUserByUsername` normaliza el email y verifica: no bloqueado temporalmente, usuario existe, `activo=true`, no `bloqueado` por admin. Si falla → `UsernameNotFoundException` ("Bad credentials").
2. `LoginSuccessHandler` resetea intentos fallidos y redirige a `/post-login`.
3. `LoginFailureHandler` registra el fallo vía `LoginAttemptService` y publica `AuditoriaEvent`. Tras el 5.º fallo bloquea 15 min y redirige a `/login?bloqueado`, publicando `CuentaBloqueadaEvent` (dispara el correo de bloqueo).
4. `/post-login` (LoginController) enruta por rol: RRHH → `/dashboard`, CANDIDATO → `/candidato/home`, ADMINISTRADOR → `/admin/dashboard`.

### 4.4 Tokens (Redis)
Los tokens de activación y de reset de contraseña se guardan en Redis (`@RedisHash("Tokens")`, clase `Token`) con TTL:
- Activación: 24 h (86 400 s).
- Reset: `password.reset.expiry.minutes` (default 15 min) × 60 s.

> **Nota:** `validarTokenReset` no comprueba caducidad explícitamente; la expiración la aplica el TTL de Redis. Ambos tipos de token coexisten por `usuarioId` indexado.

### 4.5 Política de contraseñas (`ValidadorClave`)
Mínimo 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial. Se aplica en registro, cambio de contraseña, reset y edición de RRHH.

---

## 5. Módulos y flujos de negocio

### 5.1 Registro y activación (`auth` + `registro` + listeners)
1. `RegistroController` (`POST /registro/candidato`) o `POST /registro/candidato/api` (JSON, usado por el modal del drive) validan el formulario.
2. `UsuarioService.registrarUsuario` (`AuthApi`): verifica email único, valida clave segura, guarda usuario con BCrypt, crea token de activación en Redis y publica `UsuarioRegistradoEvent`.
3. Listeners (`AFTER_COMMIT`, `@Async`):
   - `EmailEventListener` → correo de verificación con enlace `/registro/candidato/activar?token=...`.
   - `CandidatoRegistrationListener` (rol CANDIDATO) → crea perfil `Candidato` + carpeta `superfolder/Candidatos/{Nombre Apellido}/`.
   - `RRHHRegistrationListener` (rol RRHH) → crea perfil `RRHH`.
4. `GET /registro/candidato/activar?token=...` activa la cuenta (elimina el token).
5. Reenvío de activación (`POST /registro/candidato/reenviar-activacion`): elimina tokens `ACTIVACION` previos, crea uno nuevo y re-publica el evento.

### 5.2 Recuperación de contraseña (`PasswordController`)
`POST /forgot-password` → `generarTokenRecuperacion` (no revela si el email existe) → `PasswordResetSolicitadoEvent` → correo con enlace `/reset-password?token=...`. `POST /reset-password` valida la clave y la reemplaza.

### 5.3 Panel RRHH (`web/DashboardController`)
`GET /dashboard` (solo `ROLE_RRHH`): KPIs del pipeline de candidatos (Registrado/En pruebas/Entrevista/Contratado/No aceptado), carpetas/archivos, actividad reciente, próximas entrevistas.

### 5.4 Portal candidato (`web/CandidatoHomeController`, `candidatos/PerfilCandidatoController`)
- `GET /candidato/home`: resumen con estado del proceso (stepper con porcentaje; los estados de rechazo muestran "Proceso finalizado" en lugar de porcentaje), próximas entrevistas, documentos y notificaciones.
- `GET /candidato/perfil` + `POST /candidato/perfil/actualizar` (JSON): CRUD del perfil extendido (datos personales, formación/experiencia/idiomas en JSON para el frontend).

### 5.5 Gestión de candidatos (`candidatos/CandidatoController`, solo RRHH/ADMIN)
- `GET /gestion-candidatos`: listado paginado con filtros (texto, cargo, estado, experiencia mínima, ciudad).
- `GET /gestion-candidatos/api`: JSON paginado para la tabla (incluye `MatchScoreCalculator.calcularMatchScore`).
- `POST /{id}/estado`, `/{id}/editar`, `/{id}/eliminar` (JSON). Al eliminar: borra físicamente los archivos de `superfolder/`, elimina registros de `archivos`, notificaciones, publica `CandidatoEliminadoEvent` (elimina entrevistas del candidato) y borra usuario.
- Documentos: `GET /{id}/documentos`, `POST /{id}/documentos/subir`.
- Exportaciones: `GET /{id}/cv` (DOCX vía `CvService`) y `GET /export` (XLSX).

### 5.6 Drive (`candidatos/drive/DriveController` + `drive/FilesServices`)
- Almacenamiento físico: `superfolder/{carpeta}/{archivo}` (raíz del proyecto, volumen Docker `superfolder`). Tamaño máx.: 30 MB.
- **Visibilidad:** `ArchivosRepository.buscarArchivosVisiblesPara` (case-insensitive) — el usuario ve archivos donde es `propietario`, `destinario` o vinculado como `candidato`. RRHH ve el conjunto completo de archivos (con deduplicación vía `HashSet`); si el archivo pertenece a un candidato asignado (`candidatos.rrhh_email`), el RRHH asignado lo ve; los demás RRHH no (filtrado por carpeta).
- Operaciones: crear/renombrar/eliminar carpetas (recursivo), subir archivo (con reemplazo vía `fileId`), descargar, ver (inline), compartir (`POST /drive/compartir` → notificación al destinatario), cambiar estado del candidato, revisar documento (`POST /drive/actualizar-estado-archivo`, solo RRHH, solo archivos ajenos, no aplica a "No aplica", rechazo obliga observación).
- Sanitización de rutas: `Sanitizer.sanitizePath` elimina `..`, barras iniciales/finales y normaliza `\` → `/`.
- Los candidatos ven la vista `candidato-documentos.html`; los RRHH, `drive.html`.

### 5.7 Calendario (`calendario/CalendarioController` + `EventoService`)
- `GET /calendario/eventos?start&end[&candidatoId&estado&rrhhId]` devuelve eventos para el calendario con colores por estado (PENDIENTE azul, CONFIRMADO verde, REPROGRAMADO naranja, CANCELADO rojo, REALIZADA gris).
- `POST /calendario/crear` (solo RRHH): valida vía `EventoValidator` (hora 07:00–19:00, fecha no pasada, sin duplicados por candidato+fecha+hora, lugar URL, entrevistador solo letras, observaciones ≤ 500). Publica `EntrevistaAgendadaEvent` → correos a RRHH y candidato + notificación web.
- `POST /calendario/actualizar/{id}`: marca el evento como `REPROGRAMADO` y notifica.
- `POST /calendario/cambiar-estado/{id}`: candidato solo `CONFIRMADO`/`CANCELADO`; al confirmar envía correo a RRHH (`AccionCandidatoEntrevistaEvent`).
- `POST /calendario/solicitar-reprogramacion/{id}`: motivo obligatorio (≤500), nueva fecha/hora sin duplicado, estado `REPROGRAMADO`, motivo guardado en `observaciones`, correo a RRHH.
- `GET /calendario/export`: Excel de entrevistas filtrado.
- KPIs (hoy, pendientes, confirmadas, reprogramadas, canceladas, mes, candidatos únicos, próximas) solo para RRHH.

### 5.8 Notificaciones (`notificaciones`)
- `GET /notificaciones`: no leídas (por candidato si es CANDIDATO; globales para RRHH/ADMIN).
- `POST /notificaciones/{id}/leer` y `POST /notificaciones/leer-todas`.
- `EmailService`: correos con plantillas Thymeleaf (`emails/email-verificacion`, `email-recuperacion`, `email-bloqueo`, `email-entrevista`, `email-entrevista-candidato`, `email-accion-candidato`).

### 5.9 Panel admin (`admin/AdminController` + `AdminRRHHRestController`)
- `GET /admin/dashboard`: KPIs (total usuarios, RRHH activos, candidatos registrados/pendientes, entrevistas hoy), donut de roles (circunferencia SVG), actividad reciente (auditoría), configuración.
- `GET /admin` (panel de usuarios): búsqueda multicriterio (email, nombre, apellido, documento) + filtro de estado (Activo/Pendiente/Bloqueado), paginado 5/página con elipsis (`PageItem`).
- `POST /admin/crear-rrhh`: crea usuario RRHH (valida teléfono/documento numéricos, cargo solo letras) → redirige con `?pendiente` / `?error=...`.
- CRUD REST: `GET/PUT/DELETE /admin/api/rrhh/{id}`; `PUT` valida email duplicado (409, ignorando el propio), clave segura (400), teléfono/documento/cargo; `DELETE` borra `admin.rrhh` antes que el usuario (FK); `PUT /{id}/toggle-bloqueo` para bloquear/desbloquear.
- Exportaciones: `GET /admin/exportar` (usuarios con los mismos filtros de la vista), `GET /admin/reportes/exportar` (métricas).
- Auditoría: `GET /admin/actividad` (todo) y `GET /admin/auditoria` (solo tipo SEGURIDAD), paginados JSON.
- Configuración: `GET /admin/configuraciones` (claves con valor y default) y `POST /admin/configuraciones` (guarda las que llegan con prefijo `cfg_`).
- `POST /admin/cambiar-clave`: cambio de contraseña del admin logueado.

---

## 6. Configuración

### 6.1 Variables de entorno (ver `.env.example` y `application.properties`)

| Variable | Default dev | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/gestion` | JDBC |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `12345` | Credenciales DB |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis (tokens) |
| `MAIL_HOST` / `MAIL_PORT` | `smtp.gmail.com` / `587` | SMTP |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail + app password | Credenciales de correo |
| `MAIL_FROM` | `malacruz132@gmail.com` | Remitente |
| `FILE_STORAGE_ROOT` | `./superfolder` | Raíz de archivos |
| `LOGIN_MAX_ATTEMPTS` | `5` | Intentos fallidos antes de bloquear |
| `LOGIN_BLOCK_MINUTES` | `15` | Minutos de bloqueo |
| `PASSWORD_MIN_LENGTH` | `8` | Largo mínimo de clave |
| `PASSWORD_RESET_EXPIRY_MINUTES` | `15` | Vigencia del token de reset |
| `APP_BASE_URL` | `http://localhost:8080` | Base para enlaces de correos |
| `APP_SUPPORT_EMAIL` | vacío | Correo de soporte |

### 6.2 Configuración dinámica en BD (`admin.configuraciones`)
Claves gestionables desde el panel admin (con fallback a defaults): `password.min.length`, `login.max.attempts`, `login.block.minutes`, `app.name`, `app.support.email`, `password.reset.expiry.minutes`. Se leen vía `ConfiguracionApi.getValor(clave, default)`.

### 6.3 Otros ajustes relevantes
- Multipart: 30 MB por archivo y por request.
- `spring.thymeleaf.cache=false` en dev.
- Actuator expone solo `/actuator/health`.
- Perfil `prod` (`application-prod.properties`): sin `show-sql`, todo por variables de entorno obligatorias (`DB_URL`, `MAIL_*`, `APP_BASE_URL`…).

---

## 7. Eventos del sistema

| Evento | Publicado por | Listeners | Efecto |
|---|---|---|---|
| `UsuarioRegistradoEvent` | `UsuarioService` | `EmailEventListener`, `CandidatoRegistrationListener`, `RRHHRegistrationListener` | Correo de verificación; crear perfil candidato + carpeta; crear perfil RRHH |
| `PasswordResetSolicitadoEvent` | `UsuarioService` | `EmailEventListener` | Correo de recuperación |
| `CuentaBloqueadaEvent` | `LoginAttemptService` | `EmailEventListener` | Correo de bloqueo |
| `AuditoriaEvent` | `LoginFailureHandler` | `AuditoriaEventListener` | Registro en `admin.actividades` |
| `EntrevistaAgendadaEvent` | `CalendarioController` | `EntrevistaEventListener` | Correos a RRHH y candidato + notificación web |
| `EntrevistaNotificacionEvent` | `CalendarioController` | `EntrevistaEventListener` | Notificación web |
| `AccionCandidatoEntrevistaEvent` | `CalendarioController` | `EntrevistaEventListener` | Correo a RRHH (confirmación/reprogramación) |
| `CandidatoEliminadoEvent` | `CandidatoController` | `EventoService` | Borra entrevistas del candidato |

Los eventos de dominio (`shared.event`) usan `@ApplicationModuleListener` (persistencia en `public.event_publication`); los listeners de creación de perfiles usan `@TransactionalEventListener(AFTER_COMMIT)`.

---

## 8. Referencia de endpoints HTTP

### 8.1 Públicos
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/`, `/home` | Landing |
| GET/POST | `/login` | Login (email/clave) |
| GET | `/post-login` | Redirección por rol |
| GET/POST | `/forgot-password`, `/reset-password` | Recuperación de contraseña |
| GET/POST | `/registro/candidato` | Registro candidato |
| GET | `/registro/candidato/activar?token=` | Activación de cuenta |
| GET/POST | `/registro/candidato/reenviar-activacion` | Reenvío de token |
| POST | `/registro/candidato/api` | Registro JSON (modal drive) |
| GET/POST | `/registro/rrhh` | Registro RRHH |
| POST | `/api/seed` | Datos de prueba (CSRF exento) |
| GET | `/actuator/health` | Healthcheck |

### 8.2 Candidato
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/candidato/home` | Portal candidato |
| GET | `/candidato/perfil` | Ver perfil |
| POST | `/candidato/perfil/actualizar` | Actualizar perfil (JSON) |

### 8.3 RRHH / Candidato (drive y calendario)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/drive` | Vista drive/documentos (filtros: folder, page, buscar, tipo, estado, categoria, tab) |
| POST | `/drive/crear-carpeta` | Crear carpeta |
| POST | `/drive/renombrar-carpeta` | Renombrar carpeta |
| POST | `/drive/eliminar-carpeta` | Eliminar carpeta (solo propietario o RRHH) |
| POST | `/drive/subir-archivo` | Subir archivo (30 MB máx.) |
| GET | `/drive/descargar?fileId=` | Descargar archivo |
| GET | `/drive/ver-archivo/{id}` | Ver inline (content-type detectado) |
| POST | `/drive/eliminar` | Eliminar archivo (solo propietario) |
| POST | `/drive/compartir` | Compartir con email destinatario |
| POST | `/drive/actualizar-estado` | **RRHH:** cambiar estado del candidato |
| POST | `/drive/actualizar-estado-archivo` | **RRHH:** aprobar/rechazar documento |
| GET | `/calendario` | Vista calendario |
| GET | `/calendario/eventos` | Eventos por rango/filtros (JSON) |
| POST | `/calendario/crear` | **RRHH:** crear entrevista |
| POST | `/calendario/actualizar/{id}` | **RRHH:** reprogramar |
| POST | `/calendario/cambiar-estado/{id}` | Cambiar estado (candidato: CONFIRMADO/CANCELADO) |
| POST | `/calendario/solicitar-reprogramacion/{id}` | **Candidato:** solicitar reprogramación |
| POST | `/calendario/eliminar/{id}` | **RRHH:** eliminar |
| POST | `/calendario/{id}/nota` | Guardar observaciones |
| GET | `/calendario/export` | Exportar Excel de entrevistas |
| GET | `/calendario/candidato/{id}/eventos` | Eventos de un candidato |
| GET/POST | `/notificaciones` y `/notificaciones/{id}/leer`, `/leer-todas` | Notificaciones |

### 8.4 RRHH / Administrador (gestión de candidatos)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/gestion-candidatos` | Listado con filtros y paginación |
| GET | `/gestion-candidatos/api` | Listado JSON |
| GET | `/gestion-candidatos/{id}` | Detalle JSON |
| POST | `/gestion-candidatos/{id}/estado` | Cambiar estado |
| POST | `/gestion-candidatos/{id}/editar` | Editar candidato |
| POST | `/gestion-candidatos/{id}/eliminar` | Eliminar (archivos, notificaciones, entrevistas) |
| GET | `/gestion-candidatos/stats` | Contadores |
| GET | `/gestion-candidatos/{id}/documentos` | Documentos del candidato |
| POST | `/gestion-candidatos/{id}/documentos/subir` | Subir documento |
| GET | `/gestion-candidatos/{id}/cv` | Descargar CV en DOCX |
| GET | `/gestion-candidatos/export` | Exportar XLSX |

### 8.5 Administrador
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/admin/dashboard` | Dashboard con KPIs |
| GET | `/admin` | Panel de usuarios RRHH |
| POST | `/admin/crear-rrhh` | Crear RRHH |
| GET | `/admin/exportar` | Excel de usuarios |
| GET | `/admin/reportes/exportar` | Excel de métricas |
| GET | `/admin/actividad` | Actividad (JSON paginado) |
| GET | `/admin/auditoria` | Auditoría de seguridad (JSON paginado) |
| GET | `/admin/configuraciones` | Configuración actual (JSON) |
| POST | `/admin/configuraciones` | Guardar configuración |
| POST | `/admin/cambiar-clave` | Cambiar clave del admin |
| GET/PUT/DELETE | `/admin/api/rrhh/{id}` | CRUD REST de RRHH |
| PUT | `/admin/api/rrhh/{id}/toggle-bloqueo` | Bloquear/desbloquear |

### 8.6 Dashboard
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/dashboard` | Dashboard RRHH |

---

## 9. Construcción, ejecución y despliegue

### 9.1 Requisitos
- JDK 21, Maven 3.9+.
- Docker (PostgreSQL 16 + Redis 7) o instancias locales.

### 9.2 Desarrollo
```bash
# Levantar infraestructura
docker compose up -d db redis

# Ejecutar la app (dev: Thymeleaf cache off, show-sql on)
mvn spring-boot:run

# Usuarios de prueba (creados automáticamente)
#   admin@flowmatic.com / admin123*    (AdminSeeder, al arrancar)
#   POST /api/seed                     (rrhh@flowmatic.com/Admin1234, admin@sistema.com/Admin1234, 8 candidatos Test1234)
```

### 9.3 Compilación y tests
```bash
mvn clean install          # con tests
mvn clean package -DskipTests
mvn test -Dtest=BackApplicationTests   # test específico
```

### 9.4 Despliegue con Docker (completo)
```bash
docker compose up --build -d
```
- `flowmatic-db` (PostgreSQL 16, puerto 5432, volumen `pgdata`).
- `flowmatic-redis` (Redis 7, puerto 6379).
- `flowmatic-app` (imagen multi-stage: build Maven → JRE 21; puerto 8080; volumen `superfolder`; healthcheck en `/actuator/health`; TZ `America/Bogota`).
- Las credenciales de correo se pasan por variables (`MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `APP_BASE_URL`).

### 9.5 Producción
- Usar el perfil `prod` (`SPRING_PROFILES_ACTIVE=prod`) con variables de entorno obligatorias.
- **Actualizar obligatoriamente** el app password de Gmail y `APP_BASE_URL` (están hardcodeados por defecto en dev).
- El esquema evoluciona con Flyway (`ddl-auto=validate`).

---

## 10. Estrategia de pruebas

| Test | Tipo | Qué verifica |
|---|---|---|
| `ArchitectureTest` | ArchUnit | Aislamiento entre módulos (auth no depende de negocio, shared solo de java/jakarta/spring, servicios de calendario sin candidatos) |
| `ModulithTest` | Spring Modulith | Arquitectura modular sin ciclos + generación de diagramas PlantUML |
| `BackApplicationTests` | Integración | Contexto de Spring arranca (H2 en modo PostgreSQL) |
| `TokenTest` | Integración | Ciclo de tokens Redis (creación/activación) |
| `ThymeleafTest` | Integración | Render de plantillas de correo (no escapa el token) |
| `UsuarioServiceTest`, `LoginAttemptServiceTest` | Unit (Mockito) | Registro, duplicados, clave corta, eventos; bloqueo e intentos |
| `EventoValidatorTest`, `ValidadorClaveTest`, `MatchScoreCalculatorTest`, `CandidatoServiceTest`, `NotificacionServiceTest` | Unit | Reglas de validación y cálculos |

- Tests de integración usan H2 `MODE=PostgreSQL` con Flyway deshabilitado y `ddl-auto=create-drop` (`application-test.properties`).

---

## 11. Comportamientos no obvios y consideraciones

1. **IDs compartidos:** `Candidato`, `RRHH` y `Administrador` usan como PK el `id` de `auth.usuarios`. Nunca insertar perfiles sin crear antes el usuario.
2. **Borrado de RRHH:** `DELETE /admin/api/rrhh/{id}` borra primero la fila de `admin.rrhh` (FK `fk_rrhh_usuarios` ON DELETE CASCADE en V12, pero el código lo hace explícitamente).
3. **Tokens:** generar un token nuevo (activación o reset) no elimina el otro tipo; solo los del mismo tipo se limpian. El campo es compartido en Redis, por lo que un token de un tipo no sirve para el otro.
4. **Validación de reset:** no hay check explícito de expiración en `validarTokenReset`; depende del TTL de Redis.
5. **Rol `ROLE_ADMIN`:** `CalendarioController` compara contra `"ROLE_ADMIN"` (no existe en el sistema; los admins tienen `ROLE_ADMINISTRADOR`). Los admins no deberían acceder a `/calendario` (no está en la matriz), pero los chequeos internos podrían no reconocerlos.
6. **Drive RRHH:** el RRHH ve archivos de candidatos asignados (`candidatos.rrhh_email`) o donde figura como propietario/destinatario; el resto lo ve todo. La deduplicación se hace en memoria (`HashSet`).
7. **Revisión de documentos:** solo se pueden aprobar/rechazar archivos ajenos y que no tengan estado `No aplica`. El rechazo requiere observación; aprobar limpia la observación.
8. **Candidato en drive:** subir un archivo como candidato lo coloca automáticamente en `Candidatos/{Nombre Apellido}/`.
9. **Zona horaria:** fechas de archivos (`fecha_subida`, "Hoy/Ayer") se calculan en `America/Bogota`.
10. **Código muerto / legacy:**
    - `FilesServices.guardarArchivoPorEtapa()` (flujo por etapas, sin uso; todo pasa por `DriveController`).
    - `GenHash.java`, `HashPassword.java`, `Conectar.java` (raíz y `DB/`) — utilidades sueltas; `GenHash` ignora sus argumentos y genera el hash de `admin123*`.
    - `CalendarioController.exportar` y `obtenerEventos` usan `rrhhId` como filtro opcional.
11. **Sanitización de rutas:** `Sanitizer` elimina `..` y normaliza separadores; los nombres de archivo no pueden contener `/` ni `\` ni `..`.
12. **Seguridad:** el app password de Gmail y `APP_BASE_URL` vienen hardcodeados en `application.properties` (dev); deben ir por variables en producción.

---

## 12. Estructura de frontend (referencia rápida)

- **Plantillas** (`templates/`): `login`, `home`, `registro-candidato`, `registro-rrhh`, `activacion`, `caduco`, `forgot-password`, `reset-password`, `dashboard-rrhh`, `candidato`, `candidato-perfil`, `candidato-documentos`, `drive`, `calendario`, `gestion-candidatos`, `admin`, `error`, `error/404`, `error/500`.
- **Fragmentos** (`templates/fragments/`): `sidebar-rrhh`, `sidebaradmin`, `logo`, `modal-registro`, `success-message`, `admin/*` (dashboard-content, header, modals, rrhh-content).
- **Correos** (`templates/emails/`): 6 plantillas HTML estilizadas.
- **JS**: `csrf.js` (token CSRF), `gestion-candidatos.js`, `admin.js`, `admin/rrhh.js` (drawer/toasts), `i18n.js` (diccionario del admin).
- **CSS**: un archivo por vista (`base.css`, `drive.css`, `calendario.css`, `admin/*`, etc.).