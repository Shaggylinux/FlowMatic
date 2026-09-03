import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';
import { BASE_URL, TEST_USERS } from '../../config/env.js';
import { authenticate } from '../../utils/auth.js';
import { generateSummaryReports } from '../../utils/reporter.js';

const successfulBookings = new Counter('successful_bookings');
const rejectedBookings = new Counter('rejected_bookings');
const raceHandlingSuccess = new Rate('race_handling_success');

export const options = {
    scenarios: {
        race_condition_booking: {
            executor: 'per-vu-iterations',
            vus: 2,              // Exactamente 2 reclutadores concurrentes
            iterations: 1,       // 1 intento por reclutador
            maxDuration: '30s',
        },
    },
    thresholds: {
        // Exactamente 1 cita debe ser creada exitosamente y 1 debe ser rechazada limpiamente
        'successful_bookings': ['count==1'],
        'rejected_bookings': ['count==1'],
        'race_handling_success': ['rate==1.00'],
        'http_req_failed': ['rate==0.00'], // Ambas peticiones deben responder HTTP 200 manejado
    },
};

export default function () {
    const isVU1 = (__VU === 1);
    const user = isVU1 ? TEST_USERS.RRHH : TEST_USERS.RRHH2;
    const candidatoId = isVU1 ? '10001' : '10002'; // Dos candidatos diferentes compitiendo por el MISMO evaluador

    // Autenticación de cada reclutador
    const auth = authenticate(BASE_URL, user.email, user.clave);
    if (!auth.success) {
        return;
    }

    const payload = {
        candidatoId: candidatoId,
        fecha: '2026-11-20',
        hora: '10:00:00',
        tipo: 'Entrevista Técnica',
        vacante: 'Desarrollador Java Senior',
        modalidad: 'VIRTUAL',
        lugar: 'https://meet.google.com/evaluacion-concurrente-live',
        entrevistador: 'Roberto Evaluador',
        observaciones: 'Evaluacion tecnica en vivo bajo prueba de condicion de carrera',
        estado: 'PENDIENTE',
        _csrf: auth.csrfToken,
    };

    const params = {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-XSRF-TOKEN': auth.csrfToken,
        },
        tags: { name: 'POST_Agendar_Entrevista_Race' },
    };

    // Disparo concurrente para agendar al mismo evaluador en el mismo slot exacto
    const res = http.post(`${BASE_URL}/calendario/crear`, payload, params);

    let isSuccess = false;
    let isRejected = false;
    let errorMsg = '';

    try {
        const body = JSON.parse(res.body);
        if (body.success === true) {
            isSuccess = true;
            successfulBookings.add(1);
            raceHandlingSuccess.add(1);
        } else if (body.success === false) {
            isRejected = true;
            errorMsg = body.error || '';
            rejectedBookings.add(1);
            raceHandlingSuccess.add(1);
        } else {
            raceHandlingSuccess.add(0);
        }
    } catch (e) {
        raceHandlingSuccess.add(0);
    }

    check(res, {
        'Status HTTP 200 con respuesta JSON': (r) => r.status === 200,
        'Respuesta controlada (1 cita creada o 1 rechazo limpio)': () => isSuccess || isRejected,
    });
}

export function handleSummary(data) {
    return generateSummaryReports(data, 'Modulo 3 - Estres Condicion de Carrera Agendamiento (2 VUs Simultaneos)');
}
