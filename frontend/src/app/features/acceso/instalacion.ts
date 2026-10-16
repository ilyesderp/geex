import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { firstValueFrom } from 'rxjs';
import { InstalacionService } from '../../api/generated';
import { COMUNIDADES, ComunidadAutonoma } from '../../core/comunidades';
import { mensajeDeError } from '../../core/errores';
import { marcarInstalado } from '../../core/sesion/guardas';
import { SesionService } from '../../core/sesion/sesion.service';
import { coinciden, contrasenaSegura, LONGITUD_MINIMA_CONTRASENA } from '../../core/validadores';
import { Aviso } from '../../shared/aviso';
import { CampoContrasena } from '../../shared/campo-contrasena';
import { AccesoLayout } from './acceso-layout';

/** Primera puesta en marcha: crea el despacho y la cuenta del titular. */
@Component({
  selector: 'geex-instalacion',
  imports: [ReactiveFormsModule, AccesoLayout, Aviso, CampoContrasena, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatIconModule],
  templateUrl: './instalacion.html',
  styles: `
    .subtitulo { margin: 6px 0 24px; }
  `,
})
export class Instalacion {
  private readonly instalacion = inject(InstalacionService);
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  protected readonly comunidades = COMUNIDADES;
  protected readonly minimo = LONGITUD_MINIMA_CONTRASENA;
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly formulario = inject(NonNullableFormBuilder).group(
    {
      nombreDespacho: ['', [Validators.required, Validators.maxLength(150)]],
      municipio: ['Alicante', [Validators.required, Validators.maxLength(120)]],
      provincia: ['Alicante', [Validators.required, Validators.maxLength(120)]],
      comunidadAutonoma: ['COMUNITAT_VALENCIANA' as ComunidadAutonoma, Validators.required],
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      apellidos: ['', [Validators.required, Validators.maxLength(150)]],
      email: ['', [Validators.required, Validators.email]],
      contrasena: ['', [Validators.required, contrasenaSegura]],
      repetir: ['', Validators.required],
    },
    { validators: coinciden('contrasena', 'repetir') },
  );

  async instalar(): Promise<void> {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    const v = this.formulario.getRawValue();
    const datos = {
      nombreDespacho: v.nombreDespacho,
      municipio: v.municipio,
      provincia: v.provincia,
      comunidadAutonoma: v.comunidadAutonoma,
      nombre: v.nombre,
      apellidos: v.apellidos,
      email: v.email,
      contrasena: v.contrasena,
    };
    try {
      await firstValueFrom(this.instalacion.instalar(datos));
      marcarInstalado();
      await this.sesion.iniciarSesion(datos.email, datos.contrasena);
      await this.router.navigateByUrl('/');
    } catch (e) {
      this.error.set(mensajeDeError(e));
    } finally {
      this.enviando.set(false);
    }
  }
}
