package es.geex.compartido.adapter.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration(proxyBeanMethods = false)
@EnableAsync
class ConfiguracionGeneral {

    /** Reloj inyectable: los casos de uso nunca llaman a {@code Instant.now()} directamente, así los tests controlan el tiempo. */
    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }

    @Bean
    OpenAPI documentacionApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("GEEX API")
                        .description("API de GEEX, gestión de expedientes de extranjería")
                        .version("0.1"))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }
}
