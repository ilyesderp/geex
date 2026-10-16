# Configuracion inicial del entorno local de GEEX (Windows / PowerShell).
#
# - Crea el archivo .env (ignorado por git) con secretos aleatorios generados en este equipo.
# - Nunca sobrescribe un .env existente.
# - Activa los git hooks del repositorio (deteccion de secretos antes de cada commit).
$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
$envPath = Join-Path $raiz '.env'

function Nuevo-Secreto([int]$bytes) {
    $buffer = New-Object byte[] $bytes
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($buffer)
    return [Convert]::ToBase64String($buffer)
}

function Nueva-Contrasena([int]$bytes) {
    # Base64 "seguro para URL" sin relleno, valido en cadenas de conexion
    return (Nuevo-Secreto $bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

if (Test-Path $envPath) {
    Write-Host "El archivo .env ya existe; no se modifica." -ForegroundColor Yellow
} else {
    $contenido = @(
        "# Generado por scripts/setup.ps1 - NO subir a git",
        "GEEX_DB_NAME=geex",
        "GEEX_DB_ADMIN_USER=geex_admin",
        "GEEX_DB_ADMIN_PASSWORD=$(Nueva-Contrasena 24)",
        "GEEX_DB_APP_PASSWORD=$(Nueva-Contrasena 24)",
        "GEEX_JWT_SECRET=$(Nuevo-Secreto 48)",
        "GEEX_FRONTEND_URL=http://localhost:4200"
    )
    [System.IO.File]::WriteAllLines($envPath, $contenido, (New-Object System.Text.UTF8Encoding $false))
    Write-Host "Creado .env con secretos aleatorios." -ForegroundColor Green
}

git -C $raiz config core.hooksPath .githooks
Write-Host "Git hooks activados (.githooks)." -ForegroundColor Green

if (-not (Get-Command gitleaks -ErrorAction SilentlyContinue)) {
    Write-Host "Aviso: gitleaks no esta instalado. Instalalo para bloquear secretos antes de cada commit:" -ForegroundColor Yellow
    Write-Host "  winget install --id Gitleaks.Gitleaks" -ForegroundColor Yellow
}
