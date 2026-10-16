package es.geex.identidad.adapter.in.web.seguridad;

import es.geex.identidad.adapter.out.token.ClaimsGeex;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * API sin estado: cada petición lleva un JWT de acceso en la cabecera
 * {@code Authorization}. El token de refresco viaja solo en una cookie HttpOnly
 * limitada a {@code /api/auth}, así que no hace falta protección CSRF de sesión
 * (además, el endpoint de refresco exige una cabecera propia).
 */
@Configuration(proxyBeanMethods = false)
class ConfiguracionSeguridad {

    @Bean
    SecurityFilterChain cadenaSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
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
}
