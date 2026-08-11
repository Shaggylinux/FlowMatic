-- V12: Configurar ON DELETE CASCADE en Llaves Foráneas para permitir la eliminación limpia de Candidatos y Usuarios

ALTER TABLE drive.archivos DROP CONSTRAINT IF EXISTS fk_archivos_candidato;
ALTER TABLE drive.archivos ADD CONSTRAINT fk_archivos_candidato 
    FOREIGN KEY (candidato_id) REFERENCES auth.usuarios(id) ON DELETE CASCADE;

ALTER TABLE candidatos.candidatos DROP CONSTRAINT IF EXISTS fk_candidatos_usuarios;
ALTER TABLE candidatos.candidatos ADD CONSTRAINT fk_candidatos_usuarios 
    FOREIGN KEY (id) REFERENCES auth.usuarios(id) ON DELETE CASCADE;

ALTER TABLE calendario.eventos DROP CONSTRAINT IF EXISTS fk_eventos_candidatos;
ALTER TABLE calendario.eventos ADD CONSTRAINT fk_eventos_candidatos 
    FOREIGN KEY (candidato_id) REFERENCES candidatos.candidatos(id) ON DELETE CASCADE;

ALTER TABLE notificaciones.notificaciones DROP CONSTRAINT IF EXISTS fk_notificaciones_candidatos;
ALTER TABLE notificaciones.notificaciones ADD CONSTRAINT fk_notificaciones_candidatos 
    FOREIGN KEY (candidato_id) REFERENCES candidatos.candidatos(id) ON DELETE CASCADE;

ALTER TABLE admin.rrhh DROP CONSTRAINT IF EXISTS fk_rrhh_usuarios;
ALTER TABLE admin.rrhh ADD CONSTRAINT fk_rrhh_usuarios 
    FOREIGN KEY (id) REFERENCES auth.usuarios(id) ON DELETE CASCADE;

ALTER TABLE admin.administradores DROP CONSTRAINT IF EXISTS fk_administradores_usuarios;
ALTER TABLE admin.administradores ADD CONSTRAINT fk_administradores_usuarios 
    FOREIGN KEY (id) REFERENCES auth.usuarios(id) ON DELETE CASCADE;
