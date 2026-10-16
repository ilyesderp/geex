import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/** Aviso dentro de la página (error, advertencia o éxito). */
@Component({
  selector: 'geex-aviso',
  imports: [MatIconModule],
  template: `
    <div class="geex-aviso" [class]="'geex-aviso geex-aviso--' + tipo()" [attr.role]="tipo() === 'error' ? 'alert' : 'status'">
      <mat-icon aria-hidden="true">{{ icono() }}</mat-icon>
      <span><ng-content /></span>
    </div>
  `,
})
export class Aviso {
  readonly tipo = input<'error' | 'advertencia' | 'exito'>('error');
  protected readonly icono = computed(() =>
    ({ error: 'error', advertencia: 'warning', exito: 'check_circle' })[this.tipo()],
  );
}
