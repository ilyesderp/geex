import { expect, Page } from '@playwright/test';
import { randomBytes } from 'node:crypto';

/** Utilidades compartidas por las pruebas de extremo a extremo. */

const MAILPIT = process.env['GEEX_E2E_MAILPIT'] ?? 'http://localhost:8025';

export interface Credenciales {
  email: string;
  contrasena: string;
}

/** Contraseña aleatoria en cada ejecución: no hay ninguna contraseña fija en el código. */
export function contrasenaNueva(): string {
  return 'Clave-' + randomBytes(12).toString('base64url');
}

export async function iniciarSesion(page: Page, c: Credenciales): Promise<void> {
  await page.goto('/login');
  await page.locator('#email').fill(c.email);
  await page.locator('#contrasena').fill(c.contrasena);
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('heading', { level: 1 })).toContainText('Buen');
}

/** Ruta (con el token) del último enlace de un tipo enviado a un correo, leída de Mailpit. */
export async function enlaceDelCorreo(destinatario: string, tipo: 'activar' | 'restablecer'): Promise<string> {
  for (let intento = 0; intento < 20; intento++) {
    const lista = await (await fetch(`${MAILPIT}/api/v1/messages?limit=50`)).json();
    const mensaje = lista.messages?.find((m: { To: { Address: string }[] }) =>
      m.To.some((t) => t.Address.toLowerCase() === destinatario.toLowerCase()));
    if (mensaje) {
      const detalle = await (await fetch(`${MAILPIT}/api/v1/message/${mensaje.ID}`)).json();
      const enlace = (detalle.Text as string).match(new RegExp(`https?://\\S+/${tipo}\\?token=[\\w-]+`));
      if (enlace) {
        return enlace[0].replace(/^https?:\/\/[^/]+/, '');
      }
    }
    await new Promise((r) => setTimeout(r, 500));
  }
  throw new Error(`No ha llegado el correo de tipo «${tipo}» a ${destinatario}`);
}

/**
 * Nada debe salirse por la derecha: ni desplazamiento horizontal de la página, ni elementos recortados
 * (un contenedor con overflow oculto no genera barra de desplazamiento pero corta el contenido).
 * Se excluyen los elementos dentro de zonas desplazables a propósito (tablas anchas en móvil).
 */
export async function sinDesbordamientoHorizontal(page: Page): Promise<void> {
  const { ancho, fuera } = await page.evaluate(() => {
    const vw = document.documentElement.clientWidth;
    const dentroDeZonaDesplazable = (el: Element) => !!el.closest('.geex-tabla-contenedor, .cdk-overlay-container');
    const fuera = [...document.querySelectorAll('body *')]
      .filter((el) => { const r = el.getBoundingClientRect(); return r.width > 0 && r.right > vw + 1 && !dentroDeZonaDesplazable(el); })
      .slice(0, 5)
      .map((el) => `${el.tagName.toLowerCase()}.${[...el.classList].slice(0, 2).join('.')} (derecha=${Math.round(el.getBoundingClientRect().right)})`);
    return { ancho: document.documentElement.scrollWidth, fuera };
  });
  expect(fuera, `elementos que se salen por la derecha en ${page.url()} (${page.viewportSize()!.width} px)`).toEqual([]);
  expect(ancho, `desplazamiento horizontal en ${page.url()}`).toBeLessThanOrEqual(page.viewportSize()!.width);
}
