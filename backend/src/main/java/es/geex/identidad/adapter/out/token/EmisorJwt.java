package es.geex.identidad.adapter.out.token;

import es.geex.identidad.adapter.config.PropiedadesSeguridad;
import es.geex.identidad.application.port.out.EmisorTokensAcceso;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
class EmisorJwt implements EmisorTokensAcceso {

    private final JwtEncoder encoder;
    private final PropiedadesSeguridad propiedades;

    EmisorJwt(JwtEncoder encoder, PropiedadesSeguridad propiedades) {
        this.encoder = encoder;
        this.propiedades = propiedades;
    }

    @Override
    public TokenAcceso emitir(DatosToken datos, Instant ahora) {
        Instant expira = ahora.plus(propiedades.duracionAcceso());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(propiedades.emisor())
                .audience(List.of(ClaimsGeex.AUDIENCIA_PERSONAL))
                .subject(datos.usuario().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim(ClaimsGeex.DESPACHO, datos.despacho().toString())
                .claim(ClaimsGeex.ROL, datos.rol().name())
                .claim(ClaimsGeex.SUPERADMIN, datos.superadmin())
                .claim(ClaimsGeex.VERSION_TOKEN, datos.versionToken())
                .build();
        String valor = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new TokenAcceso(valor, expira);
    }
}
