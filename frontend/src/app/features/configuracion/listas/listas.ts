import { CdkDrag, CdkDragDrop, CdkDragHandle, CdkDropList, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { firstValueFrom } from 'rxjs';
import { CatalogosService } from '../../../api/generated';
import { ResumenCatalogo } from '../../../api/generated/model/resumenCatalogo';
import { ValorVista } from '../../../api/generated/model/valorVista';
import { SesionService } from '../../../core/sesion/sesion.service';
import { AvisosService } from '../../../core/ui/avisos.service';
import { ConfirmacionService } from '../../../core/ui/confirmacion.service';

type TipoCatalogo = ResumenCatalogo['tipo'];

/** Listas desplegables parametrizables del despacho. */
@Component({
  selector: 'geex-listas',
  imports: [FormsModule, CdkDropList, CdkDrag, CdkDragHandle, MatButtonModule, MatFormFieldModule, MatIconModule,
    MatInputModule, MatProgressBarModule, MatSelectModule, MatSlideToggleModule, MatTooltipModule],
  templateUrl: './listas.html',
  styleUrl: './listas.scss',
})
export class Listas implements OnInit {
  private readonly api = inject(CatalogosService);
  private readonly avisos = inject(AvisosService);
  private readonly confirmacion = inject(ConfirmacionService);
  protected readonly puedeEditar = inject(SesionService).tienePermiso('CONFIGURACION_GESTIONAR');

  protected readonly catalogos = signal<ResumenCatalogo[]>([]);
  protected readonly seleccionado = signal<ResumenCatalogo | null>(null);
  protected readonly valores = signal<ValorVista[]>([]);
  protected readonly cargandoValores = signal(false);
  protected readonly filtro = signal('');
  protected nuevaEtiqueta = '';
  protected readonly editando = signal<string | null>(null);
  protected etiquetaEditada = '';

  protected readonly grupos = computed(() => {
    const mapa = new Map<string, ResumenCatalogo[]>();
    this.catalogos().forEach((c) => mapa.set(c.grupo, [...(mapa.get(c.grupo) ?? []), c]));
    return [...mapa].map(([nombre, catalogos]) => ({ nombre, catalogos }));
  });

  protected readonly valoresFiltrados = computed(() => {
    const texto = normalizar(this.filtro());
    return texto ? this.valores().filter((v) => normalizar(v.etiqueta).includes(texto)) : this.valores();
  });

  /** Solo se reordena arrastrando cuando se ve la lista completa. */
  protected readonly puedeReordenar = computed(() => this.puedeEditar && !this.filtro());

  async ngOnInit(): Promise<void> {
    try {
      const catalogos = await firstValueFrom(this.api.listarCatalogos());
      this.catalogos.set(catalogos);
      if (catalogos.length) {
        await this.seleccionar(catalogos[0]);
      }
    } catch (e) {
      this.avisos.error('No se pudieron cargar las listas', e);
    }
  }

  protected async seleccionar(catalogo: ResumenCatalogo): Promise<void> {
    this.seleccionado.set(catalogo);
    this.filtro.set('');
    this.editando.set(null);
    await this.cargarValores();
  }

  protected async seleccionarTipo(tipo: ResumenCatalogo['tipo']): Promise<void> {
    const catalogo = this.catalogos().find((c) => c.tipo === tipo);
    if (catalogo) {
      await this.seleccionar(catalogo);
    }
  }

  protected async anadir(): Promise<void> {
    const tipo = this.tipo();
    const etiqueta = this.nuevaEtiqueta.trim();
    if (!tipo || !etiqueta) {
      return;
    }
    try {
      await firstValueFrom(this.api.anadirValor(tipo, { etiqueta }));
      this.nuevaEtiqueta = '';
      await this.refrescar();
    } catch (e) {
      this.avisos.error('No se pudo añadir', e);
    }
  }

  protected empezarEdicion(valor: ValorVista): void {
    this.editando.set(valor.id);
    this.etiquetaEditada = valor.etiqueta;
  }

  protected async guardarEtiqueta(valor: ValorVista): Promise<void> {
    if (this.editando() !== valor.id) {
      return;
    }
    const etiqueta = this.etiquetaEditada.trim();
    this.editando.set(null);
    if (!etiqueta || etiqueta === valor.etiqueta) {
      return;
    }
    await this.modificar(valor, etiqueta, valor.activo);
  }

  protected async cambiarActivo(valor: ValorVista, activo: boolean): Promise<void> {
    await this.modificar(valor, valor.etiqueta, activo);
  }

  protected async soltar(evento: CdkDragDrop<ValorVista[]>): Promise<void> {
    const tipo = this.tipo();
    if (!tipo || evento.previousIndex === evento.currentIndex) {
      return;
    }
    const lista = [...this.valores()];
    moveItemInArray(lista, evento.previousIndex, evento.currentIndex);
    this.valores.set(lista);
    try {
      this.valores.set(await firstValueFrom(this.api.reordenarValores(tipo, { ids: lista.map((v) => v.id) })));
    } catch (e) {
      this.avisos.error('No se pudo reordenar', e);
      await this.cargarValores();
    }
  }

  protected async eliminar(valor: ValorVista): Promise<void> {
    const tipo = this.tipo();
    if (!tipo) {
      return;
    }
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Eliminar valor',
      mensaje: `¿Eliminar «${valor.etiqueta}» de la lista?`,
      aceptar: 'Eliminar',
      peligroso: true,
    });
    if (!confirmado) {
      return;
    }
    try {
      await firstValueFrom(this.api.eliminarValor(tipo, valor.id));
      await this.refrescar();
    } catch (e) {
      this.avisos.error('No se pudo eliminar', e);
    }
  }

  private async modificar(valor: ValorVista, etiqueta: string, activo: boolean): Promise<void> {
    const tipo = this.tipo();
    if (!tipo) {
      return;
    }
    try {
      const actualizado = await firstValueFrom(this.api.modificarValor(tipo, valor.id, { etiqueta, activo }));
      this.valores.update((lista) => lista.map((v) => (v.id === valor.id ? actualizado : v)));
      await this.refrescarResumen();
    } catch (e) {
      this.avisos.error('No se pudo guardar el cambio', e);
      await this.cargarValores();
    }
  }

  private tipo(): TipoCatalogo | undefined {
    return this.seleccionado()?.tipo;
  }

  private async refrescar(): Promise<void> {
    await Promise.all([this.cargarValores(), this.refrescarResumen()]);
  }

  private async cargarValores(): Promise<void> {
    const tipo = this.tipo();
    if (!tipo) {
      return;
    }
    this.cargandoValores.set(true);
    try {
      this.valores.set(await firstValueFrom(this.api.listarValores(tipo, true)));
    } catch (e) {
      this.avisos.error('No se pudieron cargar los valores', e);
    } finally {
      this.cargandoValores.set(false);
    }
  }

  private async refrescarResumen(): Promise<void> {
    const catalogos = await firstValueFrom(this.api.listarCatalogos());
    this.catalogos.set(catalogos);
    this.seleccionado.set(catalogos.find((c) => c.tipo === this.tipo()) ?? null);
  }
}

function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim();
}
