package es.geex.configuracion.application.port.in;

import es.geex.compartido.domain.DespachoId;

/** Copia las listas desplegables iniciales a un despacho recién creado. */
public interface InicializarCatalogosUseCase {

    void inicializar(DespachoId despacho);
}
