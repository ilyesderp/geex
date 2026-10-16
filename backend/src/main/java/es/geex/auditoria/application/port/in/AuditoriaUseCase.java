package es.geex.auditoria.application.port.in;

import es.geex.compartido.domain.EventoAuditable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface AuditoriaUseCase {

    void registrar(EventoAuditable evento);

    PaginaAuditoria consultar(FiltroAuditoria filtro);

    record FiltroAuditoria(String accion, LocalDate desde, LocalDate hasta, int pagina, int tamano) {
    }

    record EntradaAuditoria(Instant ocurridoEn, String actor, String accion, String descripcion, String entidadTipo,
                            String entidadId) {
    }

    record PaginaAuditoria(List<EntradaAuditoria> entradas, long total, int pagina, int tamano) {
    }
}
