import { Routes } from '@angular/router';
import {
  exigirInstalado,
  exigirPermiso,
  exigirSesion,
  soloSinInstalar,
  soloSinSesion,
} from './core/sesion/guardas';
import { confirmarSalidaSinGuardar } from './core/ui/cambios-sin-guardar';

export const routes: Routes = [
  // ── Acceso (sin sesión) ───────────────────────────────────────────────────
  {
    path: 'instalacion',
    canActivate: [soloSinInstalar],
    title: 'Instalación · GEEX',
    loadComponent: () => import('./features/acceso/instalacion').then((m) => m.Instalacion),
  },
  {
    path: 'login',
    canActivate: [exigirInstalado, soloSinSesion],
    title: 'Iniciar sesión · GEEX',
    loadComponent: () => import('./features/acceso/login').then((m) => m.Login),
  },
  {
    path: 'recuperar',
    canActivate: [exigirInstalado],
    title: 'Recuperar contraseña · GEEX',
    loadComponent: () => import('./features/acceso/recuperar').then((m) => m.Recuperar),
  },
  {
    path: 'activar',
    title: 'Activar cuenta · GEEX',
    data: { modo: 'activar' },
    loadComponent: () => import('./features/acceso/elegir-contrasena').then((m) => m.ElegirContrasena),
  },
  {
    path: 'restablecer',
    title: 'Restablecer contraseña · GEEX',
    data: { modo: 'restablecer' },
    loadComponent: () => import('./features/acceso/elegir-contrasena').then((m) => m.ElegirContrasena),
  },

  // ── Aplicación (con sesión) ───────────────────────────────────────────────
  {
    path: '',
    canActivate: [exigirInstalado, exigirSesion],
    loadComponent: () => import('./layout/shell').then((m) => m.Shell),
    children: [
      {
        path: '',
        title: 'Inicio · GEEX',
        data: { titulo: 'Inicio', icono: 'home' },
        loadComponent: () => import('./features/inicio/inicio').then((m) => m.Inicio),
      },
      {
        path: 'configuracion',
        data: { titulo: 'Configuración', icono: 'settings' },
        loadComponent: () => import('./features/configuracion/configuracion-layout').then((m) => m.ConfiguracionLayout),
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'despacho' },
          {
            path: 'despacho',
            title: 'Datos del despacho · GEEX',
            canDeactivate: [confirmarSalidaSinGuardar],
            loadComponent: () => import('./features/configuracion/despacho/datos-despacho').then((m) => m.DatosDespacho),
          },
          {
            path: 'usuarios',
            title: 'Usuarios · GEEX',
            canActivate: [exigirPermiso('USUARIOS_GESTIONAR')],
            loadComponent: () => import('./features/configuracion/usuarios/usuarios').then((m) => m.Usuarios),
          },
          {
            path: 'roles',
            title: 'Roles y permisos · GEEX',
            canActivate: [exigirPermiso('PERMISOS_GESTIONAR')],
            canDeactivate: [confirmarSalidaSinGuardar],
            loadComponent: () => import('./features/configuracion/roles/roles').then((m) => m.Roles),
          },
          {
            path: 'listas',
            title: 'Listas desplegables · GEEX',
            loadComponent: () => import('./features/configuracion/listas/listas').then((m) => m.Listas),
          },
          {
            path: 'calendario',
            title: 'Calendario laboral · GEEX',
            loadComponent: () => import('./features/configuracion/calendario/calendario').then((m) => m.Calendario),
          },
          {
            path: 'auditoria',
            title: 'Auditoría · GEEX',
            canActivate: [exigirPermiso('AUDITORIA_VER')],
            loadComponent: () => import('./features/configuracion/auditoria/auditoria').then((m) => m.Auditoria),
          },
        ],
      },
      {
        path: 'mi-cuenta',
        title: 'Mi cuenta · GEEX',
        data: { titulo: 'Mi cuenta', icono: 'person' },
        loadComponent: () => import('./features/mi-cuenta/mi-cuenta').then((m) => m.MiCuenta),
      },
      {
        path: 'modulo/:clave',
        title: 'Próximamente · GEEX',
        loadComponent: () => import('./features/proximamente/proximamente').then((m) => m.Proximamente),
      },
      {
        path: 'sin-permiso',
        title: 'Sin permiso · GEEX',
        data: { titulo: 'Sin permiso', icono: 'lock' },
        loadComponent: () => import('./features/sin-permiso').then((m) => m.SinPermiso),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
