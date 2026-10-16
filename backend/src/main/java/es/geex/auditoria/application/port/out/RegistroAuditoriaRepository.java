package es.geex.auditoria.application.port.out;

import es.geex.auditoria.domain.RegistroAuditoria;
import es.geex.compartido.domain.DespachoId;
import java.time.Instant;
import java.util.List;

public interface RegistroAuditoriaRepository {

    void guardar(RegistroAuditoria registro);

    List<RegistroAuditoria> buscar(DespachoId despacho, String accion, Instant desde, Instant hasta, int desplazamiento,
                                   int limite);

    long contar(DespachoId despacho, String accion, Instant desde, Instant hasta);
}
