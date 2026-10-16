import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { SesionService } from './sesion.service';

/** Endpoints públicos: nunca llevan el token (uno caducado haría fallar la petición). */
const PUBLICOS = [
  '/api/auth/login',
  '/api/auth/refresh',
  '/api/auth/logout',
  '/api/instalacion',
  '/api/cuenta/enlaces/consultar',
  '/api/cuenta/activar',
  '/api/cuenta/recuperar',
  '/api/cuenta/restablecer',
];

function esPublico(url: string): boolean {
  const ruta = url.replace(/^https?:\/\/[^/]+/, '').split('?')[0];
  return PUBLICOS.some((publico) => ruta === publico || ruta.startsWith(publico + '/'));
}

function conToken(peticion: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
  return token ? peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : peticion;
}

/**
 * Añade el token de acceso a las llamadas a la API. Si el servidor responde 401
 * (token caducado), renueva la sesión una vez y repite la petición; si no se
 * puede renovar, lleva al usuario al inicio de sesión.
 */
export const autenticacionInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  if (!peticion.url.includes('/api/') || esPublico(peticion.url)) {
    return siguiente(peticion);
  }
  const sesion = inject(SesionService);
  return siguiente(conToken(peticion, sesion.token())).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
        return throwError(() => error);
      }
      return sesion.refrescar().pipe(
        switchMap((renovada) => {
          if (!renovada) {
            void sesion.sesionCaducada();
            return throwError(() => error);
          }
          return siguiente(conToken(peticion, sesion.token()));
        }),
      );
    }),
  );
};
