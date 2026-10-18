import type { CalendarioAnual } from '../../src/app/api/generated/model/calendarioAnual';
import type { DescripcionPermiso } from '../../src/app/api/generated/model/descripcionPermiso';
import type { DespachoVista } from '../../src/app/api/generated/model/despachoVista';
import type { PaginaAuditoria } from '../../src/app/api/generated/model/paginaAuditoria';
import type { Perfil } from '../../src/app/api/generated/model/perfil';
import type { PermisosDeRol } from '../../src/app/api/generated/model/permisosDeRol';
import type { ResumenCatalogo } from '../../src/app/api/generated/model/resumenCatalogo';
import type { UsuarioDespacho } from '../../src/app/api/generated/model/usuarioDespacho';
import type { ValorVista } from '../../src/app/api/generated/model/valorVista';

/*
 * Datos de demostración fijos para las pruebas visuales. Están tipados con el cliente generado de la API:
 * si el contrato cambia, esto deja de compilar en vez de quedarse desfasado en silencio.
 * Todo es ficticio (nombres, correos, despacho).
 */

const despachoAcceso = { id: 'd-demo', nombre: 'Extranjería Martínez & Asociados', rol: 'TITULAR', rolEtiqueta: 'Titular / Administrador' } as const;

export const perfil: Perfil = {
  usuarioId: 'u-ana',
  nombre: 'Ana',
  apellidos: 'Martínez Soler',
  email: 'ana.martinez@despacho-demo.es',
  superadmin: true,
  despachoActual: despachoAcceso,
  despachos: [despachoAcceso],
  permisos: ['CONTACTOS_VER', 'USUARIOS_GESTIONAR', 'PERMISOS_GESTIONAR', 'CONFIGURACION_GESTIONAR', 'AUDITORIA_VER'],
};

export const despacho: DespachoVista = {
  id: 'd-demo',
  nombre: 'Extranjería Martínez & Asociados',
  razonSocial: 'Martínez Asesores, S.L.',
  nif: 'B00000000',
  direccion: 'Calle Mayor, 1, 2.º',
  codigoPostal: '03002',
  municipio: 'Alicante',
  provincia: 'Alicante',
  comunidadAutonoma: 'COMUNITAT_VALENCIANA',
  telefono: '965 000 000',
  email: 'info@despacho-demo.es',
  web: 'https://despacho-demo.es',
  creadoEn: '2026-10-01T08:00:00Z',
  actualizadoEn: '2026-10-10T08:00:00Z',
  version: 3,
};

export const usuarios: UsuarioDespacho[] = [
  { id: 'u-ana', nombre: 'Ana', apellidos: 'Martínez Soler', email: 'ana.martinez@despacho-demo.es', esUstedMismo: true,
    estado: 'ACTIVO', rol: 'TITULAR', rolEtiqueta: 'Titular / Administrador', ultimoAcceso: '2026-10-16T07:45:00Z' },
  { id: 'u-carmen', nombre: 'Carmen', apellidos: 'Ruiz Pérez', email: 'carmen.ruiz@despacho-demo.es', esUstedMismo: false,
    estado: 'ACTIVO', rol: 'ABOGADO', rolEtiqueta: 'Abogado / Revisor', ultimoAcceso: '2026-10-15T16:20:00Z' },
  { id: 'u-javier', nombre: 'Javier', apellidos: 'López Fernández de la Torre', email: 'javier.lopez@despacho-demo.es',
    esUstedMismo: false, estado: 'PENDIENTE_ACTIVACION', rol: 'ADMINISTRATIVO', rolEtiqueta: 'Administrativo / Recepción',
    ultimoAcceso: undefined as unknown as string },
  { id: 'u-luis', nombre: 'Luis', apellidos: 'Martín Soler', email: 'luis.martin@despacho-demo.es', esUstedMismo: false,
    estado: 'DESACTIVADO', rol: 'GESTOR', rolEtiqueta: 'Gestor / Tramitador', ultimoAcceso: '2026-09-30T09:10:00Z' },
];

export const catalogoPermisos: DescripcionPermiso[] = [
  { permiso: 'CONTACTOS_VER', grupo: 'Contactos', descripcion: 'Ver contactos' },
  { permiso: 'CONTACTOS_EDITAR', grupo: 'Contactos', descripcion: 'Crear y editar contactos' },
  { permiso: 'EXPEDIENTES_VER', grupo: 'Expedientes', descripcion: 'Ver expedientes' },
  { permiso: 'EXPEDIENTES_REVISAR', grupo: 'Expedientes', descripcion: 'Revisar expedientes antes de presentar' },
  { permiso: 'USUARIOS_GESTIONAR', grupo: 'Administración', descripcion: 'Gestionar usuarios' },
  { permiso: 'AUDITORIA_VER', grupo: 'Administración', descripcion: 'Ver el registro de auditoría' },
];

