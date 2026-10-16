package es.geex.soporte;

import es.geex.GeexBackendApplication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base de las pruebas de integración: la aplicación completa contra un
 * PostgreSQL real (la misma imagen y el mismo script de rol que en local), con
 * peticiones HTTP reales.
 *
 * <p>Las credenciales del contenedor y la clave JWT se generan al azar en cada
 * ejecución: no hay ningún secreto en el código.
 */
@SpringBootTest(
        classes = {GeexBackendApplication.class, PruebaIntegracion.ConfiguracionPruebas.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "geex.seguridad.limitar-peticiones=false")
public abstract class PruebaIntegracion {

    protected static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String CONTRASENA_APP = aleatorio(24);

    protected static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("pgvector/pgvector:0.8.7-pg17-bookworm").asCompatibleSubstituteFor("postgres"))
            .withEnv("GEEX_DB_APP_PASSWORD", CONTRASENA_APP)
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of("../infra/docker/postgres/initdb/01-crear-rol-aplicacion.sh"), 0755),
                    "/docker-entrypoint-initdb.d/01-crear-rol-aplicacion.sh");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registro.add("spring.datasource.username", () -> "geex_app");
        registro.add("spring.datasource.password", () -> CONTRASENA_APP);
        registro.add("spring.flyway.user", POSTGRES::getUsername);
        registro.add("spring.flyway.password", POSTGRES::getPassword);
        registro.add("geex.seguridad.jwt-secreto", () -> aleatorio(48));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracionPruebas {
        @Bean
        @Primary
        CorreosCapturados correosCapturados() {
            return new CorreosCapturados();
        }
    }

    @LocalServerPort
    protected int puerto;

    @Autowired
    protected CorreosCapturados correos;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    // ── Datos de prueba ──────────────────────────────────────────────────────

    public record Credenciales(String email, String contrasena) {
    }

    public record Sesion(String token, String cookieRefresco) {
    }

    /** Deja la base de datos como recién creada (conserva los festivos nacionales y autonómicos). */
    protected static void reiniciarBaseDeDatos() {
        try (Connection c = conexionAdministrador(); var s = c.createStatement()) {
            s.execute("""
                    truncate registros_auditoria, event_publication, sesiones_refresco, tokens_un_uso, permisos_rol,
                             valores_catalogo, membresias, usuarios cascade;
                    delete from dias_inhabiles where ambito = 'LOCAL';
                    delete from despachos;""");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    protected Credenciales instalar(String nombreDespacho) {
        var titular = new Credenciales("titular-" + UUID.randomUUID() + "@despacho.test", contrasenaNueva());
        var respuesta = post("/api/instalacion", null, """
                {"nombreDespacho":"%s","municipio":"Alicante","provincia":"Alicante",
                 "comunidadAutonoma":"COMUNITAT_VALENCIANA","nombre":"Ana","apellidos":"Titular",
                 "email":"%s","contrasena":"%s"}""".formatted(nombreDespacho, titular.email(), titular.contrasena()));
        if (respuesta.estado() != 201) {
            throw new AssertionError("La instalación falló: " + respuesta);
        }
        return titular;
    }

    protected Sesion iniciarSesion(Credenciales c) {
        var respuesta = post("/api/auth/login", null,
                "{\"email\":\"%s\",\"contrasena\":\"%s\"}".formatted(c.email(), c.contrasena()));
        if (respuesta.estado() != 200) {
            throw new AssertionError("No se pudo iniciar sesión: " + respuesta);
        }
        return new Sesion(respuesta.json().get("tokenAcceso").asString(), respuesta.cookieRefresco());
    }

    protected static String contrasenaNueva() {
        return "Clave-" + aleatorio(12);
    }

    // ── HTTP ─────────────────────────────────────────────────────────────────

    public record Respuesta(int estado, String cuerpo, java.net.http.HttpHeaders cabeceras) {

        public JsonNode json() {
            return cuerpo == null || cuerpo.isBlank() ? JSON.nullNode() : JSON.readTree(cuerpo);
        }

        public String codigoError() {
            return json().path("codigo").asString();
        }

        public String cookieRefresco() {
            return cabeceras.allValues("Set-Cookie").stream()
                    .filter(c -> c.startsWith("geex_refresco="))
                    .map(c -> c.substring(0, c.indexOf(';')))
                    .findFirst().orElse(null);
        }
    }

    protected Respuesta get(String ruta, String token) {
        return enviar(peticion(ruta, token).GET());
    }

    protected Respuesta post(String ruta, String token, String json) {
        return enviar(peticion(ruta, token).header("Content-Type", "application/json")
                .POST(json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json)));
    }

    protected Respuesta put(String ruta, String token, String json) {
        return enviar(peticion(ruta, token).header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)));
    }

    protected Respuesta delete(String ruta, String token) {
        return enviar(peticion(ruta, token).DELETE());
    }

    protected Respuesta refrescar(String cookie) {
        return enviar(peticion("/api/auth/refresh", null)
                .header("Cookie", cookie)
                .header("X-GEEX-Refresco", "1")
                .POST(HttpRequest.BodyPublishers.noBody()));
    }

    private HttpRequest.Builder peticion(String ruta, String token) {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .timeout(Duration.ofSeconds(30));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private Respuesta enviar(HttpRequest.Builder builder) {
        try {
            HttpResponse<String> r = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new Respuesta(r.statusCode(), r.body(), r.headers());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    protected static Connection conexionAdministrador() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    /** Conexión con el rol de la aplicación (sujeto a Row Level Security). */
    protected static Connection conexionAplicacion() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), "geex_app", CONTRASENA_APP);
    }

    /** Espera a que se cumpla una condición (p. ej. un evento procesado de forma asíncrona). */
    protected static void esperarA(Supplier<Boolean> condicion) {
        long limite = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (System.nanoTime() < limite) {
            if (condicion.get()) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new AssertionError("La condición no se cumplió en 10 segundos");
    }

    private static String aleatorio(int bytes) {
        byte[] b = new byte[bytes];
        new SecureRandom().nextBytes(b);
        return Base64.getEncoder().encodeToString(b);
    }
}
