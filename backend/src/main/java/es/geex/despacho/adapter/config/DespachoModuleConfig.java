package es.geex.despacho.adapter.config;

import es.geex.compartido.adapter.transaccion.CasosDeUso;
import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.application.PublicadorEventos;
import es.geex.despacho.application.port.in.ConsultaDespachos;
import es.geex.despacho.application.port.in.CrearDespachoUseCase;
import es.geex.despacho.application.port.in.GestionarDespachoUseCase;
import es.geex.despacho.application.port.out.DespachoRepository;
import es.geex.despacho.application.service.DespachoService;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/** Composición del módulo: crea los casos de uso (Java plano) y los envuelve en transacciones. */
@Configuration(proxyBeanMethods = false)
class DespachoModuleConfig {

    /** No es candidato por defecto: solo se inyectan los proxies transaccionales de sus puertos. */
    @Bean(defaultCandidate = false)
    DespachoService despachoService(DespachoRepository repositorio, ContextoUsuario contexto,
                                    PublicadorEventos eventos, Clock reloj) {
        return new DespachoService(repositorio, contexto, eventos, reloj);
    }

    @Bean
    CrearDespachoUseCase crearDespachoUseCase(@Qualifier("despachoService") DespachoService servicio, PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(CrearDespachoUseCase.class, servicio, tx);
    }

    @Bean
    GestionarDespachoUseCase gestionarDespachoUseCase(@Qualifier("despachoService") DespachoService servicio, PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(GestionarDespachoUseCase.class, servicio, tx);
    }

    @Bean
    ConsultaDespachos consultaDespachos(@Qualifier("despachoService") DespachoService servicio, PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(ConsultaDespachos.class, servicio, tx);
    }
}
