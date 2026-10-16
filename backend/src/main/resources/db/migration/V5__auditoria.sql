-- ─────────────────────────────────────────────────────────────────────────────
-- Registro de auditoría: quién hizo qué y cuándo (cumplimiento LOPD/RGPD).
-- Es de solo inserción: la aplicación nunca actualiza ni borra registros.
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE registros_auditoria
(
    id           uuid PRIMARY KEY,
    despacho_id  uuid REFERENCES despachos (id),
    actor_id     uuid,
    accion       varchar(60)  NOT NULL,
    descripcion  varchar(500) NOT NULL,
    entidad_tipo varchar(60),
    entidad_id   varchar(60),
    ocurrido_en  timestamptz  NOT NULL
);
CREATE INDEX registros_auditoria_despacho_fecha_idx ON registros_auditoria (despacho_id, ocurrido_en DESC);

ALTER TABLE registros_auditoria ENABLE ROW LEVEL SECURITY;
ALTER TABLE registros_auditoria FORCE ROW LEVEL SECURITY;
CREATE POLICY registros_auditoria_aislamiento ON registros_auditoria
    USING (geex_modo_sistema() OR despacho_id = geex_despacho_actual())
    WITH CHECK (geex_modo_sistema() OR despacho_id = geex_despacho_actual());

-- Solo inserción y lectura para la aplicación
REVOKE UPDATE, DELETE ON registros_auditoria FROM geex_app;
