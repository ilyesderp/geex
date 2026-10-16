import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { firstValueFrom } from 'rxjs';
import { DespachoService } from '../../../api/generated';
import { DespachoVista } from '../../../api/generated/model/despachoVista';
import { COMUNIDADES, ComunidadAutonoma } from '../../../core/comunidades';
import { SesionService } from '../../../core/sesion/sesion.service';
import { AvisosService } from '../../../core/ui/avisos.service';
import { avisarAlCerrarPestana, ConCambiosSinGuardar } from '../../../core/ui/cambios-sin-guardar';

@Component({
  selector: 'geex-datos-despacho',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule,
    MatProgressBarModule],
  templateUrl: './datos-despacho.html',
  host: { '(window:beforeunload)': 'alCerrarPestana($event)' },
})
export class DatosDespacho implements OnInit, ConCambiosSinGuardar {
  private readonly api = inject(DespachoService);
  private readonly avisos = inject(AvisosService);
  protected readonly sesion = inject(SesionService);

  protected readonly comunidades = COMUNIDADES;
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly puedeEditar = this.sesion.tienePermiso('CONFIGURACION_GESTIONAR');
  /** Versión cargada: el servidor rechaza el guardado si otra persona lo modificó entretanto. */
  private version = 0;

  protected readonly formulario = inject(NonNullableFormBuilder).group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    razonSocial: ['', Validators.maxLength(200)],
    nif: ['', Validators.maxLength(20)],
    direccion: ['', Validators.maxLength(250)],
    codigoPostal: ['', Validators.maxLength(10)],
    municipio: ['', [Validators.required, Validators.maxLength(120)]],
    provincia: ['', [Validators.required, Validators.maxLength(120)]],
    comunidadAutonoma: ['COMUNITAT_VALENCIANA' as ComunidadAutonoma, Validators.required],
    telefono: ['', Validators.maxLength(30)],
    email: ['', [Validators.email, Validators.maxLength(254)]],
    web: ['', Validators.maxLength(250)],
  });

  async ngOnInit(): Promise<void> {
    try {
      this.cargar(await firstValueFrom(this.api.obtenerDespacho()));
    } catch (e) {
      this.avisos.error('No se pudieron cargar los datos', e);
    } finally {
      this.cargando.set(false);
    }
    if (!this.puedeEditar) {
      this.formulario.disable();
    }
  }

  async guardar(): Promise<void> {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    try {
      const d = this.formulario.getRawValue();
      this.cargar(await firstValueFrom(this.api.actualizarDespacho({
        ...d,
        razonSocial: d.razonSocial || undefined,
        nif: d.nif || undefined,
        direccion: d.direccion || undefined,
        codigoPostal: d.codigoPostal || undefined,
        telefono: d.telefono || undefined,
        email: d.email || undefined,
        web: d.web || undefined,
        version: this.version,
      })));
      await this.sesion.recargarPerfil();
      this.avisos.exito('Datos del despacho guardados');
    } catch (e) {
      this.avisos.error('No se pudo guardar', e);
    } finally {
      this.guardando.set(false);
    }
  }

  tieneCambiosSinGuardar(): boolean {
    return this.formulario.dirty && !this.guardando();
  }

  protected alCerrarPestana(evento: BeforeUnloadEvent): void {
    avisarAlCerrarPestana(evento, this.tieneCambiosSinGuardar());
  }

  private cargar(d: DespachoVista): void {
    this.version = d.version;
    this.formulario.reset({
      nombre: d.nombre,
      razonSocial: d.razonSocial ?? '',
      nif: d.nif ?? '',
      direccion: d.direccion ?? '',
      codigoPostal: d.codigoPostal ?? '',
      municipio: d.municipio,
      provincia: d.provincia,
      comunidadAutonoma: d.comunidadAutonoma,
      telefono: d.telefono ?? '',
      email: d.email ?? '',
      web: d.web ?? '',
    });
  }
}
