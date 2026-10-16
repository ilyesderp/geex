import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { firstValueFrom } from 'rxjs';
import { CuentaService } from '../../api/generated';
import { SesionService } from '../../core/sesion/sesion.service';
import { AvisosService } from '../../core/ui/avisos.service';
import { coinciden, contrasenaSegura, LONGITUD_MINIMA_CONTRASENA } from '../../core/validadores';
import { Aviso } from '../../shared/aviso';
import { CampoContrasena } from '../../shared/campo-contrasena';

@Component({
  selector: 'geex-mi-cuenta',
  imports: [ReactiveFormsModule, MatButtonModule, MatIconModule, Aviso, CampoContrasena],
  template: `
    <section class="geex-pagina">
      <div class="geex-pagina__cabecera">
        <div>
          <h1>Mi cuenta</h1>
          <p>Tus datos de acceso y los despachos en los que trabajas.</p>
        </div>
      </div>

      <div class="columnas">
        @if (sesion.perfil(); as perfil) {
          <div class="geex-tarjeta">
            <h2>{{ perfil.nombre }} {{ perfil.apellidos }}</h2>
            <p class="geex-texto-suave">{{ perfil.email }}</p>
            @if (perfil.superadmin) { <span class="geex-etiqueta geex-etiqueta--info">Administrador de la plataforma</span> }
            <h3>Despachos</h3>
            <ul class="despachos">
              @for (d of perfil.despachos; track d.id) {
                <li>
                  <span>{{ d.nombre }} <small class="geex-texto-suave">· {{ d.rolEtiqueta }}</small></span>
                  @if (d.id === perfil.despachoActual.id) { <span class="geex-etiqueta geex-etiqueta--exito">Actual</span> }
                </li>
              }
            </ul>
          </div>
        }

        <div class="geex-tarjeta">
          <h2>Cambiar contraseña</h2>
          <p class="geex-texto-suave">Por seguridad, al cambiarla se cerrarán todas tus sesiones abiertas.</p>
          <form class="geex-formulario" [formGroup]="formulario" (ngSubmit)="cambiar()">
            <geex-campo-contrasena [control]="formulario.controls.actual" etiqueta="Contraseña actual" campoId="actual" />
            <geex-campo-contrasena [control]="formulario.controls.nueva" etiqueta="Contraseña nueva" campoId="nueva"
                                   autocompletar="new-password" [ayuda]="'Mínimo ' + minimo + ' caracteres'" />
            <geex-campo-contrasena [control]="formulario.controls.repetir" etiqueta="Repetir contraseña nueva"
                                   campoId="repetir" autocompletar="new-password" />
            @if (formulario.errors?.['noCoinciden'] && formulario.controls.repetir.touched) {
              <geex-aviso>Las contraseñas no coinciden.</geex-aviso>
            }
            <div class="geex-acciones">
              <button matButton="filled" type="submit" [disabled]="formulario.invalid || enviando()">
                <mat-icon>lock_reset</mat-icon> Cambiar contraseña
              </button>
            </div>
          </form>
        </div>
      </div>
    </section>
  `,
  styles: `
    .columnas { display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); gap: 20px; align-items: start; }
    h3 { font-size: 0.95rem; margin: 20px 0 8px; }
    .despachos { list-style: none; padding: 0; margin: 0; }
    .despachos li { display: flex; justify-content: space-between; align-items: center; padding: 10px 0;
      border-bottom: 1px solid var(--geex-borde); }
  `,
})
export class MiCuenta {
  private readonly cuenta = inject(CuentaService);
  private readonly avisos = inject(AvisosService);
  protected readonly sesion = inject(SesionService);

  protected readonly minimo = LONGITUD_MINIMA_CONTRASENA;
  protected readonly enviando = signal(false);
  protected readonly formulario = inject(NonNullableFormBuilder).group(
    {
      actual: ['', Validators.required],
      nueva: ['', [Validators.required, contrasenaSegura]],
      repetir: ['', Validators.required],
    },
    { validators: coinciden('nueva', 'repetir') },
  );

  async cambiar(): Promise<void> {
    this.enviando.set(true);
    const { actual, nueva } = this.formulario.getRawValue();
    try {
      await firstValueFrom(this.cuenta.cambiarContrasena({ actual, nueva }));
      this.avisos.exito('Contraseña cambiada. Vuelve a iniciar sesión con la nueva contraseña');
      await this.sesion.cerrarSesion();
    } catch (e) {
      this.avisos.error('No se pudo cambiar la contraseña', e);
    } finally {
      this.enviando.set(false);
    }
  }
}
