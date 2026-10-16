package es.geex.despacho.domain;

import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.ReglaNegocioException;
import java.time.Instant;
import java.util.Objects;

/** Agregado Despacho: datos generales de la gestoría. */
public class Despacho {

    private final DespachoId id;
    private DatosDespacho datos;
    private final Instant creadoEn;
    private Instant actualizadoEn;
    private final long version;

    private Despacho(DespachoId id, DatosDespacho datos, Instant creadoEn, Instant actualizadoEn, long version) {
        this.id = Objects.requireNonNull(id);
        this.datos = Objects.requireNonNull(datos);
        this.creadoEn = Objects.requireNonNull(creadoEn);
        this.actualizadoEn = Objects.requireNonNull(actualizadoEn);
        this.version = version;
    }

    public static Despacho crear(DatosDespacho datos, Instant ahora) {
        validar(datos);
        return new Despacho(DespachoId.nuevo(), datos, ahora, ahora, 0);
    }

    public static Despacho reconstruir(DespachoId id, DatosDespacho datos, Instant creadoEn, Instant actualizadoEn, long version) {
        return new Despacho(id, datos, creadoEn, actualizadoEn, version);
    }

    public void actualizar(DatosDespacho nuevos, Instant ahora) {
        validar(nuevos);
        this.datos = nuevos;
        this.actualizadoEn = ahora;
    }

    private static void validar(DatosDespacho datos) {
        if (datos.nombre() == null || datos.nombre().isBlank()) {
            throw new ReglaNegocioException("DESPACHO_SIN_NOMBRE", "El nombre del despacho es obligatorio");
        }
        if (datos.municipio() == null || datos.municipio().isBlank()
                || datos.provincia() == null || datos.provincia().isBlank()) {
            throw new ReglaNegocioException("DESPACHO_SIN_UBICACION",
                    "El municipio y la provincia son obligatorios: determinan el calendario de días inhábiles");
        }
        if (datos.comunidadAutonoma() == null) {
            throw new ReglaNegocioException("DESPACHO_SIN_COMUNIDAD", "La comunidad autónoma es obligatoria");
        }
    }

    public DespachoId id() {
        return id;
    }

    public DatosDespacho datos() {
        return datos;
    }

    public ComunidadAutonoma comunidadAutonoma() {
        return datos.comunidadAutonoma();
    }

    public Instant creadoEn() {
        return creadoEn;
    }

    public Instant actualizadoEn() {
        return actualizadoEn;
    }

    public long version() {
        return version;
    }
}
