import { MODULOS, moduloPorClave } from './modulos';

describe('catálogo de módulos de la pantalla de inicio', () => {
  it('contiene los 24 apartados del documento, numerados del 1 al 24', () => {
    expect(MODULOS).toHaveLength(24);
    expect(MODULOS.map((m) => m.numero)).toEqual(Array.from({ length: 24 }, (_, i) => i + 1));
  });

  it('no repite claves', () => {
    expect(new Set(MODULOS.map((m) => m.clave)).size).toBe(MODULOS.length);
  });

  it('en la fase 0 solo está disponible Configuración', () => {
    expect(MODULOS.filter((m) => m.ruta).map((m) => m.clave)).toEqual(['configuracion']);
    expect(moduloPorClave('plazos')?.fase).toBe(1);
  });
});
