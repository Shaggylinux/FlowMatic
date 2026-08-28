-- ====================================================================
-- V18: Garantizar un único Administrador semilla oficial (Flyway)
-- ====================================================================

-- 1. Eliminar administradores secundarios de prueba si existen
DELETE FROM admin.administradores WHERE id IN (SELECT id FROM auth.usuarios WHERE email = 'admin@sistema.com');
DELETE FROM auth.usuarios WHERE email = 'admin@sistema.com';

-- 2. Insertar o actualizar el Administrador canónico
DO $$
DECLARE
    v_admin_id BIGINT;
BEGIN
    -- Asegurar registro en auth.usuarios
    IF EXISTS (SELECT 1 FROM auth.usuarios WHERE email = 'admin@flowmatic.com') THEN
        UPDATE auth.usuarios
        SET clave = '$2a$10$Sx38iLXa152avD8kynpgaetFpibsdg5oRa5bgJ/CziBvrCwtgfq1.',
            rol = 'ROLE_ADMINISTRADOR',
            activo = TRUE,
            bloqueado = FALSE
        WHERE email = 'admin@flowmatic.com'
        RETURNING id INTO v_admin_id;
    ELSE
        INSERT INTO auth.usuarios (email, clave, rol, activo, bloqueado)
        VALUES ('admin@flowmatic.com', '$2a$10$Sx38iLXa152avD8kynpgaetFpibsdg5oRa5bgJ/CziBvrCwtgfq1.', 'ROLE_ADMINISTRADOR', TRUE, FALSE)
        RETURNING id INTO v_admin_id;
    END IF;

    -- Asegurar perfil en admin.administradores
    IF NOT EXISTS (SELECT 1 FROM admin.administradores WHERE id = v_admin_id) THEN
        INSERT INTO admin.administradores (id, username, apellido)
        VALUES (v_admin_id, 'Admin', 'FlowMatic');
    ELSE
        UPDATE admin.administradores
        SET username = 'Admin',
            apellido = 'FlowMatic'
        WHERE id = v_admin_id;
    END IF;

    -- Resetear intentos de login fallidos previos
    DELETE FROM seguridad.login_attempts WHERE email = 'admin@flowmatic.com';
END $$;
