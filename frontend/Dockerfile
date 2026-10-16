# syntax=docker/dockerfile:1
# Imagen del frontend de GEEX (Angular servido por nginx). Se construye desde la raíz:
#   docker build -f frontend/Dockerfile -t geex-frontend .

# ── 1. Compilación ──────────────────────────────────────────────────────────
FROM node:22-bookworm AS construccion
# El generador del cliente OpenAPI necesita Java
RUN apt-get update \
    && apt-get install -y --no-install-recommends default-jre-headless \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

# ── 2. Servidor web ─────────────────────────────────────────────────────────
FROM nginx:1.29-alpine
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY frontend/nginx/cabeceras-seguridad.conf /etc/nginx/cabeceras-seguridad.conf
COPY --from=construccion /app/dist/geex-frontend/browser /usr/share/nginx/html
EXPOSE 80
