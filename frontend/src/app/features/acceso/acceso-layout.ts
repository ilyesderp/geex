import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { Logo } from '../../layout/logo';

/** Pantallas de acceso: panel de marca a la izquierda y formulario a la derecha. */
@Component({
  selector: 'geex-acceso-layout',
  imports: [Logo, MatIconModule],
  template: `
    <div class="acceso">
      <aside class="marca">
        <geex-logo [tamano]="44" />
        <div class="lema">
          <h1>Gestión de expedientes de extranjería</h1>
          <p>Clientes, expedientes, plazos, renovaciones y nacionalidades en un solo lugar.</p>
        </div>
        <ul class="ventajas">
          <li><mat-icon aria-hidden="true">schedule</mat-icon> Ningún plazo se pasa: renovaciones, requerimientos y recursos</li>
          <li><mat-icon aria-hidden="true">shield_lock</mat-icon> Datos protegidos y separados por despacho</li>
          <li><mat-icon aria-hidden="true">eco</mat-icon> Objetivo: cero papel</li>
        </ul>
      </aside>
      <main class="panel">
        <div class="tarjeta" [class.tarjeta--ancha]="ancha()">
          <ng-content />
        </div>
      </main>
    </div>
  `,
  styles: `
    .acceso { min-height: 100vh; display: grid; grid-template-columns: minmax(320px, 0.9fr) 1.1fr; }
    .marca { background: linear-gradient(160deg, #173a6b 0%, #112b50 60%, #0b1c35 100%); color: #fff;
      padding: 48px; display: flex; flex-direction: column; gap: 40px; }
    .lema h1 { color: #fff; font-size: 2rem; line-height: 1.2; margin-bottom: 12px; }
    .lema p { opacity: 0.8; font-size: 1.05rem; line-height: 1.5; margin: 0; }
    .ventajas { list-style: none; padding: 0; margin: auto 0 0; display: grid; gap: 14px; opacity: 0.9; }
    .ventajas li { display: flex; align-items: center; gap: 10px; }
    .ventajas mat-icon { color: #4fb3a9; }
    .panel { display: grid; place-items: center; padding: 32px 16px; }
    .tarjeta { width: 100%; max-width: 460px; background: #fff; border: 1px solid var(--geex-borde);
      border-radius: 16px; box-shadow: var(--geex-sombra-elevada); padding: 36px 32px; }
    .tarjeta--ancha { max-width: 680px; }
    /* Móvil: cabecera de marca compacta para que el formulario quede a la vista sin desplazarse */
    @media (max-width: 900px) {
      .acceso { grid-template-columns: 1fr; grid-template-rows: auto 1fr; }
      .marca { padding: 20px 20px 22px; gap: 12px; }
      .lema h1 { font-size: 1.15rem; margin: 0; }
      .lema p, .ventajas { display: none; }
      .panel { place-items: start center; padding: 20px 16px 32px; }
      .tarjeta { padding: 24px 20px; }
    }
  `,
})
export class AccesoLayout {
  readonly ancha = input(false);
}
