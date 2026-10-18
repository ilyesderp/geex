package es.geex.identidad.adapter.in.web.seguridad;

import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.adapter.out.token.ClaimsGeex;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Tras validar el JWT, ejecuta el resto de la petición dentro del
 * {@link ContextoEjecucion} del usuario: el despacho sale siempre del token
 * firmado, nunca de parámetros de la petición.
 *
 * <p>Antes comprueba que el token sigue correspondiendo al estado actual del
 * usuario (ver {@link ComprobadorAccesoVigente}); si no, responde 401 con el
 * código {@code SESION_REVOCADA}.
 */
class FiltroContextoEjecucion extends OncePerRequestFilter {

    private final ComprobadorAccesoVigente comprobador;

    FiltroContextoEjecucion(ComprobadorAccesoVigente comprobador) {
        this.comprobador = comprobador;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken autenticacion)) {
            cadena.doFilter(peticion, respuesta);
            return;
        }
        Jwt jwt = autenticacion.getToken();
        var datos = ContextoEjecucion.Datos.deUsuario(
                UsuarioId.de(jwt.getSubject()),
                DespachoId.de(jwt.getClaimAsString(ClaimsGeex.DESPACHO)),
                Rol.valueOf(jwt.getClaimAsString(ClaimsGeex.ROL)),
                Boolean.TRUE.equals(jwt.getClaimAsBoolean(ClaimsGeex.SUPERADMIN)));
        Object version = jwt.getClaim(ClaimsGeex.VERSION_TOKEN);
        if (!(version instanceof Number numero)
                || !comprobador.vigente(datos.usuarioId(), datos.despachoId(), datos.rol(), numero.intValue())) {
            rechazar(respuesta);
            return;
        }
        try {
            ContextoEjecucion.con(datos, () -> {
                cadena.doFilter(peticion, respuesta);
                return null;
            });
        } catch (IOException | ServletException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private static void rechazar(HttpServletResponse respuesta) throws IOException {
        respuesta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        respuesta.setContentType("application/problem+json");
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write("""
                {"type":"about:blank","title":"Sesión revocada","status":401,                "detail":"Tu acceso ha cambiado. Vuelve a iniciar sesión.","codigo":"SESION_REVOCADA"}""");
    }
}
