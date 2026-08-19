-- Migración V16: Índices de rendimiento para consultas y filtros frecuentes

-- Índices para autenticación y filtrado de usuarios
CREATE INDEX IF NOT EXISTS idx_usuarios_email_rol ON auth.usuarios(email, rol);
CREATE INDEX IF NOT EXISTS idx_usuarios_rol_estado ON auth.usuarios(rol, activo, bloqueado);

-- Índices para módulo de candidatos
CREATE INDEX IF NOT EXISTS idx_candidatos_estado ON candidatos.candidatos(estado);
CREATE INDEX IF NOT EXISTS idx_candidatos_cargo ON candidatos.candidatos(cargo);
CREATE INDEX IF NOT EXISTS idx_candidatos_rrhh_email ON candidatos.candidatos(rrhh_email);

-- Índices para calendario y entrevistas
CREATE INDEX IF NOT EXISTS idx_eventos_fecha_rrhh ON calendario.eventos(fecha, rrhh_id);
CREATE INDEX IF NOT EXISTS idx_eventos_candidato ON calendario.eventos(candidato_id);

-- Índices para auditoría y bitácora de actividades
CREATE INDEX IF NOT EXISTS idx_actividades_fecha ON admin.actividades(fecha DESC);

-- Índices para notificaciones
CREATE INDEX IF NOT EXISTS idx_notificaciones_leida_fecha ON notificaciones.notificaciones(leida, fecha DESC);
CREATE INDEX IF NOT EXISTS idx_notificaciones_candidato ON notificaciones.notificaciones(candidato_id);
