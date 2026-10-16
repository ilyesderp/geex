import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { autenticacionInterceptor } from './autenticacion.interceptor';
import { SesionService } from './sesion.service';

describe('autenticacionInterceptor', () => {
  const token = signal<string | null>('token-1');
  const sesion = {
    token,
    refrescar: vi.fn(),
    sesionCaducada: vi.fn(),
  };
  let http: HttpClient;
  let servidor: HttpTestingController;

  beforeEach(() => {
    token.set('token-1');
    sesion.refrescar.mockReset();
    sesion.sesionCaducada.mockReset();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([autenticacionInterceptor])),
        provideHttpClientTesting(),
        { provide: SesionService, useValue: sesion },
      ],
    });
    http = TestBed.inject(HttpClient);
    servidor = TestBed.inject(HttpTestingController);
  });

  afterEach(() => servidor.verify());

  it('añade el token a las llamadas privadas de la API', () => {
    http.get('/api/yo').subscribe();
    expect(servidor.expectOne('/api/yo').request.headers.get('Authorization')).toBe('Bearer token-1');
  });

  it('no envía el token a los endpoints públicos', () => {
    http.post('/api/auth/login', {}).subscribe();
    expect(servidor.expectOne('/api/auth/login').request.headers.has('Authorization')).toBe(false);
  });

  it('añade la cabecera anti-CSRF a las llamadas de autenticación y solo a ellas', () => {
    http.post('/api/auth/logout', null).subscribe();
    http.post('/api/auth/cambiar-despacho', {}).subscribe();
    http.get('/api/yo').subscribe();
    expect(servidor.expectOne('/api/auth/logout').request.headers.get('X-GEEX-Refresco')).toBe('1');
    expect(servidor.expectOne('/api/auth/cambiar-despacho').request.headers.get('X-GEEX-Refresco')).toBe('1');
    expect(servidor.expectOne('/api/yo').request.headers.has('X-GEEX-Refresco')).toBe(false);
  });

  it('ante un 401 renueva la sesión una vez y repite la petición con el token nuevo', () => {
    sesion.refrescar.mockImplementation(() => {
      token.set('token-2');
      return of(true);
    });
    let respuesta: unknown;
    http.get('/api/usuarios').subscribe((r) => (respuesta = r));

    servidor.expectOne('/api/usuarios').flush(null, { status: 401, statusText: 'Unauthorized' });
    const reintento = servidor.expectOne('/api/usuarios');
    expect(reintento.request.headers.get('Authorization')).toBe('Bearer token-2');
    reintento.flush([{ id: '1' }]);

    expect(sesion.refrescar).toHaveBeenCalledTimes(1);
    expect(respuesta).toEqual([{ id: '1' }]);
  });

  it('si no se puede renovar, cierra la sesión', () => {
    sesion.refrescar.mockReturnValue(of(false));
    http.get('/api/usuarios').subscribe({ error: () => undefined });

    servidor.expectOne('/api/usuarios').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(sesion.sesionCaducada).toHaveBeenCalled();
  });
});
