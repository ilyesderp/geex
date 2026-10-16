import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

/**
 * Barra fija en la parte inferior que aparece cuando hay cambios sin guardar: así
 * el botón de guardar siempre está a la vista, aunque la página sea larga.
 */
@Component({
  selector: 'geex-barra-cambios',
  imports: [MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <div class="barra" role="region" aria-label="Cambios sin guardar">
      <span class="barra__texto">
        <mat-icon aria-hidden="true">edit_note</mat-icon>
        {{ mensaje() }}
      </span>
      <span class="barra__acciones">
        <button matButton type="button" (click)="descartar.emit()" [disabled]="guardando()">Descartar</button>
        <button matButton="filled" type="button" (click)="guardar.emit()" [disabled]="guardando()">
          @if (guardando()) { <mat-spinner diameter="18" /> } @else { <mat-icon>save</mat-icon> }
          Guardar cambios
        </button>
      </span>
    </div>
  `,
  styles: `
    :host { position: fixed; left: 0; right: 0; bottom: 0; z-index: 10; display: flex; justify-content: center;
      padding: 0 16px 16px; pointer-events: none; animation: entrar 0.18s ease-out; }
    .barra { pointer-events: auto; display: flex; align-items: center; justify-content: space-between; gap: 12px 24px;
      flex-wrap: wrap; width: 100%; max-width: 760px; padding: 10px 12px 10px 18px; border-radius: 14px;
      background: var(--geex-marino); color: #fff; box-shadow: 0 10px 30px rgb(11 28 53 / 0.35); }
    .barra__texto { display: flex; align-items: center; gap: 8px; font-weight: 600; }
    .barra__texto mat-icon { color: #4fb3a9; }
    .barra__acciones { display: flex; gap: 8px; margin-left: auto; }
    .barra__acciones button[matButton]:not([matButton='filled']) { --mat-button-text-label-text-color: #fff; }
    @keyframes entrar { from { transform: translateY(16px); opacity: 0; } }
    @media (prefers-reduced-motion: reduce) { :host { animation: none; } }
  `,
})
export class BarraCambios {
  readonly mensaje = input('Tienes cambios sin guardar');
  readonly guardando = input(false);
  readonly descartar = output();
  readonly guardar = output();
}
