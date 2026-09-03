import http from 'k6/http';
import { check } from 'k6';
import { Rate } from 'k6/metrics';
import { BASE_URL, TEST_USERS } from '../../config/env.js';
import { authenticate } from '../../utils/auth.js';
import { generateSummaryReports } from '../../utils/reporter.js';

const oomErrorRate = new Rate('jvm_oom_errors');

export const options = {
    scenarios: {
        concurrent_excel_exports: {
            executor: 'per-vu-iterations',
            vus: 50,              // 50 descargas simultáneas
            iterations: 1,        // 1 descarga masiva (5,000 registros) por cada usuario virtual
            maxDuration: '2m',
        },
    },
    thresholds: {
        'jvm_oom_errors': ['rate==0.00'],
        'http_req_failed': ['rate==0.00'],
        'checks': ['rate>=0.99'],
    },
};

export function setup() {
    // Autenticación inicial para obtener sesión autorizada
    const auth = authenticate(BASE_URL, TEST_USERS.RRHH.email, TEST_USERS.RRHH.clave);
    const jar = http.cookieJar();
    const cookies = jar.cookiesForURL(BASE_URL);
    let jsessionId = '';
    if (cookies['JSESSIONID'] && cookies['JSESSIONID'].length > 0) {
        jsessionId = cookies['JSESSIONID'][0];
    }
    return {
        success: auth.success,
        csrfToken: auth.csrfToken,
        jsessionId: jsessionId,
    };
}

export default function (data) {
    if (data && data.jsessionId) {
        const jar = http.cookieJar();
        jar.set(BASE_URL, 'JSESSIONID', data.jsessionId);
    } else {
        authenticate(BASE_URL, TEST_USERS.RRHH.email, TEST_USERS.RRHH.clave);
    }

    // Solicitar descarga simultánea del reporte consolidado de 5,000 registros
    const res = http.get(`${BASE_URL}/gestion-candidatos/export`, {
        responseType: 'binary',
        tags: { name: 'GET_Export_Excel_5000_Registros' },
        timeout: '120s',
    });

    const isOOM = res.status === 500;
    oomErrorRate.add(isOOM);

    const contentDisposition = (res.headers['Content-Disposition'] || res.headers['content-disposition'] || '');
    const byteSize = res.body ? (res.body.byteLength !== undefined ? res.body.byteLength : (res.body.length || 0)) : 0;

    check(res, {
        'Descarga exitosa HTTP 200': (r) => r.status === 200,
        'Content-Disposition es adjunto .xlsx': () => {
            return contentDisposition.includes('candidatos_reporte.xlsx') || contentDisposition.includes('attachment');
        },
        'Tamaño de reporte íntegro (5,000 registros > 100 KB)': () => byteSize > 100000,
        'Sin OutOfMemoryError en JVM (Heap estable)': (r) => !isOOM && r.status === 200,
    });
}

export function handleSummary(data) {
    return generateSummaryReports(data, 'Modulo 2 - Estres Generacion Masiva Reportes (50 descargas simultaneas x 5000 registros)');
}
