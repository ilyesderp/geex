package es.geex.configuracion.adapter.in.evento;

import es.geex.configuracion.application.port.in.InicializarCatalogosUseCase;
import es.geex.despacho.domain.evento.DespachoCreado;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Síncrono: un despacho nuevo nace con sus listas desplegables, en la misma transacción. */
@Component
class InicializacionCatalogosListener {

    private final InicializarCatalogosUseCase inicializar;

    InicializacionCatalogosListener(InicializarCatalogosUseCase inicializar) {
        this.inicializar = inicializar;
    }

    @EventListener
    void alCrearDespacho(DespachoCreado evento) {
        inicializar.inicializar(evento.despachoId());
    }
}
