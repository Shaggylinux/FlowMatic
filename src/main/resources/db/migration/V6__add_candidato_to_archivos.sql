ALTER TABLE drive.archivos ADD COLUMN candidato_id bigint;
ALTER TABLE drive.archivos ADD CONSTRAINT fk_archivos_candidato FOREIGN KEY (candidato_id) REFERENCES auth.usuarios(id);
