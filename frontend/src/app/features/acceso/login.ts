import { Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterLink } from '@angular/router';
import { mensajeDeError } from '../../core/errores';
import { SesionService } from '../../core/sesion/sesion.service';
import { Aviso } from '../../shared/aviso';
import { CampoContrasena } from '../../shared/campo-contrasena';
import { AccesoLayout } from './acceso-layout';

@Component({
  selector: 'geex-login',
  imports: [ReactiveFormsModule, RouterLink, AccesoLayout, Aviso, CampoContrasena, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <geex-acceso-layout>
      <h2 class="titulo">Iniciar sesión</h2>
      <p class="geex-texto-suave subtitulo">Accede con tu correo electrónico del despacho.</p>

      @if (motivo() === 'caducada') {
        <geex-aviso tipo="advertencia">Tu sesión ha caducado. Vuelve a iniciar sesión.</geex-aviso>
      }
      @if (error()) {
        <geex-aviso>{{ error() }}</geex-aviso>
      }

      <form class="geex-formulario" [formGroup]="formulario" (ngSubmit)="entrar()">
        <mat-form-field>
          <mat-label>Correo electrónico</mat-label>
          <input matInput id="email" type="email" formControlName="email" autocomplete="username" />
          @if (formulario.controls.email.hasError('required')) {
            <mat-error>Escribe tu correo electrónico</mat-error>
          } @else if (formulario.controls.email.hasError('email')) {
            <mat-error>Correo electrónico no válido</mat-error>
          }
        </mat-form-field>
        <geex-campo-contrasena [control]="formulario.controls.contrasena" />
        <button matButton="filled" type="submit" class="entrar" [disabled]="enviando()">
          @if (enviando()) { <mat-spinner diameter="20" /> } @else { <mat-icon>login</mat-icon> }
          Entrar
        </button>
        <a routerLink="/recuperar" class="olvido">¿Has olvidado tu contraseña?</a>
      </form>
    </geex-acceso-layout>
  `,
  styles: `
    .titulo { font-size: 1.5rem; }
    .subtitulo { margin: 6px 0 20px; }
    .entrar { height: 44px; margin-top: 4px; }
    .olvido { text-align: center; font-size: 0.9rem; margin-top: 12px; }
  `,
})
export class Login {
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  /** Parámetros de la URL (?motivo=caducada&volver=/ruta). */
  readonly motivo = input<string>();
  readonly volver = input<string>();

  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly formulario = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email]],
    contrasena: ['', Validators.required],
  });

  async entrar(): Promise<void> {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    try {
      const { email, contrasena } = this.formulario.getRawValue();
      await this.sesion.iniciarSesion(email, contrasena);
      const destino = this.volver();
      await this.router.navigateByUrl(destino?.startsWith('/') ? destino : '/');
    } catch (e) {
      this.error.set(mensajeDeError(e));
    } finally {
      this.enviando.set(false);
    }
  }
}
