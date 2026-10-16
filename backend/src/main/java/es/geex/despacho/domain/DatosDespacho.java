package es.geex.despacho.domain;

import es.geex.compartido.domain.ComunidadAutonoma;

/** Datos generales del despacho (valor inmutable). */
public record DatosDespacho(
        String nombre,
        String razonSocial,
        String nif,
        String direccion,
        String codigoPostal,
        String municipio,
        String provincia,
        ComunidadAutonoma comunidadAutonoma,
        String telefono,
        String email,
        String web) {

    public DatosDespacho {
        nombre = limpiar(nombre);
        razonSocial = limpiar(razonSocial);
        nif = limpiar(nif) == null ? null : limpiar(nif).toUpperCase();
        direccion = limpiar(direccion);
        codigoPostal = limpiar(codigoPostal);
        municipio = limpiar(municipio);
        provincia = limpiar(provincia);
        telefono = limpiar(telefono);
        email = limpiar(email) == null ? null : limpiar(email).toLowerCase();
        web = limpiar(web);
    }

    /** Datos mínimos para crear un despacho desde la instalación. */
    public static DatosDespacho minimos(String nombre, String municipio, String provincia, ComunidadAutonoma comunidad) {
        return new DatosDespacho(nombre, null, null, null, null, municipio, provincia, comunidad, null, null, null);
    }

    private static String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String recortado = valor.strip();
        return recortado.isEmpty() ? null : recortado;
    }
}
