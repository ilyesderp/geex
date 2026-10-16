package es.geex.identidad.adapter.in.evento;

import es.geex.despacho.domain.evento.DespachoCreado;
import es.geex.identidad.application.port.in.InicializarDespachoUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Al crearse un despacho, prepara su matriz de permisos predeterminada.
 * Es síncrono a propósito: forma parte de la misma transacción que la creación,
 * así un despacho nunca existe sin permisos.
 */
@Component
class InicializacionDespachoListener {

    private final InicializarDespachoUseCase inicializar;

    InicializacionDespachoListener(InicializarDespachoUseCase inicializar) {
        this.inicializar = inicializar;
    }

    @EventListener
    void alCrearDespacho(DespachoCreado evento) {
        inicializar.inicializar(evento.despachoId());
    }
}
