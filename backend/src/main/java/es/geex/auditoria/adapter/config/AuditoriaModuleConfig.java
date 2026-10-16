package es.geex.auditoria.adapter.config;

import es.geex.auditoria.application.port.in.AuditoriaUseCase;
import es.geex.auditoria.application.port.out.NombresUsuariosPort;
import es.geex.auditoria.application.port.out.RegistroAuditoriaRepository;
import es.geex.auditoria.application.service.AuditoriaService;
import es.geex.compartido.adapter.transaccion.CasosDeUso;
import es.geex.compartido.application.ContextoUsuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
class AuditoriaModuleConfig {

    @Bean
    AuditoriaUseCase auditoriaUseCase(RegistroAuditoriaRepository registros, NombresUsuariosPort nombres,
                                      ContextoUsuario contexto, PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(AuditoriaUseCase.class, new AuditoriaService(registros, nombres, contexto), tx);
    }
}
