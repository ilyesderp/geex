package es.geex.identidad.adapter.in.web;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.geex.identidad.adapter.config.PropiedadesSeguridad;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * Limita la frecuencia de peticiones sensibles (inicio de sesión, recuperación
 * de contraseña) por dirección IP, con el algoritmo de "token bucket".
 *
 * <p>Es en memoria: suficiente con una sola instancia. Con varias instancias se
 * sustituiría por Bucket4j sobre Redis (ver docs/adr).
 */
@Component
class LimitadorPeticiones {

    enum Limite {
        INICIO_SESION(10, Duration.ofMinutes(1)),
        RECUPERACION(5, Duration.ofMinutes(15)),
        INSTALACION(10, Duration.ofMinutes(10));

        final int capacidad;
        final Duration periodo;

        Limite(int capacidad, Duration periodo) {
            this.capacidad = capacidad;
            this.periodo = periodo;
        }
    }

    private final Cache<String, Bucket> cubos = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .maximumSize(100_000)
            .build();

    private final boolean activo;

    LimitadorPeticiones(PropiedadesSeguridad propiedades) {
        this.activo = propiedades.limitarPeticiones();
    }

    boolean permitir(Limite limite, String clave) {
        if (!activo) {
            return true;
        }
        return cubos.get(limite.name() + ":" + clave, k -> Bucket.builder()
                        .addLimit(l -> l.capacity(limite.capacidad).refillGreedy(limite.capacidad, limite.periodo))
                        .build())
                .tryConsume(1);
    }
}
