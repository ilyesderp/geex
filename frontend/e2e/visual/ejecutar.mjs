// Ejecuta las pruebas visuales dentro de la imagen oficial de Playwright (Linux), igual que en CI, para
// que las capturas de referencia no dependan del sistema operativo ni de las fuentes del equipo.
//
//   npm run e2e:visual                          comparar con las referencias
//   npm run e2e:visual -- --update-snapshots    regenerar las referencias (revisar el cambio en el PR)
//
// Requiere Docker y una compilación previa (`npx ng build`).
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

// Misma versión que @playwright/test en package.json, fijada por digest
const IMAGEN = 'mcr.microsoft.com/playwright:v1.63.0-noble@sha256:eff16c30e6f3f4af0a03fa4b706120d5e9b0891c344a27d64559aff5900a4a27';
const FRONTEND = fileURLToPath(new URL('../../', import.meta.url));

const resultado = spawnSync('docker', [
  'run', '--rm', '--ipc=host',
  '-e', `CI=${process.env['CI'] ?? ''}`,
  '-v', `${FRONTEND}:/work`,
  '-w', '/work',
  IMAGEN,
  'npx', 'playwright', 'test', '-c', 'playwright.visual.config.ts', ...process.argv.slice(2),
], { stdio: 'inherit' });

process.exit(resultado.status ?? 1);
