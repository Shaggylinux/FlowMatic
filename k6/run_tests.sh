#!/bin/bash

# ==============================================================================
# FlowMatic - Suite de Pruebas de Carga (Nivel 3) y Estrés (Nivel 4) con k6
# ==============================================================================

set -e

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPORTS_DIR="${BASE_DIR}/reports"
mkdir -p "${REPORTS_DIR}"

export BASE_URL="${BASE_URL:-http://localhost:8080}"
export ADMIN_EMAIL="${ADMIN_EMAIL:-admin@flowmatic.com}"
export ADMIN_PASSWORD="${ADMIN_PASSWORD:-12345678}"
export RRHH_EMAIL="${RRHH_EMAIL:-rrhh@flowmatic.com}"
export RRHH_PASSWORD="${RRHH_PASSWORD:-12345678}"

# Colores para consola
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}====================================================${NC}"
echo -e "${BLUE}        FlowMatic - Performance & Quality Gates      ${NC}"
echo -e "${BLUE}====================================================${NC}"
echo -e "Objetivo: ${YELLOW}${BASE_URL}${NC}"
echo -e "Reportes: ${YELLOW}${REPORTS_DIR}${NC}\n"

# Comprobar que k6 está instalado
if ! command -v k6 &> /dev/null; then
    echo -e "${RED}[ERROR] 'k6' no está instalado en el sistema.${NC}"
    echo -e "Por favor instálalo ejecutando: sudo apt-get update && sudo apt-get install k6"
    echo -e "O descarga el binario desde https://k6.io/docs/get-started/installation/"
    exit 1
fi

run_test() {
    local test_file=$1
    local test_title=$2
    echo -e "\n${YELLOW}>>> Ejecutando: ${test_title}...${NC}"
    echo -e "Archivo: ${test_file}"
    
    # Iniciar monitor en segundo plano de procesador (CPU) y memoria
    python3 "${BASE_DIR}/utils/system_monitor.py" --start
    
    # Ejecutar prueba con k6
    k6 run "${test_file}"
    
    # Detener monitor e inyectar datos de procesador en el reporte HTML y JSON
    python3 "${BASE_DIR}/utils/system_monitor.py" --stop
    python3 "${BASE_DIR}/utils/system_monitor.py" --inject "${REPORTS_DIR}"
    
    echo -e "${GREEN}✓ Finalizado: ${test_title}${NC}"
}

