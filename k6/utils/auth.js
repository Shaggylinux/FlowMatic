import http from 'k6/http';
import { check } from 'k6';

/**
 * Obtiene el token CSRF y la cookie de sesión inicial desde /login
 */
export function getCsrfToken(baseUrl) {
    const res = http.get(`${baseUrl}/login`, {
        tags: { name: 'GET_Login_Page' }
    });

    let csrfToken = '';

    // 1. Intentar obtener de la cookie XSRF-TOKEN
    const jar = http.cookieJar();
    const cookies = jar.cookiesForURL(baseUrl);
    if (cookies['XSRF-TOKEN'] && cookies['XSRF-TOKEN'].length > 0) {
        csrfToken = cookies['XSRF-TOKEN'][0];
    }

    // 2. Si no viene en cookie, extraerlo del input hidden del HTML
    if (!csrfToken && res.body) {
        const match = res.body.match(/name=["']_csrf["']\s+value=["']([^"']+)["']/i) ||
                      res.body.match(/value=["']([^"']+)["']\s+name=["']_csrf["']/i);
        if (match && match[1]) {
            csrfToken = match[1];
        }
    }

    return {
        csrfToken: csrfToken,
        response: res
    };
}

/**
 * Realiza el inicio de sesión contra /login y retorna el estado
 */
export function authenticate(baseUrl, email, password) {
    const { csrfToken } = getCsrfToken(baseUrl);

    const payload = {
        email: email,
        clave: password,
        _csrf: csrfToken
    };

    const params = {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-XSRF-TOKEN': csrfToken
        },
        redirects: 0, // No seguir redirecciones automáticas para validar el 302 hacia el dashboard
        tags: { name: 'POST_Login' }
    };

    const loginRes = http.post(`${baseUrl}/login`, payload, params);

    const isSuccess = check(loginRes, {
        'Login status 302 o 200': (r) => r.status === 302 || r.status === 200,
        'Sin parametro de error': (r) => {
            const loc = r.headers['Location'] || '';
            return !loc.includes('error') && !loc.includes('blocked');
        }
    });

    let freshCsrfToken = csrfToken;
    if (isSuccess) {
        http.get(`${baseUrl}/calendario`, { tags: { name: 'GET_Refresh_CSRF' } });
        const jar = http.cookieJar();
        const cookies = jar.cookiesForURL(baseUrl);
        if (cookies['XSRF-TOKEN'] && cookies['XSRF-TOKEN'].length > 0) {
            freshCsrfToken = cookies['XSRF-TOKEN'][0];
        }
    }

    return {
        success: isSuccess,
        response: loginRes,
        csrfToken: freshCsrfToken
    };
}

/**
 * Genera un payload binario / texto simulado para pruebas de subida multipart
 */
export function generateDummyFile(sizeInMb, filename = 'documento_prueba.pdf', mimeType = 'application/pdf') {
    const bytesNeeded = Math.floor(sizeInMb * 1024 * 1024);
    // Para eficiencia de memoria en k6, repetimos un bloque de 1KB
    const chunk1KB = 'A'.repeat(1024);
    const repeats = Math.floor(bytesNeeded / 1024);
    const content = chunk1KB.repeat(repeats);

    return http.file(content, filename, mimeType);
}
