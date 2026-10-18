-- Row Level Security también en las tablas de identidad que no pertenecen a un único despacho.
--
-- Hasta ahora usuarios, sesiones_refresco y tokens_un_uso solo se protegían en el código. Con estas
-- políticas, un fallo en una consulta tampoco puede leer ni modificar datos de otros despachos:
--   - usuarios: un despacho ve y modifica solo a sus miembros (un usuario puede trabajar en varios).
--     Puede dar de alta usuarios nuevos (invitaciones); borrar, solo en modo sistema.
--   - sesiones_refresco y tokens_un_uso: solo las filas del despacho actual. Los enlaces de
--     restablecimiento de contraseña no tienen despacho, así que solo se ven en modo sistema
--     (inicio de sesión, renovación y enlaces ya se ejecutan en ese modo).

-- ── usuarios ─────────────────────────────────────────────────────────────────
ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuarios FORCE ROW LEVEL SECURITY;

CREATE POLICY usuarios_lectura ON usuarios FOR SELECT
    USING (geex_modo_sistema()
        OR EXISTS (SELECT 1
                   FROM membresias m
                   WHERE m.usuario_id = usuarios.id
                     AND m.despacho_id = geex_despacho_actual()));

CREATE POLICY usuarios_alta ON usuarios FOR INSERT
    WITH CHECK (geex_modo_sistema() OR geex_despacho_actual() IS NOT NULL);

CREATE POLICY usuarios_modificacion ON usuarios FOR UPDATE
    USING (geex_modo_sistema()
        OR EXISTS (SELECT 1
                   FROM membresias m
                   WHERE m.usuario_id = usuarios.id
                     AND m.despacho_id = geex_despacho_actual()))
    WITH CHECK (geex_modo_sistema()
        OR EXISTS (SELECT 1
                   FROM membresias m
                   WHERE m.usuario_id = usuarios.id
                     AND m.despacho_id = geex_despacho_actual()));

CREATE POLICY usuarios_borrado ON usuarios FOR DELETE
    USING (geex_modo_sistema());

-- ── sesiones_refresco ───────────────────────────────────────────────────────
ALTER TABLE sesiones_refresco ENABLE ROW LEVEL SECURITY;
ALTER TABLE sesiones_refresco FORCE ROW LEVEL SECURITY;

CREATE POLICY sesiones_refresco_aislamiento ON sesiones_refresco
    USING (geex_modo_sistema() OR despacho_id = geex_despacho_actual())
    WITH CHECK (geex_modo_sistema() OR despacho_id = geex_despacho_actual());

-- ── tokens_un_uso ────────────────────────────────────────────────────────────
ALTER TABLE tokens_un_uso ENABLE ROW LEVEL SECURITY;
ALTER TABLE tokens_un_uso FORCE ROW LEVEL SECURITY;

CREATE POLICY tokens_un_uso_aislamiento ON tokens_un_uso
    USING (geex_modo_sistema() OR despacho_id = geex_despacho_actual())
    WITH CHECK (geex_modo_sistema() OR despacho_id = geex_despacho_actual());
