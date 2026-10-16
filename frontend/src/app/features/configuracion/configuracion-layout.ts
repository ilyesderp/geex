import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { Permiso, SesionService } from '../../core/sesion/sesion.service';

interface Seccion {
  ruta: string;
  nombre: string;
  icono: string;
  permiso?: Permiso;
}

/**
 * Configuración del despacho. En escritorio, índice de secciones siempre visible
 * a la izquierda; en móvil, un selector que muestra la sección actual y abre la
 * lista completa (así se ve claramente que hay más secciones).
 */
@Component({
  selector: 'geex-configuracion-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatIconModule, MatMenuModule],
  template: `
    <div class="configuracion">
      <nav class="indice" aria-label="Secciones de configuración">
        <h2 class="indice__titulo">Configuración</h2>
        @for (seccion of secciones(); track seccion.ruta) {
          <a class="indice__enlace" [routerLink]="seccion.ruta" routerLinkActive="indice__enlace--activo"
             ariaCurrentWhenActive="page">
            <mat-icon aria-hidden="true">{{ seccion.icono }}</mat-icon>
            {{ seccion.nombre }}
          </a>
        }
      </nav>

      <div class="selector">
        <button type="button" class="selector__boton" [matMenuTriggerFor]="menuSecciones"
                aria-label="Cambiar de sección de configuración">
          <mat-icon class="selector__icono" aria-hidden="true">{{ actual()?.icono ?? 'settings' }}</mat-icon>
          <span class="selector__textos">
            <span class="selector__contexto">Configuración</span>
            <span class="selector__actual">{{ actual()?.nombre ?? 'Elige una sección' }}</span>
          </span>
          <mat-icon class="selector__flecha" aria-hidden="true">expand_more</mat-icon>
        </button>
        <mat-menu #menuSecciones="matMenu" class="geex-menu-secciones">
          @for (seccion of secciones(); track seccion.ruta) {
            <a mat-menu-item [routerLink]="seccion.ruta" [class.geex-menu-secciones__activa]="seccion === actual()"
               [attr.aria-current]="seccion === actual() ? 'page' : null">
              <mat-icon>{{ seccion.icono }}</mat-icon>
              <span>{{ seccion.nombre }}</span>
            </a>
          }
        </mat-menu>
      </div>

      <div class="detalle">
        <router-outlet />
      </div>
    </div>
  `,
  styles: `
    .configuracion { display: grid; grid-template-columns: 250px 1fr; min-height: calc(100vh - var(--geex-cabecera));
      background: linear-gradient(to right, #fff 249px, var(--geex-borde) 249px, var(--geex-borde) 250px, transparent 250px); }
    .indice { background: #fff; border-right: 1px solid var(--geex-borde); padding: 20px 12px;
      position: sticky; top: var(--geex-cabecera); height: calc(100vh - var(--geex-cabecera)); overflow-y: auto; }
    .indice__titulo { font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.08em;
      color: var(--geex-texto-suave); padding: 0 12px 10px; }
    .indice__enlace { display: flex; align-items: center; gap: 12px; padding: 9px 12px; border-radius: 8px;
      color: var(--geex-texto); text-decoration: none; font-weight: 500; margin-bottom: 2px; }
    .indice__enlace mat-icon { color: var(--geex-texto-suave); font-size: 20px; width: 20px; height: 20px; }
    .indice__enlace:hover { background: #f1f5f9; }
    .indice__enlace--activo { background: #e8f0fb; color: var(--geex-azul); font-weight: 650; }
    .indice__enlace--activo mat-icon { color: var(--geex-azul); }
    .detalle { min-width: 0; }
    .selector { display: none; }

    @media (max-width: 900px) {
      .configuracion { grid-template-columns: 1fr; align-content: start; background: none; }
      .indice { display: none; }
      .selector { display: block; position: sticky; top: var(--geex-cabecera); z-index: 2; padding: 10px 16px;
        background: #fff; border-bottom: 1px solid var(--geex-borde); }
      .selector__boton { display: flex; align-items: center; gap: 12px; width: 100%; padding: 8px 12px;
        border: 1px solid var(--geex-borde); border-radius: 10px; background: #f8fafc; color: var(--geex-texto);
        font: inherit; text-align: left; cursor: pointer; }
      .selector__boton:focus-visible { outline: 2px solid var(--geex-azul); outline-offset: 2px; }
      .selector__icono { color: var(--geex-azul); }
      .selector__textos { display: flex; flex-direction: column; flex: 1; min-width: 0; }
      .selector__contexto { font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.08em;
        color: var(--geex-texto-suave); }
      .selector__actual { font-weight: 650; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
      .selector__flecha { color: var(--geex-texto-suave); }
    }
  `,
})
export class ConfiguracionLayout {
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  private readonly todas: Seccion[] = [
    { ruta: 'despacho', nombre: 'Datos del despacho', icono: 'apartment' },
    { ruta: 'usuarios', nombre: 'Usuarios', icono: 'group', permiso: 'USUARIOS_GESTIONAR' },
    { ruta: 'roles', nombre: 'Roles y permisos', icono: 'admin_panel_settings', permiso: 'PERMISOS_GESTIONAR' },
    { ruta: 'listas', nombre: 'Listas desplegables', icono: 'list_alt' },
    { ruta: 'calendario', nombre: 'Calendario laboral', icono: 'calendar_month' },
    { ruta: 'auditoria', nombre: 'Auditoría', icono: 'policy', permiso: 'AUDITORIA_VER' },
  ];

  protected readonly secciones = computed(() =>
    this.todas.filter((s) => !s.permiso || this.sesion.tienePermiso(s.permiso)),
  );

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );

  /** Sección abierta ahora (para el selector de móvil). */
  protected readonly actual = computed(() => {
    const segmentos = this.url().split(/[?#]/)[0].split('/');
    return this.secciones().find((s) => segmentos.includes(s.ruta));
  });
}
