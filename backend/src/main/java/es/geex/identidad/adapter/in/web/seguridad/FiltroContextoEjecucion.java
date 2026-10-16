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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Tras validar el JWT, ejecuta el resto de la petición dentro del
 * {@link ContextoEjecucion} del usuario: el despacho sale siempre del token
 * firmado, nunca de parámetros de la petición.
 */
class FiltroContextoEjecucion extends OncePerRequestFilter {

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
}
