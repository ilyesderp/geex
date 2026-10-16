package es.geex.compartido.domain;

/**
 * Permisos de la aplicación. Cada rol tiene un conjunto de permisos configurable
 * por despacho (apartado CONFIGURACIÓN: "permisos de cada usuario a la hora de
 * utilizar el programa").
 */
public enum Permiso {
    // Contactos (clientes)
    CONTACTOS_VER("Contactos", "Ver contactos"),
    CONTACTOS_EDITAR("Contactos", "Crear y editar contactos"),
    CONTACTOS_BORRAR("Contactos", "Borrar contactos"),

    // Expedientes
    EXPEDIENTES_VER("Expedientes", "Ver expedientes"),
    EXPEDIENTES_EDITAR("Expedientes", "Crear y tramitar expedientes"),
    EXPEDIENTES_REVISAR("Expedientes", "Revisar expedientes antes de presentar"),
    EXPEDIENTES_CERRAR("Expedientes", "Cerrar expedientes"),
    EXPEDIENTES_BORRAR("Expedientes", "Borrar expedientes"),

    // Documentos
    DOCUMENTOS_VER("Documentos", "Ver documentos"),
    DOCUMENTOS_EDITAR("Documentos", "Subir y clasificar documentos"),
    DOCUMENTOS_BORRAR("Documentos", "Borrar documentos"),

    // Tareas, plazos y agenda
    TAREAS_VER_TODAS("Tareas", "Ver las tareas de todos los usuarios"),
    TAREAS_ASIGNAR("Tareas", "Asignar tareas a otros usuarios"),
    PLAZOS_VER("Plazos", "Ver plazos, renovaciones y nacionalidades"),
    AGENDA_VER("Agenda", "Ver la agenda"),
    AGENDA_EDITAR("Agenda", "Crear y modificar citas"),

    // Facturación
    FACTURACION_VER("Facturación", "Ver facturación y cobros"),
    FACTURACION_EMITIR("Facturación", "Emitir facturas, provisiones y suplidos"),
    FACTURACION_RECTIFICAR("Facturación", "Emitir facturas rectificativas"),
    HONORARIOS_GESTIONAR("Facturación", "Gestionar la tabla de honorarios"),

    // Colaboradores
    COLABORADORES_VER("Colaboradores", "Ver colaboradores"),
    COLABORADORES_GESTIONAR("Colaboradores", "Gestionar colaboradores y comisiones"),

    // Comunicaciones
    COMUNICACIONES_ENVIAR("Comunicaciones", "Enviar correos, avisos y mensajes"),
    DIFUSION_ENVIAR("Comunicaciones", "Enviar comunicaciones masivas"),
    PLANTILLAS_GESTIONAR("Comunicaciones", "Gestionar plantillas y escritos"),

    // Gestión del despacho
    INFORMES_VER("Despacho", "Ver informes"),
    IMPORTACION_EJECUTAR("Despacho", "Importar datos"),
    JORNADA_GESTIONAR("Despacho", "Ver y corregir el registro de jornada de todos"),
    USUARIOS_GESTIONAR("Administración", "Gestionar usuarios"),
    PERMISOS_GESTIONAR("Administración", "Gestionar roles y permisos"),
    CONFIGURACION_GESTIONAR("Administración", "Modificar la configuración del despacho"),
    AUDITORIA_VER("Administración", "Ver el registro de auditoría");

    private final String grupo;
    private final String descripcion;

    Permiso(String grupo, String descripcion) {
        this.grupo = grupo;
        this.descripcion = descripcion;
    }

    public String grupo() {
        return grupo;
    }

    public String descripcion() {
        return descripcion;
    }
}
