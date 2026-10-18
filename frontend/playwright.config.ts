import { defineConfig, devices } from '@playwright/test';

/**
 * Pruebas de extremo a extremo: la aplicación completa (frontend + backend + PostgreSQL + Mailpit) en un
 * navegador real. Necesitan una instalación vacía de GEEX (la primera prueba hace la instalación), así que
 * se ejecutan en CI con un entorno nuevo, o en local contra una base de datos recién creada.
 *
 *   GEEX_E2E_URL      URL del frontend (por defecto http://localhost:4200)
 *   GEEX_E2E_MAILPIT  API de Mailpit para leer los correos (por defecto http://localhost:8025)
 */
export default defineConfig({
  testDir: './e2e',
  testMatch: '**/*.e2e.ts',
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 1 : 0,
  timeout: 60_000,
  reporter: process.env['CI'] ? [['list'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: process.env['GEEX_E2E_URL'] ?? 'http://localhost:4200',
    locale: 'es-ES',
    timezoneId: 'Europe/Madrid',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
});
