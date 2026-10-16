import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { CuentaService } from '../../api/generated';
import { mensajeDeError } from '../../core/errores';
import { Aviso } from '../../shared/aviso';
import { AccesoLayout } from './acceso-layout';

@Component({
  selector: 'geex-recuperar',
  imports: [ReactiveFormsModule, RouterLink, AccesoLayout, Aviso, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatIconModule],
  template: `
    <geex-acceso-layout>
      <h2 class="titulo">Recuperar contraseña</h2>
      @if (enviado()) {
        <geex-aviso tipo="exito">
          Si el correo pertenece a una cuenta activa, recibirás un enlace para elegir una contraseña nueva.
          El enlace caduca en una hora.
        </geex-aviso>
        <a routerLink="/login">Volver a iniciar sesión</a>
      } @else {
        <p class="geex-texto-suave subtitulo">Te enviaremos un enlace para elegir una contraseña nueva.</p>
        @if (error()) {
          <geex-aviso>{{ error() }}</geex-aviso>
        }
        <form class="geex-formulario" [formGroup]="formulario" (ngSubmit)="enviar()">
          <mat-form-field>
            <mat-label>Correo electrónico</mat-label>
            <input matInput id="email" type="email" formControlName="email" autocomplete="username" />
            @if (formulario.controls.email.hasError('required')) {
              <mat-error>Escribe tu correo electrónico</mat-error>
            } @else if (formulario.controls.email.hasError('email')) {
              <mat-error>Correo electrónico no válido</mat-error>
            }
          </mat-form-field>
          <button matButton="filled" type="submit" [disabled]="enviando()">
            <mat-icon>send</mat-icon> Enviar enlace
          </button>
          <a routerLink="/login" class="volver">Volver a iniciar sesión</a>
        </form>
      }
    </geex-acceso-layout>
  `,
  styles: `
    .titulo { margin-bottom: 16px; }
    .subtitulo { margin: -10px 0 20px; }
    .volver { text-align: center; font-size: 0.9rem; margin-top: 12px; }
  `,
})
export class Recuperar {
  private readonly cuenta = inject(CuentaService);
  protected readonly enviando = signal(false);
  protected readonly enviado = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly formulario = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email]],
  });

  async enviar(): Promise<void> {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    try {
      await firstValueFrom(this.cuenta.recuperar({ email: this.formulario.getRawValue().email }));
      this.enviado.set(true);
    } catch (e) {
      this.error.set(mensajeDeError(e));
    } finally {
      this.enviando.set(false);
    }
  }
}
