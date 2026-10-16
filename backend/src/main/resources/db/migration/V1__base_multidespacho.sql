-- ─────────────────────────────────────────────────────────────────────────────
-- Base técnica: permisos del rol de aplicación, funciones de Row Level Security
-- y tabla del registro de publicación de eventos (Spring Modulith).
--
-- Las migraciones se ejecutan con el usuario administrador. La aplicación se
-- conecta como "geex_app" (sin BYPASSRLS), creado en infra/docker/postgres/initdb.
-- ─────────────────────────────────────────────────────────────────────────────

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO geex_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO geex_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO geex_app;

-- pgvector: se usará en la fase del asistente legal (RAG)
CREATE EXTENSION IF NOT EXISTS vector;

-- Despacho de la petición actual (variable de sesión fijada por la aplicación en cada conexión)
CREATE FUNCTION geex_despacho_actual() RETURNS uuid
    LANGUAGE sql STABLE
AS $$ SELECT NULLIF(current_setting('app.despacho_id', true), '')::uuid $$;

-- Modo sistema: operaciones internas (inicio de sesión, instalación, tareas programadas)
CREATE FUNCTION geex_modo_sistema() RETURNS boolean
    LANGUAGE sql STABLE
AS $$ SELECT coalesce(current_setting('app.sistema', true), '') = 'on' $$;

-- Registro de publicación de eventos de Spring Modulith (esquema v2 para PostgreSQL)
CREATE TABLE event_publication
(
    id                     UUID                     NOT NULL,
    listener_id            TEXT                     NOT NULL,
    event_type             TEXT                     NOT NULL,
    serialized_event       TEXT                     NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP WITH TIME ZONE,
    status                 TEXT,
    completion_attempts    INT,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);
CREATE INDEX event_publication_serialized_event_hash_idx ON event_publication USING hash (serialized_event);
CREATE INDEX event_publication_by_completion_date_idx ON event_publication (completion_date);
