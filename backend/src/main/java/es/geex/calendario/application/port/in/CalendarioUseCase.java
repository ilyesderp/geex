package es.geex.calendario.application.port.in;

import es.geex.calendario.domain.AmbitoDia;
import es.geex.compartido.domain.ComunidadAutonoma;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Calendario laboral del despacho (Configuración → Calendario laboral). */
public interface CalendarioUseCase {

    CalendarioAnual obtenerCalendario(int anio);

    DiaInhabilVista anadirFestivoLocal(LocalDate fecha, String descripcion);

    void eliminarFestivoLocal(UUID id);

    /** Solo administradores de la plataforma: festivos nacionales o autonómicos (p. ej. al publicarse el BOE de cada año). */
    DiaInhabilVista anadirFestivoGlobal(AmbitoDia ambito, ComunidadAutonoma comunidad, LocalDate fecha,
                                        String descripcion, boolean provisional);

    void eliminarFestivoGlobal(UUID id);

    record CalendarioAnual(int anio, ComunidadAutonoma comunidadAutonoma, String nombreComunidad,
                           List<DiaInhabilVista> dias, boolean puedeEditarGlobales) {
    }

    record DiaInhabilVista(UUID id, LocalDate fecha, AmbitoDia ambito, String ambitoEtiqueta,
                           ComunidadAutonoma comunidad, String descripcion, boolean provisional, boolean editable) {
    }
}
