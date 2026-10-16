package es.geex.identidad.application.port.in;

import es.geex.compartido.domain.DespachoId;

/** Prepara lo que Identidad necesita en un despacho recién creado (matriz de permisos predeterminada). */
public interface InicializarDespachoUseCase {

    void inicializar(DespachoId despacho);
}
