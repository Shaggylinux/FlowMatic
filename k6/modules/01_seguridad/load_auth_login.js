import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { BASE_URL, TEST_USERS } from '../../config/env.js';
import { getCsrfToken } from '../../utils/auth.js';
import { generateSummaryReports } from '../../utils/reporter.js';

// Métricas personalizadas
const serverErrorsRate = new Rate('server_5xx_errors');

export const options = {
    scenarios: {
        sustained_load_login: {
            executor: 'ramping-vus',
            startVUs: 10,
            stages: [
                { duration: '30s', target: 200 }, // Rampa de subida a 200 usuarios
                { duration: '5m', target: 200 },  // Carga sostenida de 200 VUs por 5 minutos
                { duration: '30s', target: 0 },   // Rampa de bajada
            ],
            gracefulRampDown: '30s',
        },
    },
    thresholds: {
        // Latencia percentil 95 (p95) <= 1.2 segundos (1200 ms)
        'http_req_duration{name:POST_Login}': ['p(95)<=1200'],
        // Tasa de errores HTTP 5xx = 0.00%
        'server_5xx_errors': ['rate==0.00'],
        // Éxito de los checks generales
        'checks': ['rate>=0.99'],
    },
};

export default function () {
    // 1. Obtener la página de login para extraer CSRF Token y cookie JSESSIONID
    const { csrfToken } = getCsrfToken(BASE_URL);

    // 2. Realizar petición de autenticación POST /login
    const payload = {
        email: TEST_USERS.ADMIN.email,
        clave: TEST_USERS.ADMIN.clave,
        _csrf: csrfToken,
    };

    const params = {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-XSRF-TOKEN': csrfToken,
        },
        redirects: 0,
        tags: { name: 'POST_Login' },
    };

    const res = http.post(`${BASE_URL}/login`, payload, params);

    // Registrar si hubo error 5xx de servidor
    serverErrorsRate.add(res.status >= 500);

    // Validar respuesta
    check(res, {
        'Status es 302 Redirección o 200': (r) => r.status === 302 || r.status === 200,
        'No contiene mensaje de credenciales inválidas': (r) => {
            const loc = r.headers['Location'] || '';
            return !loc.includes('error=true');
        },
        'No es error 5xx': (r) => r.status < 500,
    });

    sleep(1);
}

export function handleSummary(data) {
    return generateSummaryReports(data, 'Modulo 1 - Carga Login Sostenido (200 VUs)');
}
