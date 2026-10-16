import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, finalize, firstValueFrom, map, Observable, of, shareReplay, switchMap, tap } from 'rxjs';
import { AutenticacionService, PerfilService } from '../../api/generated';
import { Perfil } from '../../api/generated/model/perfil';
import { TokenAccesoRespuesta } from '../../api/generated/model/tokenAccesoRespuesta';

/** Permiso de la aplicación (mismos valores que el enum Permiso del backend). */
export type Permiso = Perfil['permisos'][number];

/** Margen para renovar el token de acceso antes de que caduque. */
const MARGEN_RENOVACION_MS = 60_000;

/**
 * Estado de la sesión del usuario.
 *
 * - El token de acceso (JWT, 15 min) vive solo en memoria: no se guarda en
 *   localStorage, así un script inyectado no puede robarlo.
 * - El token de refresco va en una cookie HttpOnly que el navegador envía solo
 *   a /api/auth; JavaScript nunca la ve.
 * - Antes de caducar, el token de acceso se renueva solo.
 */
@Injectable({ providedIn: 'root' })
export class SesionService {
  private readonly autenticacion = inject(AutenticacionService);
  private readonly perfilApi = inject(PerfilService);
  private readonly router = inject(Router);

  private readonly _token = signal<string | null>(null);
  private readonly _perfil = signal<Perfil | null>(null);
  private temporizador: ReturnType<typeof setTimeout> | undefined;
  private refrescoEnCurso: Observable<boolean> | null = null;

  readonly token = this._token.asReadonly();
  readonly perfil = this._perfil.asReadonly();
  readonly autenticado = computed(() => this._token() !== null && this._perfil() !== null);
  readonly permisos = computed(() => new Set<string>(this._perfil()?.permisos ?? []));

  tienePermiso(permiso: Permiso): boolean {
    return this.permisos().has(permiso);
  }

  async iniciarSesion(email: string, contrasena: string): Promise<void> {
    const respuesta = await firstValueFrom(this.autenticacion.login({ email, contrasena }));
    await this.aplicarToken(respuesta);
  }

  /** Al arrancar la aplicación: si la cookie de refresco sigue viva, recupera la sesión sin pedir contraseña. */
  async restaurar(): Promise<void> {
    await firstValueFrom(this.refrescar());
  }

  /**
   * Renueva el token de acceso. Si varias peticiones lo necesitan a la vez, solo
   * se hace una llamada (el servidor rota el token de refresco en cada uso y
   * rechazaría la segunda por reutilización).
   */
  refrescar(): Observable<boolean> {
    if (!this.refrescoEnCurso) {
      this.refrescoEnCurso = this.autenticacion.refrescar('1').pipe(
        switchMap((respuesta) => this.aplicarToken(respuesta)),
        map(() => true),
        catchError(() => {
          this.limpiar();
          return of(false);
        }),
        finalize(() => (this.refrescoEnCurso = null)),
        shareReplay(1),
      );
    }
    return this.refrescoEnCurso;
  }

  async cambiarDespacho(despachoId: string): Promise<void> {
    const respuesta = await firstValueFrom(this.autenticacion.cambiarDespacho({ despachoId }));
    await this.aplicarToken(respuesta);
    await this.router.navigateByUrl('/');
  }

  async recargarPerfil(): Promise<void> {
    this._perfil.set(await firstValueFrom(this.perfilApi.obtenerPerfil()));
  }

  async cerrarSesion(): Promise<void> {
    try {
      await firstValueFrom(this.autenticacion.logout());
    } catch {
      // Aunque falle la llamada, la sesión local se cierra igualmente
    }
    this.limpiar();
    await this.router.navigateByUrl('/login');
  }

  /** Sesión invalidada en el servidor (contraseña cambiada, acceso retirado...). */
  async sesionCaducada(): Promise<void> {
    this.limpiar();
    await this.router.navigate(['/login'], { queryParams: { motivo: 'caducada' } });
  }

  private async aplicarToken(respuesta: TokenAccesoRespuesta): Promise<void> {
    this._token.set(respuesta.tokenAcceso);
    this.programarRenovacion(respuesta.expiraEn);
    this._perfil.set(await firstValueFrom(this.perfilApi.obtenerPerfil()));
  }

  private programarRenovacion(expiraEn: string): void {
    clearTimeout(this.temporizador);
    const espera = Math.max(new Date(expiraEn).getTime() - Date.now() - MARGEN_RENOVACION_MS, 5_000);
    this.temporizador = setTimeout(() => this.refrescar().pipe(tap((ok) => !ok && this.sesionCaducada())).subscribe(), espera);
  }

  private limpiar(): void {
    clearTimeout(this.temporizador);
    this._token.set(null);
    this._perfil.set(null);
  }
}
