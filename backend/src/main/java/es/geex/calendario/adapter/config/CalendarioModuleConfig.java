package es.geex.calendario.adapter.config;

import es.geex.calendario.application.port.in.CalendarioUseCase;
import es.geex.calendario.application.port.in.InicializarCalendarioUseCase;
import es.geex.calendario.application.port.out.ComunidadDespachoPort;
import es.geex.calendario.application.port.out.DiaInhabilRepository;
import es.geex.calendario.application.service.CalendarioService;
import es.geex.compartido.adapter.transaccion.CasosDeUso;
import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.application.PublicadorEventos;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
class CalendarioModuleConfig {

    @Bean(defaultCandidate = false)
    CalendarioService calendarioService(DiaInhabilRepository dias, ComunidadDespachoPort comunidades,
                                        ContextoUsuario contexto, PublicadorEventos eventos, Clock reloj) {
        return new CalendarioService(dias, comunidades, contexto, eventos, reloj);
    }

    @Bean
    CalendarioUseCase calendarioUseCase(@Qualifier("calendarioService") CalendarioService s,
                                        PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(CalendarioUseCase.class, s, tx);
    }

    @Bean
    InicializarCalendarioUseCase inicializarCalendarioUseCase(@Qualifier("calendarioService") CalendarioService s,
                                                              PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(InicializarCalendarioUseCase.class, s, tx);
    }
}
