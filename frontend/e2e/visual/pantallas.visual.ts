import AxeBuilder from '@axe-core/playwright';
import { expect, Page, test } from '@playwright/test';
import { sinDesbordamientoHorizontal } from '../soporte';
import { simularApi } from './api-simulada';

/*
 * Regresión visual y accesibilidad de las pantallas principales, en escritorio (100 %) y en móvil
 * (390 px con escalado 125 %, el caso que delata los defectos de subpíxel).
 *
 * - Captura de cada pantalla comparada con su imagen de referencia: cualquier cambio visual hace fallar
 *   la prueba y deja la imagen de diferencias en el informe. Si el cambio es intencionado, se regeneran las
 *   referencias (`npm run e2e:visual -- --update-snapshots`) y se revisan en el PR.
 * - Comprobación automática de accesibilidad (axe-core, criterios WCAG 2.1 A y AA).
 *
 * Las referencias se generan en Linux con la imagen oficial de Playwright (igual en local y en CI): ver
 * `e2e/visual/ejecutar.mjs`.
 */

interface Pantalla {
  nombre: string;
  ruta: string;
  conSesion: boolean;
  /** Acción previa a la captura (abrir un diálogo, provocar errores…). */
  accion?: (page: Page) => Promise<void>;
}

const abrirInvitacion = (page: Page) => page.getByRole('button', { name: 'Invitar usuario' }).click();

const PANTALLAS: Pantalla[] = [
  { nombre: 'login', ruta: '/login', conSesion: false },
  { nombre: 'inicio', ruta: '/', conSesion: true },
  { nombre: 'despacho', ruta: '/configuracion/despacho', conSesion: true },
  { nombre: 'usuarios', ruta: '/configuracion/usuarios', conSesion: true },
  { nombre: 'roles', ruta: '/configuracion/roles', conSesion: true },
  { nombre: 'listas', ruta: '/configuracion/listas', conSesion: true },
  { nombre: 'calendario', ruta: '/configuracion/calendario', conSesion: true },
  { nombre: 'auditoria', ruta: '/configuracion/auditoria', conSesion: true },
  { nombre: 'mi-cuenta', ruta: '/mi-cuenta', conSesion: true },
  // Diálogos: el primer campo recibe el foco y su etiqueta flota (caso que se recortaba)
  { nombre: 'dialogo-invitar', ruta: '/configuracion/usuarios', conSesion: true, accion: abrirInvitacion },
  {
    nombre: 'dialogo-invitar-errores', ruta: '/configuracion/usuarios', conSesion: true,
    accion: async (page) => {
      await abrirInvitacion(page);
      await page.getByRole('button', { name: 'Enviar invitación' }).click();
    },
  },
  {
    nombre: 'dialogo-festivo', ruta: '/configuracion/calendario', conSesion: true,
    accion: (page) => page.getByRole('button', { name: /Festivo nacional/ }).click(),
  },
];

for (const pantalla of PANTALLAS) {
  test.describe(pantalla.nombre, () => {
    test.beforeEach(async ({ page }) => {
      // Reloj fijo: el saludo («Buenos días») y las fechas no cambian entre ejecuciones
      await page.clock.setFixedTime(new Date('2026-10-16T10:00:00+02:00'));
      await simularApi(page, { conSesion: pantalla.conSesion });
      await page.goto(pantalla.ruta);
      await page.waitForLoadState('networkidle');
      await page.evaluate(() => document.fonts.ready);
      if (pantalla.accion) {
        await pantalla.accion(page);
        await page.waitForTimeout(400); // fin de la animación de apertura del diálogo
      }
    });

    test('aspecto', async ({ page }) => {
      // Con un diálogo abierto se captura lo que se ve en pantalla (la capa oscura cubre solo la ventana)
      await expect(page).toHaveScreenshot(`${pantalla.nombre}.png`, { fullPage: !pantalla.accion });
    });

    test('accesibilidad', async ({ page }) => {
      const resultado = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze();
      const fallos = resultado.violations.map((v) =>
        `${v.id} (${v.impact}): ${v.help}\n    ${v.nodes.slice(0, 3).map((n) => n.target.join(' ')).join('\n    ')}`);
      expect(fallos, fallos.join('\n')).toEqual([]);
    });
  });
}

/*
 * Pasar de escritorio a móvil SIN recargar (como al girar una tableta o estrechar la ventana): cada pantalla
 * debe recolocarse sin que nada se salga por la derecha.
 */
test.describe('de escritorio a móvil sin recargar', () => {
  test.skip(({ isMobile }) => isMobile, 'solo tiene sentido partiendo de escritorio');
  for (const pantalla of PANTALLAS.filter((p) => !p.accion)) {
    test(pantalla.nombre, async ({ page }) => {
      await page.clock.setFixedTime(new Date('2026-10-16T10:00:00+02:00'));
      await simularApi(page, { conSesion: pantalla.conSesion });
      await page.goto(pantalla.ruta);
      await page.waitForLoadState('networkidle');
      for (const ancho of [768, 440, 390, 360]) {
        await page.setViewportSize({ width: ancho, height: 900 });
        await page.waitForTimeout(300);
        await sinDesbordamientoHorizontal(page);
      }
    });
  }
});

