import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, TEST_USERS } from '../../config/env.js';
import { authenticate, getCsrfToken, generateDummyFile } from '../../utils/auth.js';
import { generateSummaryReports } from '../../utils/reporter.js';

export const options = {
    scenarios: {
        sustained_upload_io: {
            executor: 'ramping-vus',
            startVUs: 5,
            stages: [
                { duration: '20s', target: 50 }, // Subir a 50 usuarios concurrentes
                { duration: '2m', target: 50 },  // Carga continua de 50 subidas de PDFs (3-5 MB)
                { duration: '20s', target: 0 },   // Rampa descendente
            ],
            gracefulRampDown: '30s',
        },
    },
    thresholds: {
        // Latencia percentil 95 (p95) <= 2.5 segundos (2500 ms)
        'http_req_duration{name:POST_Drive_Upload_PDF}': ['p(95)<=2500'],
        'http_req_failed': ['rate<0.02'],
    },
};

export default function () {
    const auth = authenticate(BASE_URL, TEST_USERS.RRHH.email, TEST_USERS.RRHH.clave);
    const { csrfToken } = getCsrfToken(BASE_URL);

    if (auth.success) {
        // Generar archivo PDF de 3.5 MB para simular documento de candidato
        const dummyPdf = generateDummyFile(3.5, `curriculum_${__VU}_${__ITER}.pdf`, 'application/pdf');

        const formData = {
            archivo: dummyPdf,
            folder: '',
            candidatoId: '1',
            _csrf: csrfToken,
        };

        const params = {
            headers: {
                'X-XSRF-TOKEN': csrfToken,
            },
            redirects: 0,
            tags: { name: 'POST_Drive_Upload_PDF' },
            timeout: '30s',
        };

        const res = http.post(`${BASE_URL}/drive/subir-archivo`, formData, params);

        check(res, {
            'Subida exitosa (Status 302 Redirigido a Drive o 200)': (r) => r.status === 302 || r.status === 200,
            'No es error 5xx': (r) => r.status < 500,
        });
    }

    sleep(2);
}

export function handleSummary(data) {
    return generateSummaryReports(data, 'Modulo 4 - Carga IO Subida de Archivos PDF (50 VUs)');
}
