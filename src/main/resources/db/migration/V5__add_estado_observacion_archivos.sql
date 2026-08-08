ALTER TABLE drive.archivos
ADD COLUMN estado_documento VARCHAR(255) DEFAULT 'Pendiente',
ADD COLUMN observacion TEXT;
