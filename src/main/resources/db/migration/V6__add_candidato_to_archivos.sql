ALTER TABLE drive.archivos ADD COLUMN IF NOT EXISTS candidato_id bigint;
ALTER TABLE drive.archivos DROP CONSTRAINT IF EXISTS fk_archivos_candidato;
ALTER TABLE drive.archivos ADD CONSTRAINT fk_archivos_candidato FOREIGN KEY (candidato_id) REFERENCES auth.usuarios(id);
