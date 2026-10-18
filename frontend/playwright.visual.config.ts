import { defineConfig, devices } from '@playwright/test';

/**
 * Pruebas visuales y de accesibilidad (`e2e/visual`): la compilación de producción servida en local, con la
 * API simulada con datos fijos. Se ejecutan SIEMPRE dentro de la imagen oficial de Playwright para Linux
 * (`npm run e2e:visual`, ver `e2e/visual/ejecutar.mjs`), para que las capturas sean idénticas en cualquier
 * equipo y en CI.
 */
export default defineConfig({
  testDir: './e2e/visual',
  testMatch: '**/*.visual.ts',
  snapshotPathTemplate: '{testDir}/referencias/{projectName}/{arg}{ext}',
  fullyParallel: true,
  forbidOnly: !!process.env['CI'],
  reporter: process.env['CI'] ? [['list'], ['html', { open: 'never', outputFolder: 'playwright-report-visual' }]] : 'list',
  outputDir: 'test-results-visual',
  expect: {
    toHaveScreenshot: { animations: 'disabled', caret: 'hide', maxDiffPixels: 20 },
  },
  use: {
    baseURL: 'http://localhost:4300',
    locale: 'es-ES',
    timezoneId: 'Europe/Madrid',
  },
  webServer: {
    command: 'node e2e/visual/servidor-estatico.mjs',
    url: 'http://localhost:4300',
    reuseExistingServer: false,
  },
  projects: [
    { name: 'escritorio', use: { ...devices['Desktop Chrome'], viewport: { width: 1366, height: 800 }, deviceScaleFactor: 1 } },
    {
      name: 'movil-125',
      use: { ...devices['Desktop Chrome'], viewport: { width: 390, height: 844 }, deviceScaleFactor: 1.25, isMobile: true, hasTouch: true },
    },
  ],
});
