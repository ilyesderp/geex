package es.geex.auditoria.adapter.in.web;

import es.geex.auditoria.application.port.in.AuditoriaUseCase;
import es.geex.auditoria.application.port.in.AuditoriaUseCase.FiltroAuditoria;
import es.geex.auditoria.application.port.in.AuditoriaUseCase.PaginaAuditoria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
@Tag(name = "Auditoria", description = "Auditoría: registro de actividad del despacho")
class AuditoriaController {

    private final AuditoriaUseCase auditoria;

    AuditoriaController(AuditoriaUseCase auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping
    @Operation(operationId = "consultarAuditoria", summary = "Registro de actividad, del más reciente al más antiguo")
    PaginaAuditoria consultar(
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamano) {
        return auditoria.consultar(new FiltroAuditoria(accion, desde, hasta, pagina, tamano));
    }
}
