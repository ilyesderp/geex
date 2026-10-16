#!/usr/bin/env bash
# Configuración inicial del entorno local de GEEX (Linux / macOS / Git Bash).
#
# - Crea el archivo .env (ignorado por git) con secretos aleatorios generados en este equipo.
# - Nunca sobrescribe un .env existente.
# - Activa los git hooks del repositorio (detección de secretos antes de cada commit).
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$RAIZ/.env"

secreto() { openssl rand -base64 "$1" | tr -d '\n'; }
contrasena() { openssl rand -base64 "$1" | tr -d '\n=' | tr '+/' '-_'; }

if [[ -f "$ENV_FILE" ]]; then
  echo "El archivo .env ya existe; no se modifica."
else
  umask 077
  cat > "$ENV_FILE" <<EOF
# Generado por scripts/setup.sh — NO subir a git
GEEX_DB_NAME=geex
GEEX_DB_ADMIN_USER=geex_admin
GEEX_DB_ADMIN_PASSWORD=$(contrasena 24)
GEEX_DB_APP_PASSWORD=$(contrasena 24)
GEEX_JWT_SECRET=$(secreto 48)
GEEX_FRONTEND_URL=http://localhost:4200
EOF
  echo "Creado .env con secretos aleatorios."
fi

git -C "$RAIZ" config core.hooksPath .githooks
echo "Git hooks activados (.githooks)."

if ! command -v gitleaks >/dev/null 2>&1; then
  echo "Aviso: gitleaks no está instalado. Instálalo para bloquear secretos antes de cada commit."
fi
