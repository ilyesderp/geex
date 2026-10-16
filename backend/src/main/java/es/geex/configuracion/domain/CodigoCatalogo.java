package es.geex.configuracion.domain;

import es.geex.compartido.domain.ReglaNegocioException;
import java.text.Normalizer;

/** Genera códigos estables a partir de etiquetas: "Régimen General" → "REGIMEN_GENERAL". */
public final class CodigoCatalogo {

    public static final int LONGITUD_MAXIMA = 60;

    private CodigoCatalogo() {
    }

    public static String desdeEtiqueta(String etiqueta) {
        String sinTildes = Normalizer.normalize(etiqueta == null ? "" : etiqueta, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String codigo = sinTildes.toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (codigo.isEmpty()) {
            throw new ReglaNegocioException("ETIQUETA_NO_VALIDA", "La etiqueta debe contener letras o números");
        }
        return codigo.length() > LONGITUD_MAXIMA ? codigo.substring(0, LONGITUD_MAXIMA) : codigo;
    }
}
