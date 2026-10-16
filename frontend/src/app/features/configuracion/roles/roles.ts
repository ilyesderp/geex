import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { firstValueFrom } from 'rxjs';
import { RolesService } from '../../../api/generated';
import { DescripcionPermiso } from '../../../api/generated/model/descripcionPermiso';
import { PermisosDeRol } from '../../../api/generated/model/permisosDeRol';
import { SesionService } from '../../../core/sesion/sesion.service';
import { AvisosService } from '../../../core/ui/avisos.service';
import { avisarAlCerrarPestana, ConCambiosSinGuardar } from '../../../core/ui/cambios-sin-guardar';
import { BarraCambios } from '../../../shared/barra-cambios';

type PermisoClave = DescripcionPermiso['permiso'];
type RolClave = PermisosDeRol['rol'];

interface GrupoPermisos {
  nombre: string;
  permisos: DescripcionPermiso[];
}

/**
 * Matriz de permisos: qué puede hacer cada rol del despacho (el titular siempre
 * puede todo). En escritorio se ve la matriz completa; en pantallas estrechas,
 * un rol cada vez.
 */
@Component({
  selector: 'geex-roles',
  imports: [FormsModule, MatButtonModule, MatCheckboxModule, MatFormFieldModule, MatIconModule, MatProgressBarModule,
    MatSelectModule, MatTooltipModule, BarraCambios],
  templateUrl: './roles.html',
  styleUrl: './roles.scss',
  host: { '(window:beforeunload)': 'alCerrarPestana($event)' },
})
export class Roles implements OnInit, ConCambiosSinGuardar {
  private readonly api = inject(RolesService);
  private readonly avisos = inject(AvisosService);
  private readonly sesion = inject(SesionService);

  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly roles = signal<PermisosDeRol[]>([]);
  protected readonly grupos = signal<GrupoPermisos[]>([]);
  /** Copia editable: rol → permisos marcados. */
  protected readonly edicion = signal<Record<string, Set<PermisoClave>>>({});
  private original: Record<string, Set<PermisoClave>> = {};

  protected readonly cambiados = computed(() =>
    this.roles()
      .filter((r) => r.editable && !iguales(this.edicion()[r.rol], this.original[r.rol]))
      .map((r) => r.rol),
  );

  protected readonly editables = computed(() => this.roles().filter((r) => r.editable));
  /** Rol que se edita en la vista de pantallas estrechas. */
  protected readonly rolMovil = signal<RolClave>('ABOGADO');
  protected readonly rolSeleccionado = computed(() => this.roles().find((r) => r.rol === this.rolMovil()));

  protected readonly mensajeCambios = computed(() => {
    const n = this.cambiados().length;
    return n === 1 ? 'Cambios sin guardar en 1 rol' : `Cambios sin guardar en ${n} roles`;
  });

  tieneCambiosSinGuardar(): boolean {
    return this.cambiados().length > 0;
  }

  protected alCerrarPestana(evento: BeforeUnloadEvent): void {
    avisarAlCerrarPestana(evento, this.tieneCambiosSinGuardar());
  }

  async ngOnInit(): Promise<void> {
    try {
      const [matriz, catalogo] = await Promise.all([
        firstValueFrom(this.api.obtenerMatriz()),
        firstValueFrom(this.api.catalogoPermisos()),
      ]);
      const grupos = new Map<string, DescripcionPermiso[]>();
      catalogo.forEach((p) => grupos.set(p.grupo, [...(grupos.get(p.grupo) ?? []), p]));
      this.grupos.set([...grupos].map(([nombre, permisos]) => ({ nombre, permisos })));
      this.aplicar(matriz);
    } catch (e) {
      this.avisos.error('No se pudo cargar la matriz de permisos', e);
    } finally {
      this.cargando.set(false);
    }
  }

  protected marcado(rol: RolClave, permiso: PermisoClave): boolean {
    return this.edicion()[rol]?.has(permiso) ?? false;
  }

  protected cambiar(rol: RolClave, permiso: PermisoClave, valor: boolean): void {
    this.edicion.update((actual) => {
      const conjunto = new Set(actual[rol]);
      if (valor) {
        conjunto.add(permiso);
      } else {
        conjunto.delete(permiso);
      }
      return { ...actual, [rol]: conjunto };
    });
  }

  protected descartar(): void {
    this.edicion.set(clonar(this.original));
  }

  protected async guardar(): Promise<void> {
    this.guardando.set(true);
    try {
      for (const rol of this.cambiados()) {
        await firstValueFrom(this.api.actualizarPermisosRol(rol, { permisos: [...this.edicion()[rol]] }));
      }
      this.aplicar(await firstValueFrom(this.api.obtenerMatriz()));
      await this.sesion.recargarPerfil();
      this.avisos.exito('Permisos guardados. Los cambios se aplican de inmediato');
    } catch (e) {
      this.avisos.error('No se pudieron guardar los permisos', e);
    } finally {
      this.guardando.set(false);
    }
  }

  private aplicar(matriz: PermisosDeRol[]): void {
    this.roles.set(matriz);
    this.original = Object.fromEntries(matriz.map((r) => [r.rol, new Set(r.permisos as PermisoClave[])]));
    this.edicion.set(clonar(this.original));
  }
}

function clonar(m: Record<string, Set<PermisoClave>>): Record<string, Set<PermisoClave>> {
  return Object.fromEntries(Object.entries(m).map(([k, v]) => [k, new Set(v)]));
}

function iguales(a: Set<PermisoClave> | undefined, b: Set<PermisoClave> | undefined): boolean {
  if (!a || !b || a.size !== b.size) {
    return a === b;
  }
  return [...a].every((x) => b.has(x));
}
