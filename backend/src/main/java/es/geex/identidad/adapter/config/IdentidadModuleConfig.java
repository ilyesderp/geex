package es.geex.identidad.adapter.config;

import es.geex.compartido.adapter.transaccion.CasosDeUso;
import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.application.PublicadorEventos;
import es.geex.identidad.application.port.in.AutenticacionUseCase;
import es.geex.identidad.application.port.in.ConsultaUsuarios;
import es.geex.identidad.application.port.in.CuentaUseCase;
import es.geex.identidad.application.port.in.GestionPermisosUseCase;
import es.geex.identidad.application.port.in.GestionUsuariosUseCase;
import es.geex.identidad.application.port.in.InicializarDespachoUseCase;
import es.geex.identidad.application.port.in.InstalacionUseCase;
import es.geex.identidad.application.port.in.PerfilUseCase;
import es.geex.identidad.application.port.out.CerrojoInstalacion;
import es.geex.identidad.application.port.out.CifradorContrasenas;
import es.geex.identidad.application.port.out.DespachosPort;
import es.geex.identidad.application.port.out.EmisorTokensAcceso;
import es.geex.identidad.application.port.out.MatrizPermisosRepository;
import es.geex.identidad.application.port.out.MembresiaRepository;
import es.geex.identidad.application.port.out.NotificadorCuenta;
import es.geex.identidad.application.port.out.SesionRefrescoRepository;
import es.geex.identidad.application.port.out.TokenUnUsoRepository;
import es.geex.identidad.application.port.out.UsuarioRepository;
import es.geex.identidad.application.service.AutenticacionService;
import es.geex.identidad.application.service.CuentaService;
import es.geex.identidad.application.service.GestionPermisosService;
import es.geex.identidad.application.service.GestionUsuariosService;
import es.geex.identidad.application.service.InstalacionService;
import es.geex.identidad.application.service.PerfilService;
import es.geex.identidad.application.service.PoliticaSesiones;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Composición del módulo Identidad. Los servicios son Java plano; aquí se crean
 * y se exponen solo a través de sus puertos de entrada, envueltos en transacciones.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PropiedadesSeguridad.class)
class IdentidadModuleConfig {

    @Bean
    PoliticaSesiones politicaSesiones(PropiedadesSeguridad p) {
        return new PoliticaSesiones(p.duracionRefresco(), p.duracionInvitacion(), p.duracionRestablecimiento());
    }

    @Bean
    AutenticacionUseCase autenticacionUseCase(UsuarioRepository usuarios, MembresiaRepository membresias,
                                              SesionRefrescoRepository sesiones, CifradorContrasenas cifrador,
                                              EmisorTokensAcceso emisor, ContextoUsuario contexto,
                                              PublicadorEventos eventos, PoliticaSesiones politica, Clock reloj,
                                              PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(AutenticacionUseCase.class, new AutenticacionService(usuarios, membresias,
                sesiones, cifrador, emisor, contexto, eventos, politica, reloj), tx);
    }

    @Bean
    InstalacionUseCase instalacionUseCase(DespachosPort despachos, UsuarioRepository usuarios,
                                          MembresiaRepository membresias, CifradorContrasenas cifrador,
                                          CerrojoInstalacion cerrojo, PublicadorEventos eventos, Clock reloj,
                                          PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(InstalacionUseCase.class,
                new InstalacionService(despachos, usuarios, membresias, cifrador, cerrojo, eventos, reloj), tx);
    }

    @Bean
    CuentaUseCase cuentaUseCase(UsuarioRepository usuarios, TokenUnUsoRepository tokens,
                                SesionRefrescoRepository sesiones, CifradorContrasenas cifrador,
                                NotificadorCuenta notificador, ContextoUsuario contexto, PublicadorEventos eventos,
                                PoliticaSesiones politica, Clock reloj, PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(CuentaUseCase.class, new CuentaService(usuarios, tokens, sesiones, cifrador,
                notificador, contexto, eventos, politica, reloj), tx);
    }

    @Bean
    PerfilUseCase perfilUseCase(UsuarioRepository usuarios, MembresiaRepository membresias,
                                MatrizPermisosRepository matrices, DespachosPort despachos, ContextoUsuario contexto,
                                PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(PerfilUseCase.class,
                new PerfilService(usuarios, membresias, matrices, despachos, contexto), tx);
    }

    @Bean
    GestionUsuariosUseCase gestionUsuariosUseCase(UsuarioRepository usuarios, MembresiaRepository membresias,
                                                  TokenUnUsoRepository tokens, SesionRefrescoRepository sesiones,
                                                  NotificadorCuenta notificador, DespachosPort despachos,
                                                  ContextoUsuario contexto, PublicadorEventos eventos,
                                                  PoliticaSesiones politica, Clock reloj, PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(GestionUsuariosUseCase.class, new GestionUsuariosService(usuarios, membresias,
                tokens, sesiones, notificador, despachos, contexto, eventos, politica, reloj), tx);
    }

    /** No es candidato por defecto: solo se inyectan los proxies transaccionales de sus puertos. */
    @Bean(defaultCandidate = false)
    GestionPermisosService gestionPermisosService(MatrizPermisosRepository matrices, UsuarioRepository usuarios,
                                                  ContextoUsuario contexto, PublicadorEventos eventos, Clock reloj) {
        return new GestionPermisosService(matrices, usuarios, contexto, eventos, reloj);
    }

    @Bean
    GestionPermisosUseCase gestionPermisosUseCase(@Qualifier("gestionPermisosService") GestionPermisosService s,
                                                  PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(GestionPermisosUseCase.class, s, tx);
    }

    @Bean
    ConsultaUsuarios consultaUsuarios(@Qualifier("gestionPermisosService") GestionPermisosService s,
                                      PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(ConsultaUsuarios.class, s, tx);
    }

    @Bean
    InicializarDespachoUseCase inicializarDespachoIdentidad(@Qualifier("gestionPermisosService") GestionPermisosService s,
                                                            PlatformTransactionManager tx) {
        return CasosDeUso.transaccional(InicializarDespachoUseCase.class, s, tx);
    }
}
