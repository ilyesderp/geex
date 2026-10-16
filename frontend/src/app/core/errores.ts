import { HttpErrorResponse } from '@angular/common/http';

/** Respuesta de error de la API (RFC 9457, Problem Details) con el código estable de GEEX. */
export interface ProblemaApi {
  title?: string;
  detail?: string;
  status?: number;
  codigo?: string;
  errores?: { campo: string; mensaje: string }[];
}

export function problemaDe(error: unknown): ProblemaApi {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return { status: 0, codigo: 'SIN_CONEXION', detail: 'No se puede conectar con el servidor. Comprueba tu conexión.' };
    }
    const cuerpo = error.error;
    if (cuerpo && typeof cuerpo === 'object') {
      return { status: error.status, ...(cuerpo as ProblemaApi) };
    }
    return { status: error.status, detail: 'Se ha producido un error inesperado.' };
  }
  return { detail: 'Se ha producido un error inesperado.' };
}

/** Mensaje legible en español para mostrar al usuario. */
export function mensajeDeError(error: unknown): string {
  const problema = problemaDe(error);
  if (problema.errores?.length) {
    return problema.errores.map((e) => e.mensaje).join('. ');
  }
  return problema.detail ?? problema.title ?? 'Se ha producido un error inesperado.';
}
