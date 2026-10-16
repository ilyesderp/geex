import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { InstalacionService } from '../../api/generated';
import { Permiso, SesionService } from './sesion.service';

/*
 * Nota: inject() solo funciona de forma síncrona dentro del guard. Todas las
 * dependencias se obtienen antes del primer `await`.
 */

let instalado: boolean | null = null;

/** Si GEEX aún no está instalado, todo lleva al asistente de instalación. */
export const exigirInstalado: CanActivateFn = async () => {
  const api = inject(InstalacionService);
  const router = inject(Router);
  if (instalado === null) {
    try {
      instalado = (await firstValueFrom(api.estado())).instalado;
    } catch {
      instalado = true; // si el servidor no responde, la pantalla de login mostrará el error
    }
  }
  return instalado ? true : router.createUrlTree(['/instalacion']);
};

export const soloSinInstalar: CanActivateFn = async () => {
  const api = inject(InstalacionService);
  const router = inject(Router);
  const estado = await firstValueFrom(api.estado());
  instalado = estado.instalado;
  return estado.instalado ? router.createUrlTree(['/login']) : true;
};

export function marcarInstalado(): void {
  instalado = true;
}

/** Rutas privadas: exige sesión (o la recupera con la cookie de refresco). */
export const exigirSesion: CanActivateFn = async (_ruta, estado) => {
  const sesion = inject(SesionService);
  const router = inject(Router);
  if (instalado === false) {
    return router.createUrlTree(['/instalacion']);
  }
  if (!sesion.autenticado()) {
    await sesion.restaurar();
  }
  return sesion.autenticado() ? true : router.createUrlTree(['/login'], { queryParams: { volver: estado.url } });
};

/** Rutas de acceso (login...): si ya hay sesión, va al inicio. */
export const soloSinSesion: CanActivateFn = async () => {
  const sesion = inject(SesionService);
  const router = inject(Router);
  if (!sesion.autenticado()) {
    await sesion.restaurar();
  }
  return sesion.autenticado() ? router.createUrlTree(['/']) : true;
};

/** Exige un permiso concreto (el servidor lo comprueba igualmente; esto solo evita pantallas vacías). */
export function exigirPermiso(permiso: Permiso): CanActivateFn {
  return () => inject(SesionService).tienePermiso(permiso) || inject(Router).createUrlTree(['/sin-permiso']);
}
