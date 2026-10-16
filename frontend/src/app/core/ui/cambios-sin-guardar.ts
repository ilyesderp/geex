import { inject } from '@angular/core';
import { CanDeactivateFn } from '@angular/router';
import { ConfirmacionService } from './confirmacion.service';

/** Pantalla con un formulario que puede tener cambios pendientes de guardar. */
export interface ConCambiosSinGuardar {
  tieneCambiosSinGuardar(): boolean;
}

/** Antes de salir de una pantalla con cambios sin guardar, pide confirmación. */
export const confirmarSalidaSinGuardar: CanDeactivateFn<ConCambiosSinGuardar> = (pantalla) => {
  if (!pantalla.tieneCambiosSinGuardar()) {
    return true;
  }
  return inject(ConfirmacionService).confirmar({
    titulo: '¿Salir sin guardar?',
    mensaje: 'Tienes cambios sin guardar en esta pantalla. Si sales ahora, se perderán.',
    aceptar: 'Salir sin guardar',
    peligroso: true,
  });
};

/**
 * Para el evento {@code window:beforeunload} del componente: el navegador avisa si
 * se cierra o recarga la pestaña con cambios sin guardar.
 */
export function avisarAlCerrarPestana(evento: BeforeUnloadEvent, hayCambios: boolean): void {
  if (hayCambios) {
    evento.preventDefault();
  }
}
