import { inject, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mensajeDeError } from '../errores';

/** Notificaciones breves en la esquina de la pantalla. */
@Injectable({ providedIn: 'root' })
export class AvisosService {
  private readonly snackBar = inject(MatSnackBar);

  exito(mensaje: string): void {
    this.snackBar.open(mensaje, 'Cerrar', {
      duration: 4000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: 'geex-snack-exito',
    });
  }

  error(titulo: string, causa?: unknown): void {
    const detalle = causa === undefined ? '' : ` ${mensajeDeError(causa)}`;
    this.snackBar.open(`${titulo}.${detalle}`, 'Cerrar', {
      duration: 7000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: 'geex-snack-error',
    });
  }
}
