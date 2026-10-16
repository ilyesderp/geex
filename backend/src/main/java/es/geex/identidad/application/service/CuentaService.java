package es.geex.identidad.application.service;

import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.application.PublicadorEventos;
import es.geex.compartido.domain.AutenticacionFallidaException;
import es.geex.compartido.domain.NoEncontradoException;
import es.geex.identidad.application.port.in.CuentaUseCase;
import es.geex.identidad.application.port.out.CifradorContrasenas;
import es.geex.identidad.application.port.out.NotificadorCuenta;
import es.geex.identidad.application.port.out.SesionRefrescoRepository;
import es.geex.identidad.application.port.out.TokenUnUsoRepository;
import es.geex.identidad.application.port.out.UsuarioRepository;
import es.geex.identidad.domain.PoliticaContrasenas;
import es.geex.identidad.domain.TokenUnUso;
import es.geex.identidad.domain.Usuario;
import es.geex.identidad.domain.evento.EventosIdentidad.ContrasenaCambiada;
import es.geex.identidad.domain.evento.EventosIdentidad.ContrasenaRestablecida;
import es.geex.identidad.domain.evento.EventosIdentidad.CuentaActivada;
import java.time.Clock;
import java.time.Instant;

public class CuentaService implements CuentaUseCase {

    private final UsuarioRepository usuarios;
    private final TokenUnUsoRepository tokens;
    private final SesionRefrescoRepository sesiones;
    private final CifradorContrasenas cifrador;
    private final NotificadorCuenta notificador;
    private final ContextoUsuario contexto;
    private final PublicadorEventos eventos;
    private final PoliticaSesiones politica;
    private final Clock reloj;

    public CuentaService(UsuarioRepository usuarios, TokenUnUsoRepository tokens, SesionRefrescoRepository sesiones,
                         CifradorContrasenas cifrador, NotificadorCuenta notificador, ContextoUsuario contexto,
                         PublicadorEventos eventos, PoliticaSesiones politica, Clock reloj) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.sesiones = sesiones;
        this.cifrador = cifrador;
        this.notificador = notificador;
        this.contexto = contexto;
        this.eventos = eventos;
        this.politica = politica;
        this.reloj = reloj;
    }

    @Override
    public InfoEnlace consultarEnlace(String token) {
        TokenUnUso enlace = buscarEnlace(token);
        if (enlace.usadoEn() != null || !reloj.instant().isBefore(enlace.expiraEn())) {
            throw TokenUnUso.enlaceNoValido();
        }
        Usuario usuario = cargarUsuario(enlace);
        return new InfoEnlace(enlace.tipo().name(), usuario.email(), usuario.nombre());
    }

    @Override
    public void activarCuenta(String token, String contrasena) {
        Instant ahora = reloj.instant();
        TokenUnUso enlace = buscarEnlace(token);
        enlace.validar(TokenUnUso.Tipo.INVITACION, ahora);
        PoliticaContrasenas.validar(contrasena);
        Usuario usuario = cargarUsuario(enlace);
        usuario.establecerContrasena(cifrador.cifrar(contrasena));
        usuarios.guardar(usuario);
        enlace.usar(ahora);
        tokens.guardar(enlace);
        eventos.publicar(new CuentaActivada(enlace.despachoId(), usuario.id(), ahora));
    }

    @Override
    public void solicitarRestablecimiento(String email) {
        String normalizado;
        try {
            normalizado = Usuario.normalizarEmail(email);
        } catch (RuntimeException e) {
            return;
        }
        Instant ahora = reloj.instant();
        usuarios.buscarPorEmail(normalizado)
                .filter(u -> u.activo() && !u.pendienteActivacion())
                .ifPresent(usuario -> {
                    tokens.invalidarPendientes(usuario.id(), TokenUnUso.Tipo.RESTABLECIMIENTO, ahora);
                    String token = TokensSeguros.generar();
                    Instant expira = ahora.plus(politica.duracionRestablecimiento());
                    tokens.guardar(TokenUnUso.nuevo(usuario.id(), null, TokenUnUso.Tipo.RESTABLECIMIENTO,
                            TokensSeguros.hash(token), expira, ahora));
                    notificador.enviarRestablecimiento(usuario.email(), usuario.nombre(), token, expira);
                });
    }

    @Override
    public void restablecerContrasena(String token, String contrasena) {
        Instant ahora = reloj.instant();
        TokenUnUso enlace = buscarEnlace(token);
        enlace.validar(TokenUnUso.Tipo.RESTABLECIMIENTO, ahora);
        PoliticaContrasenas.validar(contrasena);
        Usuario usuario = cargarUsuario(enlace);
        usuario.establecerContrasena(cifrador.cifrar(contrasena));
        usuarios.guardar(usuario);
        enlace.usar(ahora);
        tokens.guardar(enlace);
        sesiones.revocarDeUsuario(usuario.id(), ahora);
        eventos.publicar(new ContrasenaRestablecida(usuario.id(), ahora));
    }

    @Override
    public void cambiarContrasena(String actual, String nueva) {
        Instant ahora = reloj.instant();
        Usuario usuario = usuarios.buscar(contexto.usuarioId())
                .orElseThrow(() -> new NoEncontradoException("USUARIO_NO_ENCONTRADO", "Usuario no encontrado"));
        if (!cifrador.coincide(actual, usuario.hashContrasena())) {
            throw new AutenticacionFallidaException("CONTRASENA_ACTUAL_INCORRECTA", "La contraseña actual no es correcta");
        }
        PoliticaContrasenas.validar(nueva);
        usuario.establecerContrasena(cifrador.cifrar(nueva));
        usuarios.guardar(usuario);
        sesiones.revocarDeUsuario(usuario.id(), ahora);
        eventos.publicar(new ContrasenaCambiada(contexto.despachoId(), usuario.id(), ahora));
    }

    private TokenUnUso buscarEnlace(String token) {
        if (token == null || token.isBlank()) {
            throw TokenUnUso.enlaceNoValido();
        }
        return tokens.buscarEnlacePorHash(TokensSeguros.hash(token)).orElseThrow(TokenUnUso::enlaceNoValido);
    }

    private Usuario cargarUsuario(TokenUnUso enlace) {
        return usuarios.buscar(enlace.usuarioId()).filter(Usuario::activo).orElseThrow(TokenUnUso::enlaceNoValido);
    }
}
