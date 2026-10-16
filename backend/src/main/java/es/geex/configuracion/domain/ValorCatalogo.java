package es.geex.configuracion.domain;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.ReglaNegocioException;
import java.util.Objects;
import java.util.UUID;

/** Un valor de una lista desplegable de un despacho. */
public class ValorCatalogo {

    public static final int LONGITUD_MAXIMA_ETIQUETA = 200;

    private final UUID id;
    private final DespachoId despachoId;
    private final TipoCatalogo tipo;
    private final String codigo;
    private String etiqueta;
    private int orden;
    private boolean activo;
    private final boolean sistema;
    private final long version;

    private ValorCatalogo(UUID id, DespachoId despachoId, TipoCatalogo tipo, String codigo, String etiqueta, int orden,
                          boolean activo, boolean sistema, long version) {
        this.id = Objects.requireNonNull(id);
        this.despachoId = Objects.requireNonNull(despachoId);
        this.tipo = Objects.requireNonNull(tipo);
        this.codigo = Objects.requireNonNull(codigo);
        this.etiqueta = validarEtiqueta(etiqueta);
        this.orden = orden;
        this.activo = activo;
        this.sistema = sistema;
        this.version = version;
    }

    /** Valor inicial predefinido (no se puede borrar). */
    public static ValorCatalogo inicial(DespachoId despacho, TipoCatalogo tipo, String codigo, String etiqueta, int orden) {
        return new ValorCatalogo(UUID.randomUUID(), despacho, tipo, codigo, etiqueta, orden, true, true, 0);
    }

    /** Valor añadido por el despacho. */
    public static ValorCatalogo personalizado(DespachoId despacho, TipoCatalogo tipo, String etiqueta, int orden) {
        return new ValorCatalogo(UUID.randomUUID(), despacho, tipo, CodigoCatalogo.desdeEtiqueta(etiqueta),
                etiqueta, orden, true, false, 0);
    }

    public static ValorCatalogo reconstruir(UUID id, DespachoId despacho, TipoCatalogo tipo, String codigo,
                                            String etiqueta, int orden, boolean activo, boolean sistema, long version) {
        return new ValorCatalogo(id, despacho, tipo, codigo, etiqueta, orden, activo, sistema, version);
    }

    public void renombrar(String nuevaEtiqueta) {
        this.etiqueta = validarEtiqueta(nuevaEtiqueta);
    }

    public void cambiarActivo(boolean activo) {
        this.activo = activo;
    }

    public void moverA(int posicion) {
        this.orden = posicion;
    }

    public void comprobarQueSePuedeEliminar() {
        if (sistema) {
            throw new ReglaNegocioException("VALOR_DEL_SISTEMA",
                    "Este valor es predefinido y otras partes del programa lo usan: puedes renombrarlo o desactivarlo, pero no borrarlo");
        }
    }

    private static String validarEtiqueta(String etiqueta) {
        if (etiqueta == null || etiqueta.isBlank()) {
            throw new ReglaNegocioException("ETIQUETA_OBLIGATORIA", "La etiqueta es obligatoria");
        }
        String limpia = etiqueta.strip();
        if (limpia.length() > LONGITUD_MAXIMA_ETIQUETA) {
            throw new ReglaNegocioException("ETIQUETA_LARGA",
                    "La etiqueta no puede superar los " + LONGITUD_MAXIMA_ETIQUETA + " caracteres");
        }
        return limpia;
    }

    public UUID id() { return id; }
    public DespachoId despachoId() { return despachoId; }
    public TipoCatalogo tipo() { return tipo; }
    public String codigo() { return codigo; }
    public String etiqueta() { return etiqueta; }
    public int orden() { return orden; }
    public boolean activo() { return activo; }
    public boolean sistema() { return sistema; }
    public long version() { return version; }
}
