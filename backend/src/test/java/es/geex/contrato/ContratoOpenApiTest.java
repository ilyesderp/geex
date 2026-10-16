package es.geex.contrato;

import static org.assertj.core.api.Assertions.assertThat;

import es.geex.soporte.PruebaIntegracion;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Contrato de la API con el frontend. El frontend genera su cliente TypeScript a
 * partir de {@code frontend/openapi/geex-api.json}; este test falla si la API
 * cambia y el archivo no se ha actualizado, así el frontend nunca se desincroniza.
 *
 * <p>Para regenerarlo: {@code ./mvnw test -Dtest=ContratoOpenApiTest -Dgeex.contrato.actualizar=true}
 */
class ContratoOpenApiTest extends PruebaIntegracion {

    private static final Path CONTRATO = Path.of("../frontend/openapi/geex-api.json");
    private static final JsonMapper CANONICO = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    @Test
    void el_contrato_openapi_del_frontend_esta_actualizado() throws IOException {
        var respuesta = get("/v3/api-docs", null);
        assertThat(respuesta.estado()).isEqualTo(200);

        @SuppressWarnings("unchecked")
        Map<String, Object> especificacion = JSON.readValue(respuesta.cuerpo(), Map.class);
        especificacion.remove("servers"); // contiene el puerto aleatorio del test
        String actual = CANONICO.writeValueAsString(ordenar(especificacion)).replace("\r\n", "\n") + "\n";

        if (Boolean.getBoolean("geex.contrato.actualizar")) {
            Files.createDirectories(CONTRATO.getParent());
            Files.writeString(CONTRATO, actual, StandardCharsets.UTF_8);
            return;
        }
        assertThat(CONTRATO).as("Falta el contrato OpenAPI; genéralo con -Dgeex.contrato.actualizar=true").exists();
        assertThat(Files.readString(CONTRATO, StandardCharsets.UTF_8).replace("\r\n", "\n"))
                .as("La API ha cambiado: regenera frontend/openapi/geex-api.json con -Dgeex.contrato.actualizar=true")
                .isEqualTo(actual);
    }

    /** Ordena las claves para que el archivo sea estable entre ejecuciones. */
    @SuppressWarnings("unchecked")
    private static Object ordenar(Object valor) {
        if (valor instanceof Map<?, ?> mapa) {
            var ordenado = new TreeMap<String, Object>();
            mapa.forEach((k, v) -> ordenado.put(String.valueOf(k), ordenar(v)));
            return ordenado;
        }
        if (valor instanceof List<?> lista) {
            return lista.stream().map(ContratoOpenApiTest::ordenar).toList();
        }
        return valor;
    }
}
