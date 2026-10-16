package es.geex.calendario.adapter.in.evento;

import es.geex.calendario.application.port.in.InicializarCalendarioUseCase;
import es.geex.despacho.domain.evento.DespachoCreado;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Síncrono: el despacho nuevo recibe los festivos locales conocidos de su municipio. */
@Component
class InicializacionCalendarioListener {

    private final InicializarCalendarioUseCase inicializar;

    InicializacionCalendarioListener(InicializarCalendarioUseCase inicializar) {
        this.inicializar = inicializar;
    }

    @EventListener
    void alCrearDespacho(DespachoCreado evento) {
        inicializar.inicializar(evento.despachoId(), evento.municipio(), evento.provincia());
    }
}
