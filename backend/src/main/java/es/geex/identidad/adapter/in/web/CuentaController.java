package es.geex.identidad.adapter.in.web;

import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import es.geex.identidad.application.port.in.CuentaUseCase;
import es.geex.identidad.application.port.in.CuentaUseCase.InfoEnlace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Los tokens de los enlaces llegan siempre en el cuerpo (POST), nunca en la URL,
 * para que no queden en registros de acceso ni en el historial.
 */
@RestController
@RequestMapping("/api/cuenta")
@Tag(name = "Cuenta", description = "Activación de cuenta y contraseña")
class CuentaController {

    private final CuentaUseCase cuenta;
    private final LimitadorPeticiones limitador;

    CuentaController(CuentaUseCase cuenta, LimitadorPeticiones limitador) {
        this.cuenta = cuenta;
        this.limitador = limitador;
    }

    @PostMapping("/enlaces/consultar")
    @Operation(summary = "Comprobar un enlace de invitación o de restablecimiento")
    InfoEnlace consultarEnlace(@Valid @RequestBody TokenPeticion p) {
        return ContextoEjecucion.comoSistema(() -> cuenta.consultarEnlace(p.token()));
    }

    @PostMapping("/activar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Activar una cuenta invitada eligiendo la contraseña")
    void activar(@Valid @RequestBody TokenYContrasena p) {
        ContextoEjecucion.comoSistema(() -> cuenta.activarCuenta(p.token(), p.contrasena()));
    }

    @PostMapping("/recuperar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Solicitar un enlace para restablecer la contraseña")
    void recuperar(@Valid @RequestBody EmailPeticion p, HttpServletRequest peticion) {
        if (!limitador.permitir(LimitadorPeticiones.Limite.RECUPERACION, peticion.getRemoteAddr())) {
            throw new DemasiadasPeticionesException();
        }
        ContextoEjecucion.comoSistema(() -> cuenta.solicitarRestablecimiento(p.email()));
    }

    @PostMapping("/restablecer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elegir una contraseña nueva con el enlace recibido")
    void restablecer(@Valid @RequestBody TokenYContrasena p) {
        ContextoEjecucion.comoSistema(() -> cuenta.restablecerContrasena(p.token(), p.contrasena()));
    }

    @PostMapping("/cambiar-contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cambiar la contraseña propia (cierra todas las sesiones)")
    void cambiarContrasena(@Valid @RequestBody CambioContrasena p) {
        cuenta.cambiarContrasena(p.actual(), p.nueva());
    }

    record TokenPeticion(@NotBlank @Size(max = 200) String token) {
    }

    record TokenYContrasena(@NotBlank @Size(max = 200) String token, @NotBlank @Size(max = 128) String contrasena) {
    }

    record EmailPeticion(@NotBlank @Size(max = 254) String email) {
    }

    record CambioContrasena(@NotBlank @Size(max = 128) String actual, @NotBlank @Size(max = 128) String nueva) {
    }
}
