import { Component, input } from '@angular/core';

/** Marca de GEEX. El nombre y los colores se cambian aquí y en el tema. */
@Component({
  selector: 'geex-logo',
  template: `
    <svg viewBox="0 0 64 64" [attr.width]="tamano()" [attr.height]="tamano()" aria-hidden="true">
      <rect width="64" height="64" rx="14" [attr.fill]="claro() ? '#173a6b' : 'rgba(255,255,255,0.12)'" />
      <path d="M41 22.5a12 12 0 1 0 1.8 14.2H32" fill="none" stroke="#fff" stroke-width="6"
            stroke-linecap="round" stroke-linejoin="round" />
      <circle cx="47" cy="17" r="4" fill="#4fb3a9" />
    </svg>
    <span class="nombre" [class.nombre--oscuro]="claro()">GEEX</span>
  `,
  styles: `
    :host { display: inline-flex; align-items: center; gap: 10px; }
    .nombre { font-weight: 800; font-size: 1.25rem; letter-spacing: 0.08em; color: #fff; }
    .nombre--oscuro { color: var(--geex-marino); }
  `,
})
export class Logo {
  readonly tamano = input(34);
  /** Versión para fondos claros. */
  readonly claro = input(false);
}
