-- ─────────────────────────────────────────────────────────────────────────────
-- Despachos (tenants) e identidad: usuarios, membresías, permisos por rol,
-- tokens de un solo uso (invitación / restablecimiento) y sesiones de refresco.
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE despachos
(
    id                 uuid PRIMARY KEY,
    nombre             varchar(150) NOT NULL,
    razon_social       varchar(200),
    nif                varchar(20),
    direccion          varchar(250),
    codigo_postal      varchar(10),
    municipio          varchar(120) NOT NULL,
    provincia          varchar(120) NOT NULL,
    comunidad_autonoma varchar(30)  NOT NULL,
    telefono           varchar(30),
    email              varchar(254),
    web                varchar(250),
    activo             boolean      NOT NULL DEFAULT true,
    creado_en          timestamptz  NOT NULL,
    actualizado_en     timestamptz  NOT NULL,
    version            bigint       NOT NULL DEFAULT 0
);

ALTER TABLE despachos ENABLE ROW LEVEL SECURITY;
ALTER TABLE despachos FORCE ROW LEVEL SECURITY;
CREATE POLICY despachos_aislamiento ON despachos
    USING (geex_modo_sistema() OR id = geex_despacho_actual())
    WITH CHECK (geex_modo_sistema() OR id = geex_despacho_actual());

-- Los usuarios son globales: una misma persona puede trabajar en varios despachos.
CREATE TABLE usuarios
(
    id                uuid PRIMARY KEY,
    email             varchar(254) NOT NULL,
    nombre            varchar(100) NOT NULL,
    apellidos         varchar(150) NOT NULL,
    hash_contrasena   varchar(255),
    superadmin        boolean      NOT NULL DEFAULT false,
    activo            boolean      NOT NULL DEFAULT true,
    intentos_fallidos integer      NOT NULL DEFAULT 0,
    bloqueado_hasta   timestamptz,
    version_token     integer      NOT NULL DEFAULT 0,
    ultimo_acceso     timestamptz,
    creado_en         timestamptz  NOT NULL,
    version           bigint       NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX usuarios_email_uk ON usuarios (lower(email));

CREATE TABLE membresias
(
    id          uuid PRIMARY KEY,
    usuario_id  uuid        NOT NULL REFERENCES usuarios (id),
    despacho_id uuid        NOT NULL REFERENCES despachos (id),
    rol         varchar(30) NOT NULL,
    activa      boolean     NOT NULL DEFAULT true,
    creada_en   timestamptz NOT NULL,
    ultimo_uso  timestamptz,
    version     bigint      NOT NULL DEFAULT 0,
    CONSTRAINT membresias_usuario_despacho_uk UNIQUE (usuario_id, despacho_id)
);
CREATE INDEX membresias_despacho_idx ON membresias (despacho_id);

ALTER TABLE membresias ENABLE ROW LEVEL SECURITY;
ALTER TABLE membresias FORCE ROW LEVEL SECURITY;
CREATE POLICY membresias_aislamiento ON membresias
    USING (geex_modo_sistema() OR despacho_id = geex_despacho_actual())
    WITH CHECK (geex_modo_sistema() OR despacho_id = geex_despacho_actual());

CREATE TABLE permisos_rol
(
    despacho_id uuid        NOT NULL REFERENCES despachos (id),
    rol         varchar(30) NOT NULL,
    permiso     varchar(60) NOT NULL,
    PRIMARY KEY (despacho_id, rol, permiso)
);

ALTER TABLE permisos_rol ENABLE ROW LEVEL SECURITY;
ALTER TABLE permisos_rol FORCE ROW LEVEL SECURITY;
CREATE POLICY permisos_rol_aislamiento ON permisos_rol
    USING (geex_modo_sistema() OR despacho_id = geex_despacho_actual())
    WITH CHECK (geex_modo_sistema() OR despacho_id = geex_despacho_actual());

-- Solo se guarda el hash SHA-256 del token; el valor en claro solo viaja en el correo.
CREATE TABLE tokens_un_uso
(
    id          uuid PRIMARY KEY,
    usuario_id  uuid        NOT NULL REFERENCES usuarios (id),
    despacho_id uuid REFERENCES despachos (id),
    tipo        varchar(30) NOT NULL,
    token_hash  varchar(64) NOT NULL UNIQUE,
    expira_en   timestamptz NOT NULL,
    usado_en    timestamptz,
    creado_en   timestamptz NOT NULL
);
CREATE INDEX tokens_un_uso_usuario_idx ON tokens_un_uso (usuario_id, tipo);

-- Tokens de refresco con rotación: cada uso crea uno nuevo de la misma familia.
-- Si un token ya usado vuelve a presentarse (robo), se revoca toda la familia.
CREATE TABLE sesiones_refresco
(
    id            uuid PRIMARY KEY,
    familia_id    uuid        NOT NULL,
    usuario_id    uuid        NOT NULL REFERENCES usuarios (id),
    despacho_id   uuid        NOT NULL REFERENCES despachos (id),
    token_hash    varchar(64) NOT NULL UNIQUE,
    version_token integer     NOT NULL,
    expira_en     timestamptz NOT NULL,
    usado_en      timestamptz,
    revocado_en   timestamptz,
    creado_en     timestamptz NOT NULL,
    ip            varchar(64),
    agente        varchar(300)
);
CREATE INDEX sesiones_refresco_familia_idx ON sesiones_refresco (familia_id);
CREATE INDEX sesiones_refresco_usuario_idx ON sesiones_refresco (usuario_id);
