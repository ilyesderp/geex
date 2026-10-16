package es.geex.auditoria.adapter.in.evento;

import es.geex.auditoria.application.port.in.AuditoriaUseCase;
import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import es.geex.compartido.domain.EventoAuditable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Registra en la auditoría cualquier evento auditable, de forma asíncrona y solo
 * si la transacción que lo publicó se confirmó.
 *
 * <p>Spring Modulith guarda cada publicación en la tabla {@code event_publication}
 * dentro de la transacción original: si la aplicación se detiene antes de
 * procesarla, se reintenta al arrancar. Ningún registro de auditoría se pierde.
 */
@Component
class AuditoriaListener {

    private final AuditoriaUseCase auditoria;

    AuditoriaListener(AuditoriaUseCase auditoria) {
        this.auditoria = auditoria;
    }

    @Async
    @TransactionalEventListener
    void registrar(EventoAuditable evento) {
        ContextoEjecucion.comoSistema(() -> auditoria.registrar(evento));
    }
}
