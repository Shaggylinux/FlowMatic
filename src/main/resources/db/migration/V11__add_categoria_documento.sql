ALTER TABLE drive.archivos
ADD COLUMN IF NOT EXISTS categoria_documento VARCHAR(255) DEFAULT 'Requerido';
