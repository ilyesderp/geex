package es.geex.identidad.adapter.out.token;

/** Nombres de los claims propios del token de acceso. */
public final class ClaimsGeex {

    /** Audiencia de los tokens del personal del despacho (el portal del cliente usará otra). */
    public static final String AUDIENCIA_PERSONAL = "geex-personal";

    public static final String DESPACHO = "did";
    public static final String ROL = "rol";
    public static final String SUPERADMIN = "sa";
    public static final String VERSION_TOKEN = "ver";

    private ClaimsGeex() {
    }
}
