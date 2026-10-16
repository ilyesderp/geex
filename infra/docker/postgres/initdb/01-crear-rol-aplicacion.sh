#!/bin/bash
# Crea el rol con el que se conecta la aplicación.
#
# - Las migraciones (Flyway) se ejecutan con el usuario administrador.
# - La aplicación usa "geex_app": sin privilegios de superusuario y SIN BYPASSRLS,
#   de modo que las políticas de Row Level Security de PostgreSQL se le aplican siempre.
#
# La contraseña llega por variable de entorno (generada por scripts/setup.*).
set -euo pipefail

: "${GEEX_DB_APP_PASSWORD:?Falta la variable GEEX_DB_APP_PASSWORD}"

psql -v ON_ERROR_STOP=1 \
     --username "$POSTGRES_USER" \
     --dbname "$POSTGRES_DB" \
     -v app_password="$GEEX_DB_APP_PASSWORD" <<'EOSQL'
CREATE ROLE geex_app LOGIN PASSWORD :'app_password'
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
GRANT USAGE ON SCHEMA public TO geex_app;
EOSQL
