import http from 'k6/http';
import { check } from 'k6';
import { Rate } from 'k6/metrics';
import { BASE_URL } from '../../config/env.js';
import { getCsrfToken } from '../../utils/auth.js';
import { generateSummaryReports } from '../../utils/reporter.js';

const serverErrorsRate = new Rate('server_5xx_errors');

export const options = {
    scenarios: {
        bruteforce_spike: {
            executor: 'shared-iterations',
            vus: 50,              // 50 VUs concurrentes generando la ráfaga masiva
            iterations: 1000,     // Total de 1,000 peticiones de login incorrectas
            maxDuration: '10s',   // Intervalo de 10 segundos
        },
    },
    thresholds: {
        // Ningún fallo HTTP 5xx durante la ráfaga súbita (Tomcat y JVM estables)
        'server_5xx_errors': ['rate==0.00'],
        'http_req_failed{status:500}': ['rate==0.00'],
        'checks': ['rate>=0.99'],
    },
};

export default function () {
    const { csrfToken } = getCsrfToken(BASE_URL);

    // Ataque de fuerza bruta dirigido a cuentas objetivo simuladas con contraseñas erróneas
    const targetIdx = __VU % 5;
    const targetEmail = `target_victim_${targetIdx}@flowmatic-test.com`;
    const randomSuffix = Math.floor(Math.random() * 1000000);
    const payload = {
        email: targetEmail,
        clave: `ClaveInvalida_${randomSuffix}!`,
        _csrf: csrfToken,
    };

    const params = {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-XSRF-TOKEN': csrfToken,
        },
        redirects: 0,
        tags: { name: 'POST_BruteForce_Spike' },
    };

    const res = http.post(`${BASE_URL}/login`, payload, params);

    serverErrorsRate.add(res.status >= 500);

    check(res, {
        'Respuesta manejada (302 a error/bloqueo o 200)': (r) => r.status === 302 || r.status === 200,
        'Sin caída de servidor 5xx (Tomcat y JVM estables)': (r) => r.status < 500,
        'Defensa activa contra fuerza bruta': (r) => {
            const loc = r.headers['Location'] || '';
            return loc.includes('bloqueado') || loc.includes('error');
        },
    });
}

export function handleSummary(data) {
    return generateSummaryReports(data, 'Modulo 1 - Estres Spike Fuerza Bruta (1000 reqs en 10s)');
}
