import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, TEST_USERS } from '../../config/env.js';
import { authenticate } from '../../utils/auth.js';
import { generateSummaryReports } from '../../utils/reporter.js';

export const options = {
    scenarios: {
        calendar_feed_load: {
            executor: 'ramping-arrival-rate',
            startRate: 50,
            timeUnit: '1s',
            preAllocatedVUs: 300,
            maxVUs: 400,
            stages: [
                { duration: '20s', target: 180 }, // Escalar hasta el throughput objetivo (>= 180 req/s)
                { duration: '2m', target: 200 },  // Mantener throughput de 180-200 req/s con 300 VUs
                { duration: '20s', target: 0 },   // Rampa descendente
            ],
        },
    },
    thresholds: {
        // Tiempo de respuesta promedio <= 250 ms
        'http_req_duration{name:GET_Calendario_Eventos}': ['avg<=250', 'p(95)<=500'],
        // Throughput >= 180 req/s
        'http_reqs': ['rate>=180'],
        // Tasa de error < 1%
        'http_req_failed': ['rate<0.01'],
    },
};

export function setup() {
    authenticate(BASE_URL, TEST_USERS.RRHH.email, TEST_USERS.RRHH.clave);
}

export default function () {
    const auth = authenticate(BASE_URL, TEST_USERS.RRHH.email, TEST_USERS.RRHH.clave);

    if (auth.success) {
        // Consultar eventos del mes actual para FullCalendar
        const start = '2026-08-01';
        const end = '2026-08-31';

        const res = http.get(`${BASE_URL}/calendario/eventos?start=${start}&end=${end}`, {
            tags: { name: 'GET_Calendario_Eventos' },
        });

        check(res, {
            'Status 200 OK': (r) => r.status === 200,
            'Respuesta es JSON Array': (r) => {
                try {
                    const data = JSON.parse(r.body);
                    return Array.isArray(data);
                } catch (e) {
                    return false;
                }
            },
        });
    }

    sleep(0.1);
}

export function handleSummary(data) {
    return generateSummaryReports(data, 'Modulo 3 - Carga Feed Calendario JSON (300 Concurrencia)');
}
