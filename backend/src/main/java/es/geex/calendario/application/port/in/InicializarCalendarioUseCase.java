package es.geex.calendario.application.port.in;

import es.geex.compartido.domain.DespachoId;

/** Copia al despacho nuevo los festivos locales conocidos de su municipio. */
public interface InicializarCalendarioUseCase {

    void inicializar(DespachoId despacho, String municipio, String provincia);
}
