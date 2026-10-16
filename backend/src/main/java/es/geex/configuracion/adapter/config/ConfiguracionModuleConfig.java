package es.geex.configuracion.adapter.config;

import es.geex.compartido.adapter.transaccion.CasosDeUso;
import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.application.PublicadorEventos;
import es.geex.configuracion.application.port.in.CatalogosUseCase;
import es.geex.configuracion.application.port.in.InicializarCatalogosUseCase;
import es.geex.configuracion.application.port.out.ValorCatalogoRepository;
import es.geex.configuracion.application.service.CatalogosService;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
class ConfiguracionModuleConfig {

    @Bean(defaultCandidate = false)
    CatalogosService catalogosService(ValorCatalogoRepository valores, ContextoUsuario contexto,
                                      PublicadorEventos eventos, Clock reloj) {
        return new CatalogosService(valores, contexto, eventos, reloj);
    }

    @Bean
    CatalogosUseCase catalogosUseCase(@Qualifier("catalogosService") CatalogosService s, PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(CatalogosUseCase.class, s, tx);
    }

    @Bean
    InicializarCatalogosUseCase inicializarCatalogosUseCase(@Qualifier("catalogosService") CatalogosService s,
                                                            PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(InicializarCatalogosUseCase.class, s, tx);
    }
}
