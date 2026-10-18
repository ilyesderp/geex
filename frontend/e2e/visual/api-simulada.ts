import type { Page } from '@playwright/test';
import * as demo from './datos-demo';

/**
 * Sustituye el backend por respuestas fijas (datos de demostración) para que las capturas solo cambien
 * cuando cambie el diseño. `conSesion: false` simula a alguien que aún no ha iniciado sesión.
 */
export async function simularApi(page: Page, { conSesion }: { conSesion: boolean }): Promise<void> {
  await page.route('**/api/**', (ruta) => {
    const camino = new URL(ruta.request().url()).pathname;
    const json = (cuerpo: unknown) => ruta.fulfill({ json: cuerpo });
    switch (true) {
      case camino === '/api/instalacion':
        return json({ instalado: true });
      case camino === '/api/auth/refresh':
        return conSesion
          ? json({ tokenAcceso: 'token-demo', expiraEn: '2099-01-01T00:00:00Z' })
          : ruta.fulfill({ status: 401, json: { codigo: 'SESION_NO_VALIDA' } });
      case camino === '/api/yo':
        return json(demo.perfil);
      case camino === '/api/despacho':
        return json(demo.despacho);
      case camino === '/api/usuarios':
        return json(demo.usuarios);
      case camino === '/api/roles':
        return json(demo.matrizRoles);
      case camino === '/api/roles/permisos':
        return json(demo.catalogoPermisos);
      case camino === '/api/catalogos':
        return json(demo.catalogos);
      case /^\/api\/catalogos\/[A-Z_]+\/valores$/.test(camino):
        return json(demo.valores);
      case /^\/api\/calendario\/\d{4}$/.test(camino):
        return json(demo.calendario);
      case camino === '/api/auditoria':
        return json(demo.auditoria);
      default:
        return ruta.fulfill({ status: 404, json: { codigo: 'NO_SIMULADO', detail: camino } });
    }
  });
}
