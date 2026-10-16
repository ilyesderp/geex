package es.geex.compartido.domain;

import static es.geex.compartido.domain.Permiso.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Roles del personal del despacho. Los permisos indicados aquí son los
 * predeterminados al crear un despacho; después cada despacho puede ajustarlos,
 * salvo los del titular, que siempre tiene todos (evita quedarse sin acceso).
 */
public enum Rol {
    TITULAR("Titular / Administrador",
            "Acceso total: configuración, usuarios, facturación e informes.",
            EnumSet.allOf(Permiso.class)),

    ABOGADO("Abogado / Revisor",
            "Revisa los expedientes antes de su presentación y tramita.",
            EnumSet.of(CONTACTOS_VER, CONTACTOS_EDITAR, EXPEDIENTES_VER, EXPEDIENTES_EDITAR,
                    EXPEDIENTES_REVISAR, EXPEDIENTES_CERRAR, DOCUMENTOS_VER, DOCUMENTOS_EDITAR,
                    DOCUMENTOS_BORRAR, TAREAS_VER_TODAS, TAREAS_ASIGNAR, PLAZOS_VER, AGENDA_VER,
                    AGENDA_EDITAR, FACTURACION_VER, COLABORADORES_VER, COMUNICACIONES_ENVIAR,
                    DIFUSION_ENVIAR, PLANTILLAS_GESTIONAR, INFORMES_VER)),

    GESTOR("Gestor / Tramitador",
            "Gestiona contactos, expedientes, documentos, tareas y plazos.",
            EnumSet.of(CONTACTOS_VER, CONTACTOS_EDITAR, EXPEDIENTES_VER, EXPEDIENTES_EDITAR,
                    EXPEDIENTES_CERRAR, DOCUMENTOS_VER, DOCUMENTOS_EDITAR, TAREAS_ASIGNAR,
                    PLAZOS_VER, AGENDA_VER, AGENDA_EDITAR, FACTURACION_VER, COLABORADORES_VER,
                    COMUNICACIONES_ENVIAR)),

    ADMINISTRATIVO("Administrativo / Recepción",
            "Atiende a los clientes: contactos, agenda, documentos y notas.",
            EnumSet.of(CONTACTOS_VER, CONTACTOS_EDITAR, EXPEDIENTES_VER, DOCUMENTOS_VER,
                    DOCUMENTOS_EDITAR, PLAZOS_VER, AGENDA_VER, AGENDA_EDITAR, COMUNICACIONES_ENVIAR)),

    FACTURACION("Facturación",
            "Facturas, provisiones de fondos, suplidos, cobros y comisiones.",
            EnumSet.of(CONTACTOS_VER, EXPEDIENTES_VER, FACTURACION_VER, FACTURACION_EMITIR,
                    FACTURACION_RECTIFICAR, HONORARIOS_GESTIONAR, COLABORADORES_VER,
                    COLABORADORES_GESTIONAR, INFORMES_VER)),

    COLABORADOR("Colaborador externo",
            "Solo ve los clientes y expedientes que ha aportado al despacho.",
            EnumSet.of(CONTACTOS_VER, EXPEDIENTES_VER, DOCUMENTOS_VER));

    private final String etiqueta;
    private final String descripcion;
    private final Set<Permiso> permisosPredeterminados;

    Rol(String etiqueta, String descripcion, Set<Permiso> permisosPredeterminados) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
        this.permisosPredeterminados = Collections.unmodifiableSet(EnumSet.copyOf(permisosPredeterminados));
    }

    public String etiqueta() {
        return etiqueta;
    }

    public String descripcion() {
        return descripcion;
    }

    public Set<Permiso> permisosPredeterminados() {
        return permisosPredeterminados;
    }

    /** El titular siempre conserva todos los permisos. */
    public boolean permisosEditables() {
        return this != TITULAR;
    }
}
