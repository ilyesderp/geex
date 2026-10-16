package es.geex.compartido.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * Comunidades y ciudades autónomas. Determinan los días inhábiles autonómicos
 * que se aplican al cómputo de plazos administrativos (art. 30.7 Ley 39/2015).
 */
public enum ComunidadAutonoma {
    ANDALUCIA("andalucia", "Andalucía"),
    ARAGON("aragon", "Aragón"),
    ASTURIAS("asturias", "Principado de Asturias"),
    BALEARES("baleares", "Illes Balears"),
    CANARIAS("canarias", "Canarias"),
    CANTABRIA("cantabria", "Cantabria"),
    CASTILLA_LA_MANCHA("castilla-la-mancha", "Castilla-La Mancha"),
    CASTILLA_Y_LEON("castilla-y-leon", "Castilla y León"),
    CATALUNA("cataluna", "Cataluña"),
    COMUNITAT_VALENCIANA("valencia", "Comunitat Valenciana"),
    EXTREMADURA("extremadura", "Extremadura"),
    GALICIA("galicia", "Galicia"),
    MADRID("madrid", "Comunidad de Madrid"),
    MURCIA("murcia", "Región de Murcia"),
    NAVARRA("navarra", "Comunidad Foral de Navarra"),
    PAIS_VASCO("pais-vasco", "País Vasco"),
    LA_RIOJA("rioja", "La Rioja"),
    CEUTA("ceuta", "Ceuta"),
    MELILLA("melilla", "Melilla");

    private final String clave;
    private final String nombre;

    ComunidadAutonoma(String clave, String nombre) {
        this.clave = clave;
        this.nombre = nombre;
    }

    /** Clave corta usada en los datos abiertos del calendario de días inhábiles. */
    public String clave() {
        return clave;
    }

    public String nombre() {
        return nombre;
    }

    public static Optional<ComunidadAutonoma> porClave(String clave) {
        return Arrays.stream(values()).filter(c -> c.clave.equals(clave)).findFirst();
    }
}
