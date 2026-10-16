import { Component, computed, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { moduloPorClave } from '../../core/modulos';

/** Apartados del documento que llegan en fases posteriores del plan. */
@Component({
  selector: 'geex-proximamente',
  imports: [RouterLink, MatButtonModule, MatIconModule],
  template: `
    <section class="geex-pagina">
      @if (modulo(); as m) {
        <div class="geex-tarjeta aviso">
          <span class="icono"><mat-icon aria-hidden="true">{{ m.icono }}</mat-icon></span>
          <h1>{{ m.nombre }}</h1>
          <p class="descripcion">{{ m.descripcion }}</p>
          <p class="geex-texto-suave">
            Este apartado se construye en la <strong>fase {{ m.fase }}</strong> del plan de desarrollo.
          </p>
          <a matButton="outlined" routerLink="/"><mat-icon>arrow_back</mat-icon>Volver al inicio</a>
        </div>
      } @else {
        <div class="geex-tarjeta aviso"><h1>Apartado no encontrado</h1></div>
      }
    </section>
  `,
  styles: `
    .aviso { max-width: 640px; margin: 48px auto; text-align: center; padding: 40px 32px; }
    .icono { display: inline-grid; place-items: center; width: 64px; height: 64px; border-radius: 16px;
      background: #e8f0fb; color: var(--geex-azul); margin-bottom: 16px; }
    .icono mat-icon { font-size: 32px; width: 32px; height: 32px; }
    .descripcion { font-size: 1.05rem; line-height: 1.6; margin: 12px 0; }
    p { margin-bottom: 20px; }
  `,
})
export class Proximamente {
  readonly clave = input.required<string>();
  protected readonly modulo = computed(() => moduloPorClave(this.clave()));
}
