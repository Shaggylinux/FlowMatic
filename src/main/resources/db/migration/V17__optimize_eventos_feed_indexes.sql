-- Migración V17: Índices compuestos de alto rendimiento para el feed del calendario y consultas de eventos

-- Índice compuesto para rango de fechas y ordenamiento horario sin Sort en memoria
CREATE INDEX IF NOT EXISTS idx_eventos_fecha_hora ON calendario.eventos(fecha ASC, hora ASC);

-- Índice para filtrado por fecha y estado de entrevista
CREATE INDEX IF NOT EXISTS idx_eventos_fecha_estado ON calendario.eventos(fecha, estado);

-- Índices para búsqueda directa de eventos por candidato y reclutador con orden cronológico
CREATE INDEX IF NOT EXISTS idx_eventos_candidato_fecha_hora ON calendario.eventos(candidato_id, fecha ASC, hora ASC);
CREATE INDEX IF NOT EXISTS idx_eventos_rrhh_fecha_hora ON calendario.eventos(rrhh_id, fecha ASC, hora ASC);
