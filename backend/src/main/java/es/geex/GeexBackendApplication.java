package es.geex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * GEEX — gestión de expedientes de extranjería.
 *
 * <p>Monolito modular con arquitectura hexagonal: cada paquete de primer nivel
 * ({@code identidad}, {@code despacho}, {@code configuracion}, ...) es un módulo con
 * sus capas {@code domain}, {@code application} y {@code adapter}. Los límites entre
 * módulos se verifican con Spring Modulith y ArchUnit en los tests.
 */
@SpringBootApplication
public class GeexBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeexBackendApplication.class, args);
    }
}
