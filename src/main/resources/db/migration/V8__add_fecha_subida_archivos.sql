ALTER TABLE drive.archivos ADD COLUMN IF NOT EXISTS fecha_subida TIMESTAMP;

UPDATE drive.archivos SET fecha_subida = now() WHERE fecha_subida IS NULL;

UPDATE drive.archivos SET candidato_id = u.id
FROM auth.usuarios u
WHERE u.email = drive.archivos.propietario
  AND u.rol = 'ROLE_CANDIDATO'
  AND drive.archivos.candidato_id IS NULL;