case "$1" in
    # Módulo 1
    "load-auth")
        run_test "${BASE_DIR}/modules/01_seguridad/load_auth_login.js" "Modulo 1 - Carga Sostenida /login (200 VUs / 5 min)"
        ;;
    "stress-auth")
        run_test "${BASE_DIR}/modules/01_seguridad/stress_auth_bruteforce.js" "Modulo 1 - Estres Spike Fuerza Bruta (1,000 reqs / 10s)"
        ;;
    # Módulo 2
    "load-candidatos")
        run_test "${BASE_DIR}/modules/02_candidatos/load_candidatos_busqueda.js" "Modulo 2 - Carga Busqueda y Filtros Dinamicos (150 VUs)"
        ;;
    "stress-candidatos"|"stress-reportes"|"stress-excel")
        run_test "${BASE_DIR}/modules/02_candidatos/stress_reporte_excel.js" "Modulo 2 - Estres Generacion Masiva Reportes (50 descargas simultaneas x 5000 registros)"
        ;;
    # Módulo 3
    "load-calendario")
        run_test "${BASE_DIR}/modules/03_calendario/load_calendario_feed.js" "Modulo 3 - Carga Feed Calendario JSON (300 Concurrencia)"
        ;;
    "stress-calendario"|"stress-carrera"|"stress-race"|"stress-agendamiento")
        run_test "${BASE_DIR}/modules/03_calendario/stress_race_condition.js" "Modulo 3 - Estres Condicion de Carrera Agendamiento (2 VUs Simultaneos)"
        ;;
    # Módulo 4
    "load-drive")
        run_test "${BASE_DIR}/modules/04_drive/load_drive_upload.js" "Modulo 4 - Carga IO Subida Continua PDFs (50 VUs)"
        ;;
    "stress-drive"|"stress-limites"|"stress-sobrecarga"|"stress-upload")
        run_test "${BASE_DIR}/modules/04_drive/stress_drive_limites.js" "Modulo 4 - Estres Limites 29.5MB y Sobrecarga 45MB"
        ;;
    # Suites combinadas
    "load-all")
        echo -e "${BLUE}=== Ejecutando Suite Completa de Pruebas de Carga (Nivel 3) ===${NC}"
        run_test "${BASE_DIR}/modules/01_seguridad/load_auth_login.js" "Modulo 1 - Carga Login"
        run_test "${BASE_DIR}/modules/02_candidatos/load_candidatos_busqueda.js" "Modulo 2 - Carga Candidatos"
        run_test "${BASE_DIR}/modules/03_calendario/load_calendario_feed.js" "Modulo 3 - Carga Calendario"
        run_test "${BASE_DIR}/modules/04_drive/load_drive_upload.js" "Modulo 4 - Carga Drive IO"
        ;;
    "stress-all")
        echo -e "${BLUE}=== Ejecutando Suite Completa de Pruebas de Estres (Nivel 4) ===${NC}"
        run_test "${BASE_DIR}/modules/01_seguridad/stress_auth_bruteforce.js" "Modulo 1 - Estres Fuerza Bruta"
        run_test "${BASE_DIR}/modules/02_candidatos/stress_reporte_excel.js" "Modulo 2 - Estres Memoria Excel"
        run_test "${BASE_DIR}/modules/03_calendario/stress_race_condition.js" "Modulo 3 - Estres Condicion Carrera"
        run_test "${BASE_DIR}/modules/04_drive/stress_drive_limites.js" "Modulo 4 - Estres Limites Drive"
        ;;
    "all")
        echo -e "${BLUE}=== Ejecutando Suite Completa (Carga + Estres) ===${NC}"
        run_test "${BASE_DIR}/modules/01_seguridad/load_auth_login.js" "Modulo 1 - Carga Login"
        run_test "${BASE_DIR}/modules/01_seguridad/stress_auth_bruteforce.js" "Modulo 1 - Estres Fuerza Bruta"
        run_test "${BASE_DIR}/modules/02_candidatos/load_candidatos_busqueda.js" "Modulo 2 - Carga Candidatos"
        run_test "${BASE_DIR}/modules/02_candidatos/stress_reporte_excel.js" "Modulo 2 - Estres Memoria Excel"
        run_test "${BASE_DIR}/modules/03_calendario/load_calendario_feed.js" "Modulo 3 - Carga Calendario"
        run_test "${BASE_DIR}/modules/03_calendario/stress_race_condition.js" "Modulo 3 - Estres Condicion Carrera"
        run_test "${BASE_DIR}/modules/04_drive/load_drive_upload.js" "Modulo 4 - Carga Drive IO"
        run_test "${BASE_DIR}/modules/04_drive/stress_drive_limites.js" "Modulo 4 - Estres Limites Drive"
        ;;
    *)
        echo "Uso: ./k6/run_tests.sh [opcion]"
        echo ""
        echo "Opciones individuales:"
        echo "  load-auth           : Carga Nivel 3 - /login (200 VUs / 5 min)"
        echo "  stress-auth         : Estrés Nivel 4 - Spike Fuerza Bruta (1000 reqs / 10s)"
        echo "  load-candidatos     : Carga Nivel 3 - Búsqueda y Filtros Dinámicos (150 VUs)"
        echo "  stress-candidatos   : Estrés Nivel 4 - Memoria JVM Exportación Excel (50 VUs)"
        echo "  load-calendario     : Carga Nivel 3 - Feed Calendario JSON (300 Concurrencia)"
        echo "  stress-calendario   : Estrés Nivel 4 - Condición de Carrera en Agendamiento"
        echo "  load-drive          : Carga Nivel 3 - Subida Continua PDFs (50 VUs)"
        echo "  stress-drive        : Estrés Nivel 4 - Cuotas (29.5MB) y Sobrecarga (45MB)"
        echo ""
        echo "Suites grupales:"
        echo "  load-all            : Ejecutar todas las pruebas de carga (Nivel 3)"
        echo "  stress-all          : Ejecutar todas las pruebas de estrés (Nivel 4)"
        echo "  all                 : Ejecutar suite completa (Nivel 3 + Nivel 4)"
        exit 1
        ;;
esac

echo -e "\n${GREEN}====================================================${NC}"
echo -e "${GREEN}✓ Ejecución completada. Reportes generados en:${NC}"
echo -e "${YELLOW}${REPORTS_DIR}${NC}"
echo -e "${GREEN}====================================================${NC}"
