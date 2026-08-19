-- V14: Configurar ON DELETE CASCADE en Llave Foránea de Eventos hacia RRHH

ALTER TABLE calendario.eventos DROP CONSTRAINT IF EXISTS fk_eventos_rrhh;
ALTER TABLE calendario.eventos ADD CONSTRAINT fk_eventos_rrhh 
    FOREIGN KEY (rrhh_id) REFERENCES admin.rrhh(id) ON DELETE CASCADE;
