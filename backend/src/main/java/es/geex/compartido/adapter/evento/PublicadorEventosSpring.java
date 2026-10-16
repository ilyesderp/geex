package es.geex.compartido.adapter.evento;

import es.geex.compartido.application.PublicadorEventos;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class PublicadorEventosSpring implements PublicadorEventos {

    private final ApplicationEventPublisher publicador;

    PublicadorEventosSpring(ApplicationEventPublisher publicador) {
        this.publicador = publicador;
    }

    @Override
    public void publicar(Object evento) {
        publicador.publishEvent(evento);
    }
}
