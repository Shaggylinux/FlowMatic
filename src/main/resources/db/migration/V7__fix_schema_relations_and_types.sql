-- V6: Añadir Llaves Foráneas y corregir tipo de dato de Historial

-- 1. Asegurar integridad referencial (Foreign Keys)
ALTER TABLE candidatos.candidatos DROP CONSTRAINT IF EXISTS fk_candidatos_usuarios;
ALTER TABLE candidatos.candidatos ADD CONSTRAINT fk_candidatos_usuarios FOREIGN KEY (id) REFERENCES auth.usuarios(id);

ALTER TABLE admin.rrhh DROP CONSTRAINT IF EXISTS fk_rrhh_usuarios;
ALTER TABLE admin.rrhh ADD CONSTRAINT fk_rrhh_usuarios FOREIGN KEY (id) REFERENCES auth.usuarios(id);

ALTER TABLE admin.administradores DROP CONSTRAINT IF EXISTS fk_administradores_usuarios;
ALTER TABLE admin.administradores ADD CONSTRAINT fk_administradores_usuarios FOREIGN KEY (id) REFERENCES auth.usuarios(id);

ALTER TABLE calendario.eventos DROP CONSTRAINT IF EXISTS fk_eventos_candidatos;
ALTER TABLE calendario.eventos ADD CONSTRAINT fk_eventos_candidatos FOREIGN KEY (candidato_id) REFERENCES candidatos.candidatos(id);

ALTER TABLE calendario.eventos DROP CONSTRAINT IF EXISTS fk_eventos_rrhh;
ALTER TABLE calendario.eventos ADD CONSTRAINT fk_eventos_rrhh FOREIGN KEY (rrhh_id) REFERENCES admin.rrhh(id);

ALTER TABLE notificaciones.notificaciones DROP CONSTRAINT IF EXISTS fk_notificaciones_candidatos;
ALTER TABLE notificaciones.notificaciones ADD CONSTRAINT fk_notificaciones_candidatos FOREIGN KEY (candidato_id) REFERENCES candidatos.candidatos(id);

-- 2. Corregir tipo de dato en el historial
-- Se usa USING para castear los valores existentes si los hubiera.
ALTER TABLE shared.historial 
    ALTER COLUMN fecha TYPE TIMESTAMP USING fecha::timestamp;
