// Configuración centralizada de variables de entorno y usuarios de prueba
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const TEST_USERS = {
    ADMIN: {
        email: __ENV.ADMIN_EMAIL || 'admin@flowmatic.com',
        clave: __ENV.ADMIN_PASSWORD || '12345678'
    },
    RRHH: {
        email: __ENV.RRHH_EMAIL || 'rrhh@flowmatic.com',
        clave: __ENV.RRHH_PASSWORD || '12345678'
    },
    RRHH2: {
        email: __ENV.RRHH2_EMAIL || 'rrhh2@flowmatic.com',
        clave: __ENV.RRHH2_PASSWORD || '12345678'
    },
    CANDIDATO: {
        email: __ENV.CANDIDATO_EMAIL || 'candidato_1@flowmatic-report.com',
        clave: __ENV.CANDIDATO_PASSWORD || '12345678'
    }
};

export const DEFAULT_TIMEOUTS = {
    connect: '10s',
    request: '30s'
};
