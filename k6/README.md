# FlowMatic - Suite de Pruebas de Rendimiento (k6) y Cobertura (JaCoCo)

Este directorio contiene los scripts automatizados para la ejecución de **Pruebas de Carga (Nivel 3)** y **Pruebas de Estrés (Nivel 4)** mediante **k6**, así como la configuración para la generación de reportes de cobertura con **JaCoCo**.

---

## 1. Criterios de Entrada (Entry Criteria)

Antes de iniciar la ejecución de las pruebas, asegúrate de cumplir con el estado técnico requerido:

1. **Servicios en Docker:**
   ```bash
   docker compose up -d --build
   ```
   Esto levantará:
   - `flowmatic-app` (Spring Boot 4.0.6 en http://localhost:8080)
   - `flowmatic-db` (PostgreSQL 16 con las 16 migraciones Flyway ejecutadas)
   - `flowmatic-redis` (Redis 7 para sesiones y caché)

2. **Instalación de k6:**
   - **Linux (Ubuntu/Debian):**
     ```bash
     sudo gpg -k
     sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
     echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
     sudo apt-get update && sudo apt-get install k6
     ```
   - O usando binario standalone: https://k6.io/docs/get-started/installation/

---

## 2. Estructura de Módulos y Pruebas

```
k6/
├── config/
│   └── env.js                         # Configuración de URLs, timeouts y credenciales
├── utils/
│   ├── auth.js                        # Extractor CSRF, autenticación y generador de archivos multipart
│   └── reporter.js                    # Generador de reportes HTML interactivos y JSON
├── modules/
│   ├── 01_seguridad/
│   │   ├── load_auth_login.js         # Nivel 3: 200 inicios concurrentes / 5 min continuos
│   │   └── stress_auth_bruteforce.js  # Nivel 4: Spike súbito de 1,000 logins erróneos en 10s
│   ├── 02_candidatos/
│   │   ├── load_candidatos_busqueda.js# Nivel 3: 150 usuarios aplicando filtros simultáneos (Java, Medellín, Senior)
│   │   └── stress_reporte_excel.js    # Nivel 4: 50 descargas simultáneas de reportes masivos Excel (JVM Memory)
│   ├── 03_calendario/
│   │   ├── load_calendario_feed.js    # Nivel 3: 300 peticiones concurrentes a feed JSON (>= 180 req/s, avg <= 250ms)
│   │   └── stress_race_condition.js   # Nivel 4: Condición de carrera (2 reclutadores agendando simultáneamente)
│   └── 04_drive/
│       ├── load_drive_upload.js       # Nivel 3: 50 subidas continuas de PDFs de 3 a 5 MB (p95 <= 2.5s)
│       └── stress_drive_limites.js    # Nivel 4: 25 subidas simultáneas de 29.5 MB + intento de 45 MB (HTTP 413)
├── reports/                           # Directorio donde se guardan los reportes HTML y JSON generados
└── run_tests.sh                       # Script bash unificado de ejecución
```

---

## 3. Ejecución de Pruebas de Rendimiento

El script `./k6/run_tests.sh` permite ejecutar pruebas específicas o suites completas:

```bash
# Dar permisos de ejecución si es necesario
chmod +x k6/run_tests.sh

# Ejecutar pruebas individuales:
./k6/run_tests.sh load-auth
./k6/run_tests.sh stress-auth
./k6/run_tests.sh load-candidatos
./k6/run_tests.sh stress-candidatos
./k6/run_tests.sh load-calendario
./k6/run_tests.sh stress-calendario
./k6/run_tests.sh load-drive
./k6/run_tests.sh stress-drive

# Ejecutar por categorías:
./k6/run_tests.sh load-all      # Todas las pruebas de carga (Nivel 3)
./k6/run_tests.sh stress-all    # Todas las pruebas de estrés (Nivel 4)
./k6/run_tests.sh all           # Suite completa
```

### Variables de Entorno Personalizables:
```bash
BASE_URL="http://flowmatic.tech" ./k6/run_tests.sh load-auth
```

---

## 4. Generación de Reportes de Cobertura con JaCoCo

El plugin de **JaCoCo 0.8.12** ha sido integrado en el `pom.xml` del proyecto.

### Para ejecutar las pruebas unitarias y generar el reporte JaCoCo:
```bash
./mvnw clean test
```

### Ubicación del reporte JaCoCo generado:
- **Reporte HTML interactivo:** `target/site/jacoco/index.html`
- **Reporte XML (para SonarQube/CI/CD):** `target/site/jacoco/jacoco.xml`

Para abrir el reporte en el navegador:
```bash
xdg-open target/site/jacoco/index.html  # En Linux
# o abrir directamente el archivo HTML
```

---

## 5. Criterios de Aceptación (Quality Gates)

| Módulo | Tipo de Prueba | Métrica Clave / Umbral | Estado Objetivo |
| :--- | :--- | :--- | :--- |
| **M1: Seguridad** | Carga (`/login`) | Latencia p95 $\le$ 1.2s, Errores 5xx = 0.00% | CPU BCrypt $\le$ 65% |
| **M1: Seguridad** | Estrés Spike | 1,000 reqs / 10s | Sin OOM ni caída de Tomcat |
| **M2: Candidatos** | Carga (Filtros) | 150 VUs, Latencia p95 $\le$ 1.5s, 10 reg/pág | Índices B-Tree V16 |
| **M2: Candidatos** | Estrés Memoria | 50 descargas masivas de Excel | Sin `OutOfMemoryError` |
| **M3: Calendario** | Carga (Feed) | Throughput $\ge$ 180 req/s, avg $\le$ 250ms | HikariCP $\le$ 8/10 |
| **M3: Calendario** | Estrés Carrera | 2 citas en el mismo milisegundo | 1 creada, 1 rechazada limpiamente |
| **M4: Drive** | Carga I/O | 50 subidas continuas de PDFs 3-5 MB, p95 $\le$ 2.5s | I/O fluido sin bloqueos |
| **M4: Drive** | Estrés Cuotas | 25 subidas de 29.5 MB (OK) + 1 de 45 MB (413) | Rechazo HTTP 413 sin saturar Heap |
