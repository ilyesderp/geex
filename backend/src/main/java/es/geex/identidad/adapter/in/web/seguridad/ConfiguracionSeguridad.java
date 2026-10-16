package es.geex.identidad.adapter.in.web.seguridad;

import es.geex.identidad.adapter.out.token.ClaimsGeex;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * API sin estado: cada petición lleva un JWT de acceso en la cabecera
 * {@code Authorization}, que el navegador nunca envía por su cuenta.
 *
 * <p>La única credencial automática es la cookie de refresco (HttpOnly,
 * SameSite=Strict, limitada a {@code /api/auth}). Como defensa CSRF adicional,
 * toda petición que modifica datos y lleva esa cookie debe incluir la cabecera
 * {@code X-GEEX-Refresco}: un formulario de otro sitio no puede añadirla y una
 * petición entre orígenes con ella requiere CORS, que no está permitido.
 */
@Configuration(proxyBeanMethods = false)
class ConfiguracionSeguridad {

    static final String COOKIE_REFRESCO = "geex_refresco";
    static final String CABECERA_REFRESCO = "X-GEEX-Refresco";
    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Bean
    SecurityFilterChain cadenaSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .requireCsrfProtectionMatcher(ConfiguracionSeguridad::cookieSinCabeceraPropia)
                        .csrfTokenRepository(new SinTokensCsrf()))
                .exceptionHandling(e -> e.defaultAccessDeniedHandlerFor(
                        ConfiguracionSeguridad::rechazarCsrf, ConfiguracionSeguridad::cookieSinCabeceraPropia))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .headers(h -> h
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh", "/api/auth/logout")
                        .permitAll()
                        .requestMatchers("/api/instalacion", "/api/instalacion/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cuenta/enlaces/consultar", "/api/cuenta/activar",
                                "/api/cuenta/recuperar", "/api/cuenta/restablecer").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(token ->
                        new JwtAuthenticationToken(token,
                                List.of(new SimpleGrantedAuthority("ROL_" + token.getClaimAsString(ClaimsGeex.ROL))),
                                token.getSubject()))))
                .addFilterAfter(new FiltroContextoEjecucion(), BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    /** Petición que modifica datos, lleva la cookie de refresco y no trae la cabecera propia. */
    static boolean cookieSinCabeceraPropia(HttpServletRequest peticion) {
        if (METODOS_SEGUROS.contains(peticion.getMethod()) || peticion.getHeader(CABECERA_REFRESCO) != null) {
            return false;
        }
        Cookie[] cookies = peticion.getCookies();
        return cookies != null && Arrays.stream(cookies).anyMatch(c -> COOKIE_REFRESCO.equals(c.getName()));
    }

    /** Respuesta RFC 9457 con un código estable, sin pasar por la página de error. */
    private static void rechazarCsrf(HttpServletRequest peticion, HttpServletResponse respuesta,
                                     AccessDeniedException e) throws IOException {
        respuesta.setStatus(HttpServletResponse.SC_FORBIDDEN);
        respuesta.setContentType("application/problem+json");
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write("""
                {"type":"about:blank","title":"Petición rechazada","status":403,                "detail":"Falta la cabecera %s","codigo":"CABECERA_REQUERIDA"}""".formatted(CABECERA_REFRESCO));
    }

    /**
     * No hay tokens CSRF que guardar (la defensa es la cabecera propia): cualquier
     * petición que el filtro considere sospechosa se rechaza con 403.
     */
    private static final class SinTokensCsrf implements CsrfTokenRepository {
        @Override
        public CsrfToken generateToken(HttpServletRequest peticion) {
            return new DefaultCsrfToken(CABECERA_REFRESCO, "_csrf", UUID.randomUUID().toString());
        }

        @Override
        public void saveToken(CsrfToken token, HttpServletRequest peticion, HttpServletResponse respuesta) {
            // Nada que guardar: la API no tiene estado
        }

        @Override
        public CsrfToken loadToken(HttpServletRequest peticion) {
            return null;
        }
    }
}