const todos = catalogoPermisos.map((p) => p.permiso);
export const matrizRoles: PermisosDeRol[] = [
  { rol: 'TITULAR', etiqueta: 'Titular / Administrador', descripcion: 'Acceso total', editable: false, permisos: todos },
  { rol: 'ABOGADO', etiqueta: 'Abogado / Revisor', descripcion: 'Revisa antes de presentar', editable: true,
    permisos: ['CONTACTOS_VER', 'CONTACTOS_EDITAR', 'EXPEDIENTES_VER', 'EXPEDIENTES_REVISAR'] },
  { rol: 'GESTOR', etiqueta: 'Gestor / Tramitador', descripcion: 'Tramita expedientes', editable: true,
    permisos: ['CONTACTOS_VER', 'CONTACTOS_EDITAR', 'EXPEDIENTES_VER'] },
  { rol: 'ADMINISTRATIVO', etiqueta: 'Administrativo / Recepción', descripcion: 'Atención y agenda', editable: true,
    permisos: ['CONTACTOS_VER'] },
  { rol: 'FACTURACION', etiqueta: 'Facturación', descripcion: 'Facturas y cobros', editable: true, permisos: [] },
  { rol: 'COLABORADOR', etiqueta: 'Colaborador externo', descripcion: 'Solo sus clientes', editable: true, permisos: [] },
];

export const catalogos: ResumenCatalogo[] = [
  { tipo: 'TIPO_PERSONA', nombre: 'Tipo de persona', grupo: 'Contactos', descripcion: 'Si el contacto es una persona o una empresa.', valores: 2, activos: 2 },
  { tipo: 'GENERO', nombre: 'Género', grupo: 'Contactos', descripcion: 'Opciones del campo Género en la ficha del contacto.', valores: 2, activos: 2 },
  { tipo: 'TIPO_EXPEDIENTE', nombre: 'Tipo de expediente', grupo: 'Expedientes', descripcion: 'Área a la que pertenece cada expediente.', valores: 3, activos: 3 },
];

export const valores: ValorVista[] = [
  { id: 'v1', codigo: 'FISICA', etiqueta: 'Física', orden: 1, activo: true, sistema: true },
  { id: 'v2', codigo: 'JURIDICA', etiqueta: 'Jurídica', orden: 2, activo: true, sistema: true },
  { id: 'v3', codigo: 'AUTONOMO', etiqueta: 'Autónomo', orden: 3, activo: false, sistema: false },
];

export const calendario: CalendarioAnual = {
  anio: 2026,
  comunidadAutonoma: 'COMUNITAT_VALENCIANA',
  nombreComunidad: 'Comunitat Valenciana',
  puedeEditarGlobales: true,
  dias: [
    { id: 'f1', fecha: '2026-01-01', ambito: 'NACIONAL', ambitoEtiqueta: 'Nacional', comunidad: undefined as unknown as CalendarioAnual['comunidadAutonoma'], descripcion: 'Año Nuevo', provisional: false, editable: true },
    { id: 'f2', fecha: '2026-03-19', ambito: 'AUTONOMICO', ambitoEtiqueta: 'Autonómico', comunidad: 'COMUNITAT_VALENCIANA', descripcion: 'San José', provisional: false, editable: true },
    { id: 'f3', fecha: '2026-04-16', ambito: 'LOCAL', ambitoEtiqueta: 'Local', comunidad: undefined as unknown as CalendarioAnual['comunidadAutonoma'], descripcion: 'Santa Faz', provisional: false, editable: true },
    { id: 'f4', fecha: '2026-06-23', ambito: 'LOCAL', ambitoEtiqueta: 'Local', comunidad: undefined as unknown as CalendarioAnual['comunidadAutonoma'], descripcion: 'Hogueras de San Juan', provisional: false, editable: true },
    { id: 'f5', fecha: '2026-10-09', ambito: 'AUTONOMICO', ambitoEtiqueta: 'Autonómico', comunidad: 'COMUNITAT_VALENCIANA', descripcion: 'Día de la Comunitat Valenciana', provisional: false, editable: true },
    { id: 'f6', fecha: '2026-12-25', ambito: 'NACIONAL', ambitoEtiqueta: 'Nacional', comunidad: undefined as unknown as CalendarioAnual['comunidadAutonoma'], descripcion: 'Natividad del Señor', provisional: false, editable: true },
  ],
};

export const auditoria: PaginaAuditoria = {
  pagina: 0,
  tamano: 25,
  total: 4,
  entradas: [
    { ocurridoEn: '2026-10-16T07:45:00Z', actor: 'Ana Martínez Soler', accion: 'SESION_INICIADA', descripcion: 'Inicio de sesión', entidadTipo: 'USUARIO', entidadId: 'u-ana' },
    { ocurridoEn: '2026-10-15T16:21:00Z', actor: 'Ana Martínez Soler', accion: 'USUARIO_INVITADO', descripcion: 'Invitado javier.lopez@despacho-demo.es con el rol Administrativo / Recepción', entidadTipo: 'USUARIO', entidadId: 'u-javier' },
    { ocurridoEn: '2026-10-14T11:02:00Z', actor: 'Ana Martínez Soler', accion: 'CALENDARIO_MODIFICADO', descripcion: 'Añadido festivo local 23/06/2026 (Hogueras de San Juan)', entidadTipo: 'DIA_INHABIL', entidadId: 'f4' },
    { ocurridoEn: '2026-10-01T08:00:00Z', actor: 'Sistema', accion: 'DESPACHO_CREADO', descripcion: 'Despacho creado: Extranjería Martínez & Asociados', entidadTipo: 'DESPACHO', entidadId: 'd-demo' },
  ],
};
