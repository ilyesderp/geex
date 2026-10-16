package es.geex.identidad.adapter.out.token;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import es.geex.identidad.adapter.config.PropiedadesSeguridad;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Firma y validación de los JWT con HS256 (clave simétrica). Es adecuado porque
 * el mismo servicio emite y valida los tokens; si en el futuro los validara otro
 * servicio, se cambiaría a RS256 con JWKS sin tocar la capa de aplicación.
 */
@Configuration(proxyBeanMethods = false)
class ConfiguracionJwt {

    private static final int BYTES_MINIMOS = 32;

    @Bean
    SecretKey claveJwt(PropiedadesSeguridad propiedades) {
        String secreto = propiedades.jwtSecreto();
        if (secreto == null || secreto.isBlank()) {
            throw new IllegalStateException(
                    "Falta geex.seguridad.jwt-secreto (variable GEEX_JWT_SECRET). Ejecuta scripts/setup para generarla.");
        }
        byte[] bytes = Base64.getDecoder().decode(secreto.strip());
        if (bytes.length < BYTES_MINIMOS) {
            throw new IllegalStateException("La clave JWT debe tener al menos " + BYTES_MINIMOS + " bytes (256 bits)");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey clave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey clave, PropiedadesSeguridad propiedades) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(propiedades.emisor()),
                new JwtClaimValidator<java.util.List<String>>(JwtClaimNames.AUD,
                        aud -> aud != null && aud.contains(ClaimsGeex.AUDIENCIA_PERSONAL)),
                new JwtClaimValidator<String>(ClaimsGeex.DESPACHO, d -> d != null && !d.isBlank()),
                new JwtClaimValidator<String>(ClaimsGeex.ROL, r -> r != null && !r.isBlank())));
        return decoder;
    }
}
