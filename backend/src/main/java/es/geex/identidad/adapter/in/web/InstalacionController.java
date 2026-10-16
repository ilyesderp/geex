package es.geex.identidad.adapter.in.web;

import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.identidad.application.port.in.InstalacionUseCase;
import es.geex.identidad.application.port.in.InstalacionUseCase.DatosInstalacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instalacion")
@Tag(name = "Instalacion", description = "Instalación: primera puesta en marcha de GEEX")
class InstalacionController {

    private final InstalacionUseCase instalacion;
    private final LimitadorPeticiones limitador;

    InstalacionController(InstalacionUseCase instalacion, LimitadorPeticiones limitador) {
        this.instalacion = instalacion;
        this.limitador = limitador;
    }

    @GetMapping
    @Operation(summary = "Indica si GEEX ya está instalado")
    EstadoInstalacion estado() {
        return new EstadoInstalacion(ContextoEjecucion.comoSistema(instalacion::consultarSiInstalado));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear el primer despacho y su administrador")
    void instalar(@Valid @RequestBody InstalacionPeticion p, HttpServletRequest peticion) {
        if (!limitador.permitir(LimitadorPeticiones.Limite.INSTALACION, peticion.getRemoteAddr())) {
            throw new DemasiadasPeticionesException();
        }
        ContextoEjecucion.comoSistema(() -> instalacion.instalar(new DatosInstalacion(p.nombreDespacho(),
                p.municipio(), p.provincia(), p.comunidadAutonoma(), p.nombre(), p.apellidos(), p.email(),
                p.contrasena())));
    }

    record EstadoInstalacion(boolean instalado) {
    }

    record InstalacionPeticion(
            @NotBlank @Size(max = 150) String nombreDespacho,
            @NotBlank @Size(max = 120) String municipio,
            @NotBlank @Size(max = 120) String provincia,
            @NotNull ComunidadAutonoma comunidadAutonoma,
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 150) String apellidos,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 128) String contrasena) {
    }
}
