import { Component, input, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

/** Campo de contraseña con botón para mostrarla u ocultarla. */
@Component({
  selector: 'geex-campo-contrasena',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatIconModule, MatButtonModule],
  template: `
    <mat-form-field>
      <mat-label>{{ etiqueta() }}</mat-label>
      <input matInput [id]="campoId()" [type]="visible() ? 'text' : 'password'" [formControl]="control()"
             [autocomplete]="autocompletar()" />
      <button matIconButton matSuffix type="button" (click)="visible.set(!visible())"
              [attr.aria-label]="visible() ? 'Ocultar contraseña' : 'Mostrar contraseña'">
        <mat-icon>{{ visible() ? 'visibility_off' : 'visibility' }}</mat-icon>
      </button>
      @if (ayuda()) {
        <mat-hint>{{ ayuda() }}</mat-hint>
      }
      @if (control().hasError('required')) {
        <mat-error>Obligatorio</mat-error>
      } @else if (control().hasError('contrasenaCorta') || control().hasError('contrasenaSimple')) {
        <mat-error>Mínimo 12 caracteres y no demasiado simple</mat-error>
      }
    </mat-form-field>
  `,
  styles: `mat-form-field { width: 100%; }`,
})
export class CampoContrasena {
  readonly control = input.required<FormControl<string>>();
  readonly etiqueta = input('Contraseña');
  readonly campoId = input('contrasena');
  readonly autocompletar = input<'current-password' | 'new-password'>('current-password');
  readonly ayuda = input<string>();
  protected readonly visible = signal(false);
}
