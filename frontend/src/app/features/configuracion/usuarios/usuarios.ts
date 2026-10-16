import { DatePipe } from '@angular/common';
import { AfterViewInit, Component, inject, OnInit, signal, TemplateRef, viewChild } from '@angular/core';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { firstValueFrom } from 'rxjs';
import { UsuariosService } from '../../../api/generated';
import { UsuarioDespacho } from '../../../api/generated/model/usuarioDespacho';
import { Rol, ROLES } from '../../../core/roles';
import { AvisosService } from '../../../core/ui/avisos.service';
import { ConfirmacionService } from '../../../core/ui/confirmacion.service';

@Component({
  selector: 'geex-usuarios',
  imports: [ReactiveFormsModule, FormsModule, DatePipe, MatTableModule, MatSortModule, MatButtonModule, MatIconModule,
    MatMenuModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatProgressBarModule],
  templateUrl: './usuarios.html',
  styles: `
    .persona { display: flex; flex-direction: column; padding: 6px 0; }
    .persona small { color: var(--geex-texto-suave); overflow-wrap: anywhere; }
    .resumen-movil { align-items: center; flex-wrap: wrap; gap: 8px; margin-top: 6px; font-size: 0.85rem; }
    .usted { font-size: 0.75rem; color: var(--geex-azul); font-weight: 600; margin-left: 6px; }
    .buscador { width: 100%; max-width: 380px; }
    .col-acciones { width: 56px; text-align: right; }
  `,
})
export class Usuarios implements OnInit, AfterViewInit {
  private readonly api = inject(UsuariosService);
  private readonly avisos = inject(AvisosService);
  private readonly confirmacion = inject(ConfirmacionService);
  private readonly dialogo = inject(MatDialog);
  private readonly orden = viewChild(MatSort);
  private readonly plantillaInvitar = viewChild.required<TemplateRef<unknown>>('dialogoInvitar');
  private readonly plantillaRol = viewChild.required<TemplateRef<unknown>>('dialogoRol');
  private dialogoAbierto: MatDialogRef<unknown> | null = null;

  protected readonly roles = ROLES;
  protected readonly columnas = ['nombre', 'rolEtiqueta', 'estado', 'ultimoAcceso', 'acciones'];
  protected readonly datos = new MatTableDataSource<UsuarioDespacho>([]);
  protected readonly cargando = signal(true);
  protected readonly invitando = signal(false);
  protected readonly usuarioRol = signal<UsuarioDespacho | null>(null);
  protected rolSeleccionado: Rol = 'GESTOR';

  protected readonly formulario = inject(NonNullableFormBuilder).group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellidos: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email]],
    rol: ['GESTOR' as Rol, Validators.required],
  });

  constructor() {
    this.datos.filterPredicate = (u, filtro) =>
      normalizar(`${u.nombre} ${u.apellidos} ${u.email} ${u.rolEtiqueta}`).includes(filtro);
  }

  async ngOnInit(): Promise<void> {
    await this.cargar();
  }

  ngAfterViewInit(): void {
    this.datos.sort = this.orden() ?? null;
  }

  protected filtrar(texto: string): void {
    this.datos.filter = normalizar(texto);
  }

  protected claseEstado(estado: UsuarioDespacho['estado']): string {
    return estado === 'ACTIVO' ? 'geex-etiqueta--exito' : estado === 'PENDIENTE_ACTIVACION' ? 'geex-etiqueta--aviso' : '';
  }

  protected textoEstado(estado: UsuarioDespacho['estado']): string {
    return estado === 'ACTIVO' ? 'Activo' : estado === 'PENDIENTE_ACTIVACION' ? 'Pendiente de activar' : 'Desactivado';
  }

  protected abrirInvitacion(): void {
    this.formulario.reset({ nombre: '', apellidos: '', email: '', rol: 'GESTOR' });
    this.dialogoAbierto = this.dialogo.open(this.plantillaInvitar(), { width: '560px' });
  }

  protected async invitar(): Promise<void> {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.invitando.set(true);
    try {
      const usuario = await firstValueFrom(this.api.invitar(this.formulario.getRawValue()));
      this.dialogoAbierto?.close();
      this.avisos.exito(usuario.estado === 'PENDIENTE_ACTIVACION'
        ? `Invitación enviada: ${usuario.email} recibirá un correo para activar su cuenta (válido 48 horas)`
        : `${usuario.email} ya tenía cuenta: ahora también tiene acceso a este despacho`);
      await this.cargar();
    } catch (e) {
      this.avisos.error('No se pudo invitar', e);
    } finally {
      this.invitando.set(false);
    }
  }

  protected abrirCambioRol(usuario: UsuarioDespacho): void {
    this.rolSeleccionado = usuario.rol;
    this.usuarioRol.set(usuario);
    this.dialogoAbierto = this.dialogo.open(this.plantillaRol(), { width: '460px' });
  }

  protected async guardarRol(): Promise<void> {
    const usuario = this.usuarioRol();
    if (!usuario) {
      return;
    }
    this.dialogoAbierto?.close();
    await this.ejecutar(() => firstValueFrom(this.api.cambiarRol(usuario.id, { rol: this.rolSeleccionado })),
      `Rol de ${usuario.nombre} actualizado. Se le pedirá volver a iniciar sesión`);
  }

  protected async reenviar(usuario: UsuarioDespacho): Promise<void> {
    await this.ejecutar(() => firstValueFrom(this.api.reenviar(usuario.id)),
      `Se ha enviado un nuevo enlace a ${usuario.email}`);
  }

  protected async desactivar(usuario: UsuarioDespacho): Promise<void> {
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Desactivar acceso',
      mensaje: `${usuario.nombre} ${usuario.apellidos} dejará de poder entrar en este despacho y se cerrarán sus sesiones. Podrás reactivarlo más adelante.`,
      aceptar: 'Desactivar',
      peligroso: true,
    });
    if (confirmado) {
      await this.ejecutar(() => firstValueFrom(this.api.desactivar(usuario.id)),
        `${usuario.nombre} ya no puede acceder al despacho`);
    }
  }

  protected async reactivar(usuario: UsuarioDespacho): Promise<void> {
    await this.ejecutar(() => firstValueFrom(this.api.reactivar(usuario.id)),
      `${usuario.nombre} vuelve a tener acceso al despacho`);
  }

  private async ejecutar(accion: () => Promise<unknown>, mensajeExito: string): Promise<void> {
    try {
      await accion();
      this.avisos.exito(mensajeExito);
      await this.cargar();
    } catch (e) {
      this.avisos.error('No se pudo completar la acción', e);
    }
  }

  private async cargar(): Promise<void> {
    this.cargando.set(true);
    try {
      this.datos.data = await firstValueFrom(this.api.listarUsuarios());
    } catch (e) {
      this.avisos.error('No se pudo cargar la lista de usuarios', e);
    } finally {
      this.cargando.set(false);
    }
  }
}

function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim();
}
