package es.geex.identidad.adapter.in.web;

import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import es.geex.compartido.domain.AutenticacionFallidaException;
import es.geex.compartido.domain.DespachoId;
import es.geex.identidad.application.port.in.AutenticacionUseCase;
import es.geex.identidad.application.port.in.AutenticacionUseCase.Credenciales;
import es.geex.identidad.application.port.in.AutenticacionUseCase.Origen;
import es.geex.identidad.application.port.in.AutenticacionUseCase.ResultadoSesion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Autenticación: inicio, renovación y cierre de sesión")
class AutenticacionController {

    /** Cabecera obligatoria en el refresco: un formulario de otro sitio no puede añadirla. */
    static final String CABECERA_REFRESCO = "X-GEEX-Refresco";

    private final AutenticacionUseCase autenticacion;
    private final CookieRefresco cookie;
    private final LimitadorPeticiones limitador;
    private final Clock reloj;

    AutenticacionController(AutenticacionUseCase autenticacion, CookieRefresco cookie, LimitadorPeticiones limitador,
                            Clock reloj) {
        this.autenticacion = autenticacion;
        this.cookie = cookie;
        this.limitador = limitador;
        this.reloj = reloj;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con correo y contraseña")
    ResponseEntity<TokenAccesoRespuesta> login(@Valid @RequestBody LoginPeticion p, HttpServletRequest peticion) {
        if (!limitador.permitir(LimitadorPeticiones.Limite.INICIO_SESION, peticion.getRemoteAddr())) {
            throw new DemasiadasPeticionesException();
        }
        var resultado = ContextoEjecucion.comoSistema(() ->
                autenticacion.iniciarSesion(new Credenciales(p.email(), p.contrasena(), origen(peticion))));
        return responder(resultado);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar el token de acceso con la cookie de refresco")
    ResponseEntity<TokenAccesoRespuesta> refrescar(
            @RequestHeader(name = CABECERA_REFRESCO, required = false) String cabecera,
            @Parameter(hidden = true) @CookieValue(name = CookieRefresco.NOMBRE, required = false) String token,
            HttpServletRequest peticion) {
        if (cabecera == null) {
            throw new AutenticacionFallidaException("CABECERA_REQUERIDA", "Petición de refresco no válida");
        }
        var resultado = ContextoEjecucion.comoSistema(() -> autenticacion.refrescar(token, origen(peticion)));
        return responder(resultado);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión")
    ResponseEntity<Void> logout(@Parameter(hidden = true) @CookieValue(name = CookieRefresco.NOMBRE, required = false) String token) {
        ContextoEjecucion.comoSistema(() -> autenticacion.cerrarSesion(token));
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.borrar()).build();
    }

    @PostMapping("/cambiar-despacho")
    @Operation(summary = "Cambiar al despacho indicado (para usuarios con acceso a varios)")
    ResponseEntity<TokenAccesoRespuesta> cambiarDespacho(
            @Valid @RequestBody CambioDespachoPeticion p,
            @Parameter(hidden = true) @CookieValue(name = CookieRefresco.NOMBRE, required = false) String token,
            HttpServletRequest peticion) {
        var resultado = ContextoEjecucion.comoSistema(() ->
                autenticacion.cambiarDespacho(new DespachoId(p.despachoId()), token, origen(peticion)));
        return responder(resultado);
    }

    private ResponseEntity<TokenAccesoRespuesta> responder(ResultadoSesion resultado) {
        return switch (resultado) {
            case ResultadoSesion.Emitida(var sesion) -> {
                Instant ahora = reloj.instant();
                yield ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.crear(sesion.tokenRefresco(), sesion.refrescoExpiraEn(), ahora))
                        .header(HttpHeaders.CACHE_CONTROL, "no-store")
                        .body(new TokenAccesoRespuesta(sesion.tokenAcceso(), sesion.accesoExpiraEn()));
            }
            case ResultadoSesion.Rechazada(var codigo, var mensaje) -> throw new AutenticacionFallidaException(codigo, mensaje);
        };
    }

    private static Origen origen(HttpServletRequest peticion) {
        return new Origen(peticion.getRemoteAddr(), peticion.getHeader(HttpHeaders.USER_AGENT));
    }

    record LoginPeticion(@NotBlank @Size(max = 254) String email, @NotBlank @Size(max = 128) String contrasena) {
    }

    record CambioDespachoPeticion(@NotNull UUID despachoId) {
    }

    record TokenAccesoRespuesta(String tokenAcceso, Instant expiraEn) {
    }
}
