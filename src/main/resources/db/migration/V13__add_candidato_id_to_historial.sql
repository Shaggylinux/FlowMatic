-- V13: Añadir candidato_id a shared.historial para trazabilidad de estados y auditoría

ALTER TABLE shared.historial ADD COLUMN IF NOT EXISTS candidato_id BIGINT;

ALTER TABLE shared.historial DROP CONSTRAINT IF EXISTS fk_historial_candidatos;
ALTER TABLE shared.historial ADD CONSTRAINT fk_historial_candidatos 
    FOREIGN KEY (candidato_id) REFERENCES candidatos.candidatos(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_historial_candidato ON shared.historial(candidato_id);
CREATE INDEX IF NOT EXISTS idx_historial_fecha ON shared.historial(fecha);
