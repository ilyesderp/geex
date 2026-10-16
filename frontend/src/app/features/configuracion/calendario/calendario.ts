import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal, TemplateRef, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTableModule } from '@angular/material/table';
import { firstValueFrom } from 'rxjs';
import { CalendarioService } from '../../../api/generated';
import { CalendarioAnual } from '../../../api/generated/model/calendarioAnual';
import { DiaInhabilVista } from '../../../api/generated/model/diaInhabilVista';
import { FestivoGlobalPeticion } from '../../../api/generated/model/festivoGlobalPeticion';
import { COMUNIDADES, ComunidadAutonoma } from '../../../core/comunidades';
import { SesionService } from '../../../core/sesion/sesion.service';
import { AvisosService } from '../../../core/ui/avisos.service';
import { ConfirmacionService } from '../../../core/ui/confirmacion.service';

type Ambito = FestivoGlobalPeticion.AmbitoEnum;

/**
 * Calendario laboral: festivos nacionales, de la comunidad autónoma y locales.
 * Sábados y domingos son siempre inhábiles. Es la base del cómputo de plazos.
 */
@Component({
  selector: 'geex-calendario',
  imports: [FormsModule, DatePipe, MatTableModule, MatButtonModule, MatIconModule, MatSelectModule, MatFormFieldModule,
    MatInputModule, MatDatepickerModule, MatDialogModule, MatCheckboxModule, MatProgressBarModule, MatTooltipModule],
  templateUrl: './calendario.html',
  styles: `
    .controles { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
    .controles mat-form-field { width: 120px; }
    .resumen { display: grid; grid-template-columns: repeat(3, max-content); gap: 8px 40px; margin-bottom: 16px; }
    .resumen strong { font-size: 1.5rem; color: var(--geex-marino); display: block; }
    .fecha { white-space: nowrap; }
    .fecha small { display: block; color: var(--geex-texto-suave); text-transform: capitalize; }
    .nota { margin: 16px 0 0; font-size: 0.85rem; }
    .provisional { margin-left: 8px; }
    /* Icono neutro; solo se pinta de rojo al pasar por encima (una columna roja entera distrae) */
    .eliminar { color: var(--geex-texto-suave); }
    .eliminar:hover, .eliminar:focus-visible { color: #b3261e; }
    .col-acciones { width: 56px; text-align: right; }
    @media (max-width: 600px) {
      .resumen { grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; font-size: 0.8rem; line-height: 1.3; }
      .resumen strong { font-size: 1.3rem; }
    }
  `,
})
export class Calendario implements OnInit {
  private readonly api = inject(CalendarioService);
  private readonly avisos = inject(AvisosService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly dialogo = inject(MatDialog);
  private readonly plantillaFestivo = viewChild.required<TemplateRef<unknown>>('dialogoFestivo');
  private dialogoAbierto: MatDialogRef<unknown> | null = null;
  protected readonly puedeEditar = inject(SesionService).tienePermiso('CONFIGURACION_GESTIONAR');

  private readonly anioActual = new Date().getFullYear();
  protected readonly anios = [this.anioActual - 1, this.anioActual, this.anioActual + 1];
  protected readonly anio = signal(this.anioActual);
  protected readonly calendario = signal<CalendarioAnual | null>(null);
  protected readonly cargando = signal(false);
  protected readonly columnas = ['fecha', 'descripcion', 'ambito', 'acciones'];
  protected readonly comunidades = COMUNIDADES;
  protected readonly ambitos: { valor: Ambito; nombre: string }[] = [
    { valor: 'NACIONAL', nombre: 'Nacional' },
    { valor: 'AUTONOMICO', nombre: 'Autonómico' },
  ];

  protected readonly tipoDialogo = signal<'local' | 'global'>('local');
  protected nuevaFecha: Date | null = null;
  protected nuevaDescripcion = '';
  protected nuevoAmbito: Ambito = 'NACIONAL';
  protected nuevaComunidad: ComunidadAutonoma = 'COMUNITAT_VALENCIANA';
  protected nuevoProvisional = false;

  /** Los sábados y domingos ya son inhábiles: no se pueden elegir. */
  protected readonly soloLaborables = (fecha: Date | null): boolean => {
    const dia = (fecha ?? new Date()).getDay();
    return dia !== 0 && dia !== 6;
  };

  protected readonly conteo = computed(() => {
    const dias = this.calendario()?.dias ?? [];
    return {
      nacional: dias.filter((d) => d.ambito === 'NACIONAL').length,
      autonomico: dias.filter((d) => d.ambito === 'AUTONOMICO').length,
      local: dias.filter((d) => d.ambito === 'LOCAL').length,
    };
  });

  async ngOnInit(): Promise<void> {
    await this.cargar();
  }

  protected async cambiarAnio(anio: number): Promise<void> {
    this.anio.set(anio);
    await this.cargar();
  }

  protected claseAmbito(dia: DiaInhabilVista): string {
    return dia.ambito === 'NACIONAL' ? 'geex-etiqueta--info' : dia.ambito === 'AUTONOMICO' ? 'geex-etiqueta--exito' : 'geex-etiqueta--aviso';
  }

  protected abrir(tipo: 'local' | 'global'): void {
    this.tipoDialogo.set(tipo);
    this.nuevaFecha = null;
    this.nuevaDescripcion = '';
    this.nuevoAmbito = 'NACIONAL';
    this.nuevaComunidad = this.calendario()?.comunidadAutonoma ?? 'COMUNITAT_VALENCIANA';
    this.nuevoProvisional = false;
    this.dialogoAbierto = this.dialogo.open(this.plantillaFestivo(), { width: '480px' });
  }

  protected async guardar(): Promise<void> {
    if (!this.nuevaFecha || !this.nuevaDescripcion.trim()) {
      return;
    }
    const fecha = aFechaIso(this.nuevaFecha);
    try {
      if (this.tipoDialogo() === 'local') {
        await firstValueFrom(this.api.anadirLocal({ fecha, descripcion: this.nuevaDescripcion.trim() }));
      } else {
        await firstValueFrom(this.api.anadirGlobal({
          ambito: this.nuevoAmbito,
          comunidadAutonoma: this.nuevoAmbito === 'AUTONOMICO' ? this.nuevaComunidad : undefined,
          fecha,
          descripcion: this.nuevaDescripcion.trim(),
          provisional: this.nuevoProvisional,
        }));
      }
      this.dialogoAbierto?.close();
      this.avisos.exito('Festivo añadido al calendario');
      await this.cargar();
    } catch (e) {
      this.avisos.error('No se pudo añadir el festivo', e);
    }
  }

  protected async eliminar(dia: DiaInhabilVista): Promise<void> {
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Eliminar festivo',
      mensaje: `¿Eliminar «${dia.descripcion}» del calendario?`,
      aceptar: 'Eliminar',
      peligroso: true,
    });
    if (!confirmado) {
      return;
    }
    try {
      await firstValueFrom(dia.ambito === 'LOCAL' ? this.api.eliminarLocal(dia.id) : this.api.eliminarGlobal(dia.id));
      await this.cargar();
    } catch (e) {
      this.avisos.error('No se pudo eliminar el festivo', e);
    }
  }

  private async cargar(): Promise<void> {
    this.cargando.set(true);
    try {
      this.calendario.set(await firstValueFrom(this.api.obtenerCalendario(this.anio())));
    } catch (e) {
      this.avisos.error('No se pudo cargar el calendario', e);
    } finally {
      this.cargando.set(false);
    }
  }
}

/** Fecha local a "AAAA-MM-DD" (sin pasar por UTC, que podría cambiar el día). */
function aFechaIso(fecha: Date): string {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}
