// Servidor estático mínimo para las pruebas visuales: sirve la compilación de producción de Angular y
// devuelve index.html para cualquier ruta de la aplicación (como hace nginx en producción).
import { createReadStream, existsSync, statSync } from 'node:fs';
import { createServer } from 'node:http';
import { extname, join, normalize } from 'node:path';

const RAIZ = new URL('../../dist/geex-frontend/browser/', import.meta.url).pathname.replace(/^\/([A-Za-z]:)/, '$1');
const PUERTO = Number(process.env['PUERTO'] ?? 4300);
const TIPOS = {
  '.html': 'text/html; charset=utf-8', '.js': 'text/javascript', '.css': 'text/css', '.svg': 'image/svg+xml',
  '.woff2': 'font/woff2', '.woff': 'font/woff', '.ttf': 'font/ttf', '.png': 'image/png', '.ico': 'image/x-icon',
  '.json': 'application/json', '.webmanifest': 'application/manifest+json',
};

if (!existsSync(join(RAIZ, 'index.html'))) {
  console.error(`No existe la compilación en ${RAIZ}: ejecuta antes «npx ng build».`);
  process.exit(1);
}

createServer((peticion, respuesta) => {
  const ruta = normalize(decodeURIComponent(new URL(peticion.url, 'http://x').pathname)).replace(/^([/\\])+/, '');
  let archivo = join(RAIZ, ruta);
  if (!archivo.startsWith(RAIZ) || !existsSync(archivo) || statSync(archivo).isDirectory()) {
    archivo = join(RAIZ, 'index.html');
  }
  respuesta.writeHead(200, { 'Content-Type': TIPOS[extname(archivo)] ?? 'application/octet-stream' });
  createReadStream(archivo).pipe(respuesta);
}).listen(PUERTO, () => console.log(`Servidor de pruebas visuales en http://localhost:${PUERTO}`));
