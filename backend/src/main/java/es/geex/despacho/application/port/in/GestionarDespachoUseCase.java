package es.geex.despacho.application.port.in;

import es.geex.compartido.domain.ComunidadAutonoma;
import java.time.Instant;

/** Consulta y modificación de los datos generales del despacho del usuario actual. */
public interface GestionarDespachoUseCase {

    DespachoVista obtenerActual();

    DespachoVista actualizar(DatosDespachoComando datos);

    record DatosDespachoComando(
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
    }

    record DespachoVista(
            String id,
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
            String web,
            Instant creadoEn,
            Instant actualizadoEn) {
    }
}
