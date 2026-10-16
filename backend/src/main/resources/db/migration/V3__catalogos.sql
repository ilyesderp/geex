-- ─────────────────────────────────────────────────────────────────────────────
-- Listas desplegables parametrizables ("el programa quiero que sea totalmente
-- parametrizable"). Qué listas existen lo define el código (TipoCatalogo); sus
-- valores son de cada despacho, que puede añadir, renombrar, reordenar y desactivar.
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE valores_catalogo
(
    id              uuid PRIMARY KEY,
    despacho_id     uuid         NOT NULL REFERENCES despachos (id),
    catalogo_codigo varchar(50)  NOT NULL,
    codigo          varchar(60)  NOT NULL,
    etiqueta        varchar(200) NOT NULL,
    orden           integer      NOT NULL,
    activo          boolean      NOT NULL DEFAULT true,
    sistema         boolean      NOT NULL DEFAULT false,
    version         bigint       NOT NULL DEFAULT 0,
    CONSTRAINT valores_catalogo_codigo_uk UNIQUE (despacho_id, catalogo_codigo, codigo)
);
CREATE INDEX valores_catalogo_lista_idx ON valores_catalogo (despacho_id, catalogo_codigo, orden);

ALTER TABLE valores_catalogo ENABLE ROW LEVEL SECURITY;
ALTER TABLE valores_catalogo FORCE ROW LEVEL SECURITY;
CREATE POLICY valores_catalogo_aislamiento ON valores_catalogo
    USING (geex_modo_sistema() OR despacho_id = geex_despacho_actual())
    WITH CHECK (geex_modo_sistema() OR despacho_id = geex_despacho_actual());
