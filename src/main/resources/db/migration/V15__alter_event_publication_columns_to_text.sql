-- Migración V15: Cambiar columnas de event_publication a TEXT para permitir eventos serializados de Spring Modulith
ALTER TABLE public.event_publication 
    ALTER COLUMN listener_id TYPE TEXT,
    ALTER COLUMN event_type TYPE TEXT,
    ALTER COLUMN serialized_event TYPE TEXT;
