-- ─────────────────────────────────────────────────────────────────────────────
-- Calendario de días inhábiles para el cómputo de plazos administrativos
-- (art. 30 Ley 39/2015). Sábados y domingos son siempre inhábiles y no se guardan.
--
-- - Nacionales y autonómicos: globales (despacho_id NULL), visibles para todos.
-- - Locales: de cada despacho (dos festivos locales por municipio).
--
-- Datos de 2026: Resolución de 18 de noviembre de 2025 de la Secretaría de Estado
-- de Función Pública (BOE-A-2025-23702), tomados del conjunto de datos abiertos
-- plazos-extranjeria-es (MIT, © 2026 Luis Durá Esteve). 2027 es provisional:
-- solo festivos nacionales fijos y Viernes Santo hasta que se publique el BOE.
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE dias_inhabiles
(
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    despacho_id        uuid REFERENCES despachos (id),
    ambito             varchar(20)  NOT NULL,
    comunidad_autonoma varchar(30),
    fecha              date         NOT NULL,
    descripcion        varchar(200) NOT NULL,
    provisional        boolean      NOT NULL DEFAULT false,
    creado_en          timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT dias_inhabiles_ambito_ck CHECK (
        (ambito = 'NACIONAL' AND despacho_id IS NULL AND comunidad_autonoma IS NULL)
        OR (ambito = 'AUTONOMICO' AND despacho_id IS NULL AND comunidad_autonoma IS NOT NULL)
        OR (ambito = 'LOCAL' AND despacho_id IS NOT NULL))
);
CREATE UNIQUE INDEX dias_inhabiles_nacional_uk ON dias_inhabiles (fecha) WHERE ambito = 'NACIONAL';
CREATE UNIQUE INDEX dias_inhabiles_autonomico_uk ON dias_inhabiles (comunidad_autonoma, fecha) WHERE ambito = 'AUTONOMICO';
CREATE UNIQUE INDEX dias_inhabiles_local_uk ON dias_inhabiles (despacho_id, fecha) WHERE ambito = 'LOCAL';

ALTER TABLE dias_inhabiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE dias_inhabiles FORCE ROW LEVEL SECURITY;
-- Lectura: los globales los ven todos; los locales, solo su despacho
CREATE POLICY dias_inhabiles_lectura ON dias_inhabiles FOR SELECT
    USING (despacho_id IS NULL OR geex_modo_sistema() OR despacho_id = geex_despacho_actual());
-- Escritura: solo los del propio despacho (los globales, en modo sistema)
CREATE POLICY dias_inhabiles_escritura ON dias_inhabiles FOR ALL
    USING (geex_modo_sistema() OR despacho_id = geex_despacho_actual())
    WITH CHECK (geex_modo_sistema() OR despacho_id = geex_despacho_actual());

-- Festivos locales conocidos por municipio: se copian al despacho al crearlo.
CREATE TABLE festivos_locales_referencia
(
    municipio   varchar(120) NOT NULL,
    provincia   varchar(120) NOT NULL,
    fecha       date         NOT NULL,
    descripcion varchar(200) NOT NULL,
    PRIMARY KEY (municipio, provincia, fecha)
);

