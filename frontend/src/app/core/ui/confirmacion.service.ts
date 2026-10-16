import { Component, inject, Injectable } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';

export interface DatosConfirmacion {
  titulo: string;
  mensaje: string;
  aceptar: string;
  peligroso?: boolean;
}

@Component({
  selector: 'geex-dialogo-confirmacion',
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ datos.titulo }}</h2>
    <mat-dialog-content><p>{{ datos.mensaje }}</p></mat-dialog-content>
    <mat-dialog-actions align="end">
      <button matButton type="button" [mat-dialog-close]="false">Cancelar</button>
      <button matButton="filled" type="button" [class.peligro]="datos.peligroso" [mat-dialog-close]="true" cdkFocusInitial>
        {{ datos.aceptar }}
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    p { margin: 0; line-height: 1.5; }
    .peligro { --mat-button-filled-container-color: #b3261e; }
  `,
})
class DialogoConfirmacion {
  protected readonly datos = inject<DatosConfirmacion>(MAT_DIALOG_DATA);
}

/** Pide confirmación antes de una acción (desactivar, eliminar...). */
@Injectable({ providedIn: 'root' })
export class ConfirmacionService {
  private readonly dialogo = inject(MatDialog);

  async confirmar(datos: DatosConfirmacion): Promise<boolean> {
    const ref = this.dialogo.open(DialogoConfirmacion, { data: datos, width: '440px', autoFocus: 'dialog' });
    return (await firstValueFrom(ref.afterClosed())) === true;
  }
}
