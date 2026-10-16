package es.geex.identidad;

import static org.assertj.core.api.Assertions.assertThat;

import es.geex.soporte.PruebaIntegracion;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Con la limitación activada (valor por defecto en producción), un ataque de fuerza bruta recibe 429. */
@TestPropertySource(properties = "geex.seguridad.limitar-peticiones=true")
class LimiteFrecuenciaTest extends PruebaIntegracion {

    @Test
    void tras_diez_intentos_por_minuto_desde_la_misma_ip_responde_429() {
        for (int i = 0; i < 10; i++) {
            var respuesta = post("/api/auth/login", null,
                    "{\"email\":\"atacante@despacho.test\",\"contrasena\":\"prueba-%d\"}".formatted(i));
            assertThat(respuesta.estado()).isEqualTo(401);
        }
        var bloqueada = post("/api/auth/login", null, "{\"email\":\"atacante@despacho.test\",\"contrasena\":\"x\"}");
        assertThat(bloqueada.estado()).isEqualTo(429);
        assertThat(bloqueada.codigoError()).isEqualTo("DEMASIADAS_PETICIONES");
    }
}
