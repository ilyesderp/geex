package es.geex.identidad.application.port.in;

import es.geex.compartido.domain.ComunidadAutonoma;

/** Primera puesta en marcha: crea el primer despacho y su administrador. */
public interface InstalacionUseCase {

    boolean consultarSiInstalado();

    void instalar(DatosInstalacion datos);

    record DatosInstalacion(
            String nombreDespacho,
            String municipio,
            String provincia,
            ComunidadAutonoma comunidadAutonoma,
            String nombre,
            String apellidos,
            String email,
            String contrasena) {
    }
}