INSERT INTO dias_inhabiles (ambito, comunidad_autonoma, fecha, descripcion, provisional) VALUES
    ('NACIONAL', NULL, DATE '2026-01-01', 'Año Nuevo', false),
    ('NACIONAL', NULL, DATE '2026-01-06', 'Epifanía del Señor', false),
    ('NACIONAL', NULL, DATE '2026-04-03', 'Viernes Santo', false),
    ('NACIONAL', NULL, DATE '2026-05-01', 'Fiesta del Trabajo', false),
    ('NACIONAL', NULL, DATE '2026-10-12', 'Fiesta Nacional de España', false),
    ('NACIONAL', NULL, DATE '2026-12-08', 'Inmaculada Concepción', false),
    ('NACIONAL', NULL, DATE '2026-12-25', 'Natividad del Señor', false),
    ('NACIONAL', NULL, DATE '2027-01-01', 'Año Nuevo', true),
    ('NACIONAL', NULL, DATE '2027-01-06', 'Epifanía del Señor', true),
    ('NACIONAL', NULL, DATE '2027-03-26', 'Viernes Santo', true),
    ('NACIONAL', NULL, DATE '2027-10-12', 'Fiesta Nacional de España', true),
    ('NACIONAL', NULL, DATE '2027-11-01', 'Todos los Santos', true),
    ('NACIONAL', NULL, DATE '2027-12-06', 'Día de la Constitución Española', true),
    ('NACIONAL', NULL, DATE '2027-12-08', 'Inmaculada Concepción', true),
    ('AUTONOMICO', 'ANDALUCIA', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'ANDALUCIA', DATE '2026-11-02', 'Lunes siguiente a Todos los Santos', false),
    ('AUTONOMICO', 'ANDALUCIA', DATE '2026-12-07', 'Lunes siguiente al Día de la Constitución', false),
    ('AUTONOMICO', 'ARAGON', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'ARAGON', DATE '2026-04-23', 'San Jorge, Día de Aragón', false),
    ('AUTONOMICO', 'ARAGON', DATE '2026-11-02', 'Lunes siguiente a Todos los Santos', false),
    ('AUTONOMICO', 'ARAGON', DATE '2026-12-07', 'Lunes siguiente al Día de la Constitución', false),
    ('AUTONOMICO', 'ASTURIAS', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'ASTURIAS', DATE '2026-09-08', 'Día de Asturias', false),
    ('AUTONOMICO', 'ASTURIAS', DATE '2026-11-02', 'Lunes siguiente a Todos los Santos', false),
    ('AUTONOMICO', 'ASTURIAS', DATE '2026-12-07', 'Lunes siguiente al Día de la Constitución', false),
    ('AUTONOMICO', 'BALEARES', DATE '2026-03-02', 'Lunes siguiente al Día de las Illes Balears', false),
    ('AUTONOMICO', 'BALEARES', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'BALEARES', DATE '2026-04-06', 'Lunes de Pascua', false),
    ('AUTONOMICO', 'CANARIAS', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'CANARIAS', DATE '2026-11-02', 'Lunes siguiente a Todos los Santos', false),
    ('AUTONOMICO', 'CANTABRIA', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'CANTABRIA', DATE '2026-07-28', 'Día de las Instituciones de Cantabria', false),
    ('AUTONOMICO', 'CANTABRIA', DATE '2026-09-15', 'La Bien Aparecida', false),
    ('AUTONOMICO', 'CANTABRIA', DATE '2026-12-07', 'Lunes siguiente al Día de la Constitución', false),
    ('AUTONOMICO', 'CASTILLA_LA_MANCHA', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'CASTILLA_LA_MANCHA', DATE '2026-04-06', 'Lunes de Pascua', false),
    ('AUTONOMICO', 'CASTILLA_LA_MANCHA', DATE '2026-06-04', 'Corpus Christi', false),
    ('AUTONOMICO', 'CASTILLA_LA_MANCHA', DATE '2026-11-02', 'Lunes siguiente a Todos los Santos', false),
    ('AUTONOMICO', 'CASTILLA_Y_LEON', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'CASTILLA_Y_LEON', DATE '2026-04-23', 'Fiesta de Castilla y León', false),
    ('AUTONOMICO', 'CASTILLA_Y_LEON', DATE '2026-11-02', 'Lunes siguiente a Todos los Santos', false),
    ('AUTONOMICO', 'CASTILLA_Y_LEON', DATE '2026-12-07', 'Lunes siguiente al Día de la Constitución', false),
    ('AUTONOMICO', 'CATALUNA', DATE '2026-04-06', 'Lunes de Pascua', false),
    ('AUTONOMICO', 'CATALUNA', DATE '2026-06-24', 'San Juan', false),
    ('AUTONOMICO', 'CATALUNA', DATE '2026-09-11', 'Diada Nacional de Cataluña', false),
    ('AUTONOMICO', 'COMUNITAT_VALENCIANA', DATE '2026-03-19', 'San José', false),
    ('AUTONOMICO', 'COMUNITAT_VALENCIANA', DATE '2026-04-06', 'Lunes de Pascua', false),
    ('AUTONOMICO', 'COMUNITAT_VALENCIANA', DATE '2026-06-24', 'San Juan', false),
    ('AUTONOMICO', 'COMUNITAT_VALENCIANA', DATE '2026-10-09', 'Día de la Comunitat Valenciana', false),
    ('AUTONOMICO', 'EXTREMADURA', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'EXTREMADURA', DATE '2026-09-08', 'Día de Extremadura', false),
    ('AUTONOMICO', 'EXTREMADURA', DATE '2026-11-02', 'Lunes siguiente a Todos los Santos', false),
    ('AUTONOMICO', 'EXTREMADURA', DATE '2026-12-07', 'Lunes siguiente al Día de la Constitución', false),
    ('AUTONOMICO', 'GALICIA', DATE '2026-03-19', 'San José', false),
    ('AUTONOMICO', 'GALICIA', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'GALICIA', DATE '2026-06-24', 'San Juan', false),
    ('AUTONOMICO', 'MADRID', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'MADRID', DATE '2026-11-02', 'Lunes siguiente a Todos los Santos', false),
    ('AUTONOMICO', 'MADRID', DATE '2026-12-07', 'Lunes siguiente al Día de la Constitución', false),
    ('AUTONOMICO', 'MURCIA', DATE '2026-03-19', 'San José', false),
    ('AUTONOMICO', 'MURCIA', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'MURCIA', DATE '2026-06-09', 'Día de la Región de Murcia', false),
    ('AUTONOMICO', 'MURCIA', DATE '2026-12-07', 'Lunes siguiente al Día de la Constitución', false),
    ('AUTONOMICO', 'NAVARRA', DATE '2026-03-19', 'San José', false),
    ('AUTONOMICO', 'NAVARRA', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'NAVARRA', DATE '2026-04-06', 'Lunes de Pascua', false),
    ('AUTONOMICO', 'NAVARRA', DATE '2026-11-02', 'Lunes siguiente a Todos los Santos', false),
    ('AUTONOMICO', 'PAIS_VASCO', DATE '2026-03-19', 'San José', false),
    ('AUTONOMICO', 'PAIS_VASCO', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'PAIS_VASCO', DATE '2026-04-06', 'Lunes de Pascua', false),
    ('AUTONOMICO', 'LA_RIOJA', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'LA_RIOJA', DATE '2026-04-06', 'Lunes de Pascua', false),
    ('AUTONOMICO', 'LA_RIOJA', DATE '2026-06-09', 'Día de La Rioja', false),
    ('AUTONOMICO', 'LA_RIOJA', DATE '2026-12-07', 'Lunes siguiente al Día de la Constitución', false),
    ('AUTONOMICO', 'CEUTA', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'CEUTA', DATE '2026-05-27', 'Fiesta del Sacrificio (Eid al-Adha)', false),
    ('AUTONOMICO', 'CEUTA', DATE '2026-08-05', 'Nuestra Señora de África', false),
    ('AUTONOMICO', 'CEUTA', DATE '2026-09-02', 'Día de Ceuta', false),
    ('AUTONOMICO', 'MELILLA', DATE '2026-03-20', 'Fiesta del Fin del Ramadán (Eid al-Fitr)', false),
    ('AUTONOMICO', 'MELILLA', DATE '2026-04-02', 'Jueves Santo', false),
    ('AUTONOMICO', 'MELILLA', DATE '2026-05-27', 'Fiesta del Sacrificio (Eid al-Adha)', false),
    ('AUTONOMICO', 'MELILLA', DATE '2026-12-07', 'Lunes siguiente al Día de la Constitución', false);

-- Alicante 2026: Santa Faz (16 de abril) y Hogueras (23 de junio), según el calendario laboral del Ayuntamiento.
INSERT INTO festivos_locales_referencia (municipio, provincia, fecha, descripcion) VALUES
    ('Alicante', 'Alicante', DATE '2026-04-16', 'Santa Faz'),
    ('Alicante', 'Alicante', DATE '2026-06-23', 'Hogueras de San Juan');
