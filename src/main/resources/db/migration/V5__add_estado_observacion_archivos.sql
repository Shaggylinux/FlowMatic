ALTER TABLE drive.archivos
ADD COLUMN IF NOT EXISTS estado_documento VARCHAR(255) DEFAULT 'Pendiente',
ADD COLUMN IF NOT EXISTS observacion TEXT;
