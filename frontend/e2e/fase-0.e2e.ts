import { expect, test } from '@playwright/test';
import { contrasenaNueva, Credenciales, enlaceDelCorreo, iniciarSesion, sinDesbordamientoHorizontal } from './soporte';

/*
 * Recorridos clave de la fase 0, en orden: cada prueba parte del estado que deja la anterior
 * (instalación → invitación → edición concurrente → revocación de acceso → móvil).
 */
// Sin reintentos: cada prueba depende del estado que deja la anterior (reintentar empezaría por instalar otra vez)
test.describe.configure({ mode: 'serial', retries: 0 });

const titular: Credenciales = { email: 'titular@despacho-e2e.test', contrasena: contrasenaNueva() };
const gestor: Credenciales = { email: 'gestor@despacho-e2e.test', contrasena: contrasenaNueva() };

test('instalación inicial: crea el despacho y entra como titular', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/instalacion$/);

  await page.locator('#nombreDespacho').fill('Despacho de Pruebas E2E');
  await page.locator('#municipio').fill('Alicante');
  await page.locator('#provincia').fill('Alicante');
  await page.locator('#nombre').fill('Ana');
  await page.locator('#apellidos').fill('Titular');
  await page.locator('#email').fill(titular.email);
  await page.locator('#contrasena').fill(titular.contrasena);
  await page.locator('#repetir').fill(titular.contrasena);
  await page.getByRole('button', { name: 'Crear despacho y entrar' }).click();

  await expect(page.getByRole('heading', { level: 1 })).toContainText('Ana');
  await expect(page.getByRole('link', { name: /Configuración/ }).first()).toBeVisible();

  // Una vez instalado, la instalación ya no está disponible
  await page.goto('/instalacion');
  await expect(page).not.toHaveURL(/\/instalacion$/);
});

test('un formulario vacío explica qué falta en lugar de desactivar el botón', async ({ page }) => {
  await page.goto('/login');
  await page.context().clearCookies();
  await page.goto('/login');
  const entrar = page.getByRole('button', { name: 'Entrar' });
  await expect(entrar).toBeEnabled();
  await entrar.click();
  await expect(page.getByText('Escribe tu correo electrónico')).toBeVisible();
});

test('invitación: el titular invita a un gestor, que activa su cuenta desde el correo', async ({ page }) => {
  await iniciarSesion(page, titular);
  await page.goto('/configuracion/usuarios');
  await page.getByRole('button', { name: 'Invitar usuario' }).click();
  const dialogo = page.getByRole('dialog');
  // El diálogo pone el foco en «Nombre» al terminar de abrirse: hasta entonces, lo que se escriba en otro
  // campo podría acabar en «Nombre» (pasó en CI)
  await expect(dialogo.getByLabel('Nombre')).toBeFocused();
  await dialogo.getByLabel('Nombre').fill('Luis');
  await dialogo.getByLabel('Apellidos').fill('Gestor');
  await dialogo.getByLabel('Correo electrónico').fill(gestor.email);
  const enviadaEn = new Date();
  await dialogo.getByRole('button', { name: 'Enviar invitación' }).click();
  await expect(page.getByText(gestor.email, { exact: true })).toBeVisible();

  await page.context().clearCookies();
  await page.goto(await enlaceDelCorreo(gestor.email, 'activar', enviadaEn));
  await expect(page.getByRole('heading', { name: 'Activa tu cuenta' })).toBeVisible();
  await page.locator('#contrasena').fill(gestor.contrasena);
  await page.locator('#repetir').fill(gestor.contrasena);
  await page.getByRole('button', { name: 'Activar cuenta y entrar' }).click();
  await expect(page.getByRole('heading', { level: 1 })).toContainText('Luis');
});

test('edición concurrente: quien guarda con datos desactualizados recibe un aviso, no pisa los cambios', async ({
  browser,
}) => {
  const primera = await (await browser.newContext()).newPage();
  const segunda = await (await browser.newContext()).newPage();
  await iniciarSesion(primera, titular);
  await iniciarSesion(segunda, titular);
  await primera.goto('/configuracion/despacho');
  await segunda.goto('/configuracion/despacho');

  const nombre = (p: typeof primera) => p.getByLabel('Nombre comercial');
  await nombre(primera).fill('Nombre guardado por la primera persona');
  await primera.getByRole('button', { name: 'Guardar cambios' }).click();
  await expect(primera.getByText('Datos del despacho guardados')).toBeVisible();

  await nombre(segunda).fill('Nombre de la segunda persona');
  await segunda.getByRole('button', { name: 'Guardar cambios' }).click();
  await expect(segunda.getByText(/Otro usuario ha modificado estos datos/)).toBeVisible();

  await segunda.reload();
  await expect(nombre(segunda)).toHaveValue('Nombre guardado por la primera persona');
});

test('revocación: al desactivar al gestor pierde el acceso al instante', async ({ browser }) => {
  const paginaGestor = await (await browser.newContext()).newPage();
  await iniciarSesion(paginaGestor, gestor);

  const paginaTitular = await (await browser.newContext()).newPage();
  await iniciarSesion(paginaTitular, titular);
  await paginaTitular.goto('/configuracion/usuarios');
  await paginaTitular.getByRole('row', { name: new RegExp(gestor.email) })
    .getByRole('button', { name: 'Acciones del usuario' }).click();
  await paginaTitular.getByRole('menuitem', { name: 'Desactivar acceso' }).click();
  await paginaTitular.getByRole('dialog').getByRole('button', { name: 'Desactivar' }).click();
  await expect(paginaTitular.getByRole('row', { name: new RegExp(gestor.email) })).toContainText('Desactivado');

  // El token del gestor (en memoria, sin recargar la página) aún no ha caducado, pero el servidor ya lo
  // rechaza en su siguiente acción, y la aplicación le lleva al inicio de sesión
  // Se lee la respuesta en la red (route.fetch): la aplicación navega al login enseguida y el navegador
  // descartaría el cuerpo antes de poder leerlo con waitForResponse
  const codigos: string[] = [];
  await paginaGestor.route('**/api/despacho', async (ruta) => {
    const respuesta = await ruta.fetch();
    if (respuesta.status() === 401) {
      codigos.push((await respuesta.json()).codigo);
    }
    await ruta.fulfill({ response: respuesta });
  });
  await paginaGestor.getByRole('link', { name: /Configuración/ }).first().click();
  await expect(paginaGestor).toHaveURL(/\/login/);
  expect(codigos).toContain('SESION_REVOCADA');
});

test('móvil: las pantallas principales caben en 390 px sin desplazamiento horizontal', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await iniciarSesion(page, titular);
  for (const ruta of ['/', '/configuracion/despacho', '/configuracion/usuarios', '/configuracion/roles',
    '/configuracion/listas', '/configuracion/calendario', '/configuracion/auditoria', '/mi-cuenta']) {
    await page.goto(ruta);
    await page.waitForLoadState('networkidle');
    await sinDesbordamientoHorizontal(page);
  }
  // En móvil, la sección de configuración se elige en un selector visible
  await page.goto('/configuracion/usuarios');
  await expect(page.getByRole('button', { name: 'Cambiar de sección de configuración' })).toBeVisible();
});
