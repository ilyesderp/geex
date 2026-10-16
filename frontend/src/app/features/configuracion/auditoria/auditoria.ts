import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { firstValueFrom } from 'rxjs';
import { AuditoriaService } from '../../../api/generated';
import { EntradaAuditoria } from '../../../api/generated/model/entradaAuditoria';
import { AvisosService } from '../../../core/ui/avisos.service';

/** Registro de actividad del despacho (solo lectura; trazabilidad exigida por el RGPD). */
@Component({
  selector: 'geex-auditoria',
  imports: [FormsModule, DatePipe, MatTableModule, MatPaginatorModule, MatFormFieldModule, MatSelectModule,
    MatInputModule, MatDatepickerModule, MatButtonModule, MatIconModule, MatProgressBarModule],
  templateUrl: './auditoria.html',
  styles: `
    .filtros { display: flex; gap: 12px; flex-wrap: wrap; align-items: center; margin-bottom: 8px; }
    .filtros .accion { min-width: 280px; }
    .filtros .fecha { width: 180px; }
    .cuando { white-space: nowrap; }
  `,
})
export class Auditoria implements OnInit {
  private readonly api = inject(AuditoriaService);
  private readonly avisos = inject(AvisosService);

  protected readonly acciones = [
    { valor: 'SESION_INICIADA', nombre: 'Inicio de sesión' },
    { valor: 'INICIO_SESION_FALLIDO', nombre: 'Inicio de sesión fallido' },
    { valor: 'USUARIO_INVITADO', nombre: 'Usuario invitado' },
    { valor: 'CUENTA_ACTIVADA', nombre: 'Cuenta activada' },
    { valor: 'ROL_CAMBIADO', nombre: 'Rol cambiado' },
    { valor: 'ACCESO_DESACTIVADO', nombre: 'Acceso desactivado' },
    { valor: 'ACCESO_REACTIVADO', nombre: 'Acceso reactivado' },
    { valor: 'PERMISOS_ACTUALIZADOS', nombre: 'Permisos modificados' },
    { valor: 'CONTRASENA_CAMBIADA', nombre: 'Contraseña cambiada' },
    { valor: 'SESION_REUTILIZADA', nombre: 'Alerta de seguridad: sesión reutilizada' },
    { valor: 'DESPACHO_CREADO', nombre: 'Despacho creado' },
    { valor: 'DESPACHO_ACTUALIZADO', nombre: 'Datos del despacho' },
    { valor: 'LISTA_MODIFICADA', nombre: 'Listas desplegables' },
    { valor: 'CALENDARIO_MODIFICADO', nombre: 'Calendario laboral' },
    { valor: 'INSTALACION_COMPLETADA', nombre: 'Instalación' },
  ];
  private readonly nombres = new Map(this.acciones.map((a) => [a.valor, a.nombre]));

  protected readonly columnas = ['ocurridoEn', 'actor', 'accion', 'descripcion'];
  protected readonly entradas = signal<EntradaAuditoria[]>([]);
  protected readonly total = signal(0);
  protected readonly cargando = signal(false);
  protected readonly pagina = signal(0);
  protected readonly filas = signal(25);
  protected accion: string | null = null;
  protected desde: Date | null = null;
  protected hasta: Date | null = null;

  async ngOnInit(): Promise<void> {
    await this.cargar();
  }

  protected nombreAccion(codigo: string): string {
    return this.nombres.get(codigo) ?? codigo;
  }

  protected esAlerta(codigo: string): boolean {
    return codigo === 'SESION_REUTILIZADA' || codigo === 'INICIO_SESION_FALLIDO';
  }

  protected async cambiarPagina(evento: PageEvent): Promise<void> {
    this.pagina.set(evento.pageIndex);
    this.filas.set(evento.pageSize);
    await this.cargar();
  }

  protected async aplicarFiltros(): Promise<void> {
    this.pagina.set(0);
    await this.cargar();
  }

  protected async limpiar(): Promise<void> {
    this.accion = null;
    this.desde = null;
    this.hasta = null;
    await this.aplicarFiltros();
  }

  private async cargar(): Promise<void> {
    this.cargando.set(true);
    try {
      const resultado = await firstValueFrom(this.api.consultarAuditoria(
        this.accion ?? undefined, aIso(this.desde), aIso(this.hasta), this.pagina(), this.filas()));
      this.entradas.set(resultado.entradas);
      this.total.set(resultado.total);
    } catch (e) {
      this.avisos.error('No se pudo cargar la auditoría', e);
    } finally {
      this.cargando.set(false);
    }
  }
}

function aIso(fecha: Date | null): string | undefined {
  if (!fecha) {
    return undefined;
  }
  return `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}-${String(fecha.getDate()).padStart(2, '0')}`;
}
