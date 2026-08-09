CREATE SCHEMA IF NOT EXISTS public;
CREATE TABLE IF NOT EXISTS public.event_publication (
    id UUID NOT NULL,
    listener_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    serialized_event VARCHAR(255) NOT NULL,
    publication_date TIMESTAMP NOT NULL,
    completion_date TIMESTAMP,
    PRIMARY KEY (id, listener_id)
);
