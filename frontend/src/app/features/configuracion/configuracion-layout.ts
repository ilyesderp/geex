import { Component, computed, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Permiso, SesionService } from '../../core/sesion/sesion.service';

interface Seccion {
  ruta: string;
  nombre: string;
  icono: string;
  permiso?: Permiso;
}

/** Configuración del despacho: índice de secciones siempre visible a la izquierda. */
@Component({
  selector: 'geex-configuracion-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatIconModule],
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
    @media (max-width: 900px) {
      .configuracion { grid-template-columns: 1fr; background: none; }
      .indice { position: static; height: auto; display: flex; flex-wrap: wrap; gap: 4px; border-right: 0;
        border-bottom: 1px solid var(--geex-borde); }
      .indice__titulo { width: 100%; }
    }
  `,
})
export class ConfiguracionLayout {
  private readonly sesion = inject(SesionService);

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
}
