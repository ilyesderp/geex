package es.geex.identidad.adapter.in.web;

import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import es.geex.identidad.application.port.in.PerfilUseCase;
import es.geex.identidad.application.port.in.PerfilUseCase.Perfil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/yo")
@Tag(name = "Perfil", description = "Usuario de la sesión actual")
class PerfilController {

    private final PerfilUseCase perfil;

    PerfilController(PerfilUseCase perfil) {
        this.perfil = perfil;
    }

    /** En modo sistema porque lista los despachos del usuario, que pertenecen a varios tenants. */
    @GetMapping
    @Operation(operationId = "obtenerPerfil", summary = "Datos, permisos y despachos del usuario actual")
    Perfil obtener() {
        return ContextoEjecucion.comoSistema(perfil::obtenerPerfil);
    }
}
