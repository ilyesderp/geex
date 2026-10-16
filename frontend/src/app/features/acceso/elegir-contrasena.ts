import { Component, inject, input, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { CuentaService } from '../../api/generated';
import { mensajeDeError } from '../../core/errores';
import { SesionService } from '../../core/sesion/sesion.service';
import { coinciden, contrasenaSegura, LONGITUD_MINIMA_CONTRASENA } from '../../core/validadores';
import { Aviso } from '../../shared/aviso';
import { CampoContrasena } from '../../shared/campo-contrasena';
import { AccesoLayout } from './acceso-layout';

/**
 * Elegir contraseña desde un enlace recibido por correo: activación de una
 * cuenta invitada o restablecimiento de contraseña.
 */
@Component({
  selector: 'geex-elegir-contrasena',
  imports: [ReactiveFormsModule, RouterLink, AccesoLayout, Aviso, CampoContrasena, MatButtonModule, MatIconModule,
    MatProgressSpinnerModule],
  template: `
    <geex-acceso-layout>
      @if (cargando()) {
        <div class="geex-centrado"><mat-spinner diameter="40" /></div>
      } @else if (enlaceInvalido()) {
        <h2 class="titulo">Enlace no válido</h2>
        <geex-aviso tipo="advertencia">{{ enlaceInvalido() }}</geex-aviso>
        <a [routerLink]="activar ? '/login' : '/recuperar'">
          {{ activar ? 'Ir a iniciar sesión' : 'Solicitar un enlace nuevo' }}
        </a>
      } @else {
        <h2>{{ activar ? 'Activa tu cuenta' : 'Elige una contraseña nueva' }}</h2>
        <p class="geex-texto-suave subtitulo">
          {{ activar ? 'Hola ' + nombre() + ', elige la contraseña con la que entrarás a GEEX.' : 'Cuenta: ' + email() }}
        </p>
        @if (error()) {
          <geex-aviso>{{ error() }}</geex-aviso>
        }
        <form class="geex-formulario" [formGroup]="formulario" (ngSubmit)="guardar()">
          <geex-campo-contrasena [control]="formulario.controls.contrasena" autocompletar="new-password"
                                 [ayuda]="'Mínimo ' + minimo + ' caracteres. Una frase es más segura'" />
          <geex-campo-contrasena [control]="formulario.controls.repetir" etiqueta="Repetir contraseña" campoId="repetir"
                                 autocompletar="new-password" />
          @if (formulario.errors?.['noCoinciden'] && formulario.controls.repetir.touched) {
            <geex-aviso>Las contraseñas no coinciden.</geex-aviso>
          }
          <button matButton="filled" type="submit" [disabled]="enviando()">
            <mat-icon>check</mat-icon> {{ activar ? 'Activar cuenta y entrar' : 'Guardar contraseña' }}
          </button>
        </form>
      }
    </geex-acceso-layout>
  `,
  styles: `
    .titulo { margin-bottom: 16px; }
    .subtitulo { margin: 6px 0 20px; }
  `,
})
export class ElegirContrasena implements OnInit {
  private readonly cuenta = inject(CuentaService);
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  /** Token del enlace (?token=...). */
  readonly token = input<string>();
  protected readonly activar = inject(ActivatedRoute).snapshot.data['modo'] === 'activar';

  protected readonly minimo = LONGITUD_MINIMA_CONTRASENA;
  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly enlaceInvalido = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly email = signal('');
  protected readonly nombre = signal('');

  protected readonly formulario = inject(NonNullableFormBuilder).group(
    {
      contrasena: ['', [Validators.required, contrasenaSegura]],
      repetir: ['', Validators.required],
    },
    { validators: coinciden('contrasena', 'repetir') },
  );

  async ngOnInit(): Promise<void> {
    const token = this.token();
    if (!token) {
      this.enlaceInvalido.set('Falta el código del enlace. Abre el enlace completo del correo.');
      this.cargando.set(false);
      return;
    }
    try {
      const info = await firstValueFrom(this.cuenta.consultarEnlace({ token }));
      this.email.set(info.email);
      this.nombre.set(info.nombre);
    } catch (e) {
      this.enlaceInvalido.set(mensajeDeError(e));
    } finally {
      this.cargando.set(false);
    }
  }

  async guardar(): Promise<void> {
    const token = this.token();
    if (!token) {
      return;
    }
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    const { contrasena } = this.formulario.getRawValue();
    try {
      if (this.activar) {
        await firstValueFrom(this.cuenta.activar({ token, contrasena }));
      } else {
        await firstValueFrom(this.cuenta.restablecer({ token, contrasena }));
      }
      await this.sesion.iniciarSesion(this.email(), contrasena);
      await this.router.navigateByUrl('/');
    } catch (e) {
      this.error.set(mensajeDeError(e));
    } finally {
      this.enviando.set(false);
    }
  }
}
