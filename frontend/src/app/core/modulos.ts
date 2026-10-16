/**
 * Los 24 apartados de la pantalla de inicio, en el orden del documento de
 * requisitos (pág. 3), con la fase del plan en la que se construye cada uno.
 */
export interface ModuloGeex {
  clave: string;
  numero: number;
  nombre: string;
  descripcion: string;
  /** Nombre del icono de Material Symbols. */
  icono: string;
  fase: number;
  /** Ruta si el módulo ya está disponible. */
  ruta?: string;
}

export const FASE_ACTUAL = 0;

export const MODULOS: ModuloGeex[] = [
  { clave: 'contactos', numero: 1, nombre: 'Contactos', icono: 'groups', fase: 1,
    descripcion: 'Ficha completa del cliente, contactos vinculados, residencia, nacionalidad y LOPD.' },
  { clave: 'expedientes', numero: 2, nombre: 'Expedientes', icono: 'folder_open', fase: 2,
    descripcion: 'Tramitación de cada expediente: estado, documentos, tareas, seguimiento y resolución.' },
  { clave: 'documentos', numero: 3, nombre: 'Documentos', icono: 'description', fase: 2,
    descripcion: 'Todos los documentos subidos, por orden cronológico, con su fecha de caducidad.' },
  { clave: 'tareas', numero: 4, nombre: 'Tareas', icono: 'task_alt', fase: 3,
    descripcion: 'Tareas entre usuarios del despacho, con prioridad, vencimiento y reglas automáticas.' },
  { clave: 'seguimientos', numero: 5, nombre: 'Seguimientos', icono: 'history', fase: 2,
    descripcion: 'Actuaciones realizadas en cada expediente, desde la apertura hasta la resolución.' },
  { clave: 'requerimientos', numero: 6, nombre: 'Requerimientos', icono: 'report', fase: 2,
    descripcion: 'Requerimientos de Extranjería con cómputo automático de días hábiles o naturales.' },
  { clave: 'recursos', numero: 7, nombre: 'Recursos', icono: 'gavel', fase: 2,
    descripcion: 'Recursos de reposición, alzada, contenciosos y alegaciones, con sus plazos.' },
  { clave: 'renovaciones', numero: 8, nombre: 'Renovaciones', icono: 'autorenew', fase: 1,
    descripcion: 'Avisos de renovación 90 o 60 días antes de la caducidad de la tarjeta, según el régimen.' },
  { clave: 'nacionalidades', numero: 9, nombre: 'Nacionalidades', icono: 'badge', fase: 1,
    descripcion: 'Fecha en la que cada cliente puede pedir la nacionalidad española (10, 5, 2 o 1 año).' },
  { clave: 'agenda', numero: 10, nombre: 'Agenda', icono: 'calendar_month', fase: 3,
    descripcion: 'Agenda común del despacho por día, semana y mes; citas online de los clientes.' },
  { clave: 'facturacion', numero: 11, nombre: 'Facturación', icono: 'euro', fase: 5,
    descripcion: 'Honorarios, provisiones de fondos, suplidos, gastos y notas de abono; cobros y exportación.' },
  { clave: 'checklist', numero: 12, nombre: 'Check list trámites', icono: 'checklist', fase: 2,
    descripcion: 'Documentos y requisitos de cada trámite; sin los obligatorios no se puede presentar.' },
  { clave: 'colaboradores', numero: 13, nombre: 'Colaboradores', icono: 'handshake', fase: 5,
    descripcion: 'Procedencia de los clientes, expedientes aportados y liquidación de comisiones.' },
  { clave: 'plantillas', numero: 14, nombre: 'Plantillas / escritos', icono: 'edit_document', fase: 6,
    descripcion: 'Hojas de encargo, mandatos, LOPD y campañas, rellenadas con los datos del cliente.' },
  { clave: 'honorarios', numero: 15, nombre: 'Honorarios', icono: 'sell', fase: 5,
    descripcion: 'Precios predefinidos de trámites y servicios, con descuentos al facturar.' },
  { clave: 'informes', numero: 16, nombre: 'Informes', icono: 'bar_chart', fase: 5,
    descripcion: 'Expedientes presentados, favorables, denegados y recurridos; facturación y clientes.' },
  { clave: 'configuracion', numero: 17, nombre: 'Configuración', icono: 'settings', fase: 0, ruta: '/configuracion',
    descripcion: 'Datos del despacho, usuarios, permisos, listas desplegables y calendario laboral.' },
  { clave: 'enlaces', numero: 18, nombre: 'Enlaces', icono: 'link', fase: 3,
    descripcion: 'Accesos directos a las webs de Extranjería, Seguridad Social, Carpeta Ciudadana...' },
  { clave: 'avisos', numero: 19, nombre: 'Avisos', icono: 'notifications', fase: 6,
    descripcion: 'Avisos estandarizados a los clientes por SMS o por la app, con constancia de lectura.' },
  { clave: 'mail', numero: 20, nombre: 'Mail', icono: 'mail', fase: 6,
    descripcion: 'Correos desde el programa, en español y en el idioma del cliente, registrados en su ficha.' },
  { clave: 'chat', numero: 21, nombre: 'Chat / WhatsApp', icono: 'chat', fase: 7,
    descripcion: 'Chat privado con el cliente desde la app; cada conversación queda registrada.' },
  { clave: 'plazos', numero: 22, nombre: 'Plazos', icono: 'schedule', fase: 1,
    descripcion: 'Todas las fechas críticas: tarjetas, documentos, requerimientos, recursos y renovaciones.' },
  { clave: 'area-cliente', numero: 23, nombre: 'Área cliente', icono: 'public', fase: 7,
    descripcion: 'Portal y app del cliente: sus datos, expedientes, documentos compartidos y pagos.' },
  { clave: 'firma', numero: 24, nombre: '@Firma', icono: 'signature', fase: 6,
    descripcion: 'Firma electrónica en remoto de mandatos, hojas de encargo y LOPD. Objetivo: cero papel.' },
];

export function moduloPorClave(clave: string): ModuloGeex | undefined {
  return MODULOS.find((m) => m.clave === clave);
}
