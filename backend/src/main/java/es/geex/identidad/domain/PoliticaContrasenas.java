package es.geex.identidad.domain;

import es.geex.compartido.domain.ReglaNegocioException;

/**
 * Requisitos de contraseña. Se sigue la recomendación actual (NIST SP 800-63B):
 * longitud mínima alta en lugar de reglas de composición que llevan a contraseñas
 * predecibles.
 */
public final class PoliticaContrasenas {

    public static final int LONGITUD_MINIMA = 12;
    public static final int LONGITUD_MAXIMA = 128;

    private PoliticaContrasenas() {
    }

    public static void validar(String contrasena) {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA) {
            throw new ReglaNegocioException("CONTRASENA_DEBIL",
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres");
        }
        if (contrasena.length() > LONGITUD_MAXIMA) {
            throw new ReglaNegocioException("CONTRASENA_LARGA",
                    "La contraseña no puede superar los " + LONGITUD_MAXIMA + " caracteres");
        }
        if (contrasena.chars().distinct().count() < 4) {
            throw new ReglaNegocioException("CONTRASENA_DEBIL",
                    "La contraseña es demasiado simple: usa más caracteres distintos");
        }
    }
}
