import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'geex-sin-permiso',
  imports: [RouterLink, MatButtonModule, MatIconModule],
  template: `
    <section class="geex-pagina">
      <div class="geex-tarjeta aviso">
        <mat-icon class="candado" aria-hidden="true">lock</mat-icon>
        <h1>No tienes permiso para ver esta sección</h1>
        <p class="geex-texto-suave">Si lo necesitas, pide al titular del despacho que lo active para tu rol.</p>
        <a matButton="outlined" routerLink="/"><mat-icon>arrow_back</mat-icon>Volver al inicio</a>
      </div>
    </section>
  `,
  styles: `
    .aviso { max-width: 560px; margin: 48px auto; text-align: center; padding: 40px 32px; }
    .candado { font-size: 40px; width: 40px; height: 40px; color: var(--geex-azul); margin-bottom: 12px; }
    p { margin: 12px 0 20px; }
  `,
})
export class SinPermiso {}
