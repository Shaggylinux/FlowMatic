import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';
import { BASE_URL, TEST_USERS } from '../../config/env.js';
import { authenticate, generateDummyFile } from '../../utils/auth.js';
import { generateSummaryReports } from '../../utils/reporter.js';

const successfulValidUploads = new Counter('valid_uploads_29_5mb');
const rejectedOversizedUploads = new Counter('rejected_oversized_45mb');
const server5xxErrors = new Rate('server_5xx_errors');

export const options = {
    scenarios: {
        // Escenario A: 25 subidas simultáneas en el límite permitido (29.5 MB)
        allowed_limit_uploads: {
            executor: 'per-vu-iterations',
            vus: 25,
            iterations: 1,
            maxDuration: '3m',
            exec: 'testAllowedLimit',
        },
        // Escenario B: Intento de subida de archivo que sobrepasa el límite (45 MB)
        oversized_upload_rejection: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 1,
            startTime: '2s',
            maxDuration: '1m',
            exec: 'testOversizedLimit',
        },
    },
    thresholds: {
        'valid_uploads_29_5mb': ['count==25'],     // 25 subidas de 29.5MB completadas con éxito
        'rejected_oversized_45mb': ['count==1'],    // Archivo de 45MB rechazado con HTTP 413
        'server_5xx_errors': ['rate==0.00'],        // 0 errores 5xx de servidor
    },
};

export function testAllowedLimit() {
    const auth = authenticate(BASE_URL, TEST_USERS.RRHH.email, TEST_USERS.RRHH.clave);
    if (!auth.success) {
        return;
    }

    // Generar archivo en el límite permitido: 29.5 MB
    const file29MB = generateDummyFile(29.5, `archivo_limite_${__VU}.zip`, 'application/zip');

    const formData = {
        archivo: file29MB,
        folder: '',
        _csrf: auth.csrfToken,
    };

    const params = {
        headers: {
            'X-XSRF-TOKEN': auth.csrfToken,
        },
        redirects: 0,
        tags: { name: 'POST_Upload_29MB_Allowed' },
        timeout: '180s',
    };

    const res = http.post(`${BASE_URL}/drive/subir-archivo`, formData, params);

    const isSuccess = (res.status === 302 || res.status === 200);
    if (isSuccess) {
        successfulValidUploads.add(1);
    }
    if (res.status >= 500) {
        server5xxErrors.add(1);
    } else {
        server5xxErrors.add(0);
    }

    check(res, {
        'Archivo 29.5MB subido exitosamente (Status 302 o 200)': () => isSuccess,
    });
}

export function testOversizedLimit() {
    const auth = authenticate(BASE_URL, TEST_USERS.RRHH.email, TEST_USERS.RRHH.clave);
    if (!auth.success) {
        return;
    }

    // Generar archivo que sobrepasa el límite de 30MB: 45 MB
    const file45MB = generateDummyFile(45.0, 'archivo_excedido_45mb.zip', 'application/zip');

    const formData = {
        archivo: file45MB,
        folder: '',
        _csrf: auth.csrfToken,
    };

    const params = {
        headers: {
            'X-XSRF-TOKEN': auth.csrfToken,
        },
        redirects: 0,
        tags: { name: 'POST_Upload_45MB_Rejected' },
        timeout: '60s',
    };

    const res = http.post(`${BASE_URL}/drive/subir-archivo`, formData, params);

    // Se espera rechazo HTTP 413 (Payload Too Large)
    const is413 = (res.status === 413);
    if (is413) {
        rejectedOversizedUploads.add(1);
    }
    if (res.status >= 500) {
        server5xxErrors.add(1);
    } else {
        server5xxErrors.add(0);
    }

    check(res, {
        'Rechazo inmediato con HTTP 413 (Payload Too Large)': () => is413,
    });
}

export function handleSummary(data) {
    return generateSummaryReports(data, 'Modulo 4 - Estres Limite de Capacidad (30 MB) y Sobrecarga (45 MB)');
}
