import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, TEST_USERS } from '../../config/env.js';
import { authenticate } from '../../utils/auth.js';
import { generateSummaryReports } from '../../utils/reporter.js';

export const options = {
    scenarios: {
        concurrent_filter_search: {
            executor: 'ramping-vus',
            startVUs: 10,
            stages: [
                { duration: '30s', target: 150 }, // Subida a 150 usuarios concurrentes
                { duration: '3m', target: 150 },  // Carga sostenida durante 3 minutos
                { duration: '30s', target: 0 },   // Bajada
            ],
            gracefulRampDown: '30s',
        },
    },
    thresholds: {
        // Latencia percentil 95 (p95) <= 1.5 segundos (1500 ms)
        'http_req_duration{name:GET_Gestion_Candidatos_Filtro}': ['p(95)<=1500'],
        'http_req_failed': ['rate<0.01'],
        'checks': ['rate>=0.95'],
    },
};

export function setup() {
    // Autenticarse como RRHH para asegurar sesión válida
    authenticate(BASE_URL, TEST_USERS.RRHH.email, TEST_USERS.RRHH.clave);
}

export default function () {
    // Autenticar cada VU si no tiene sesión activa
    const auth = authenticate(BASE_URL, TEST_USERS.RRHH.email, TEST_USERS.RRHH.clave);

    if (auth.success) {
        // Petición con filtros simultáneos: Java, Medellín, Senior, paginación de 10 registros
        const url = `${BASE_URL}/gestion-candidatos?search=Java&ciudad=Medellin&cargo=Senior&page=0&size=10`;

        const res = http.get(url, {
            tags: { name: 'GET_Gestion_Candidatos_Filtro' },
        });

        check(res, {
            'Status es 200 OK': (r) => r.status === 200,
            'HTML renderiza tabla de candidatos': (r) => r.body && r.body.includes('candidato'),
            'Respuesta no vacía': (r) => r.body && r.body.length > 500,
        });
    }

    sleep(1);
}

export function handleSummary(data) {
    return generateSummaryReports(data, 'Modulo 2 - Carga Busqueda y Filtros Candidatos (150 VUs)');
}
