package es.geex.compartido.adapter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

/**
 * Las respuestas de la API siempre incluyen todos sus campos (pueden ser
 * {@code null}, pero están). Se marcan como obligatorios en el contrato OpenAPI
 * para que el cliente TypeScript generado tenga tipos exactos en vez de campos
 * opcionales. Los cuerpos de petición no se tocan: su obligatoriedad sale de las
 * anotaciones de validación ({@code @NotBlank}, {@code @NotNull}...).
 */
@Component
class CamposObligatoriosEnRespuestas implements OpenApiCustomizer {

    private static final String PREFIJO_REF = "#/components/schemas/";

    @Override
    public void customise(OpenAPI api) {
        if (api.getComponents() == null || api.getComponents().getSchemas() == null || api.getPaths() == null) {
            return;
        }
        Map<String, Schema> esquemas = api.getComponents().getSchemas();
        Deque<String> pendientes = new ArrayDeque<>();
        api.getPaths().values().forEach(ruta -> ruta.readOperations().forEach(operacion -> {
            if (operacion.getResponses() != null) {
                operacion.getResponses().values().stream()
                        .filter(r -> r.getContent() != null)
                        .flatMap(r -> r.getContent().values().stream())
                        .map(mt -> mt.getSchema())
                        .filter(Objects::nonNull)
                        .forEach(esquema -> referencias(esquema, pendientes));
            }
        }));

        Set<String> visitados = new HashSet<>();
        while (!pendientes.isEmpty()) {
            String nombre = pendientes.pop();
            Schema<?> esquema = esquemas.get(nombre);
            if (esquema == null || !visitados.add(nombre) || esquema.getProperties() == null) {
                continue;
            }
            esquema.setRequired(new ArrayList<>(esquema.getProperties().keySet()));
            esquema.getProperties().values().forEach(propiedad -> referencias(propiedad, pendientes));
        }
    }

    private static void referencias(Schema<?> esquema, Deque<String> pendientes) {
        if (esquema.get$ref() != null && esquema.get$ref().startsWith(PREFIJO_REF)) {
            pendientes.push(esquema.get$ref().substring(PREFIJO_REF.length()));
        }
        if (esquema.getItems() != null) {
            referencias(esquema.getItems(), pendientes);
        }
    }
}
