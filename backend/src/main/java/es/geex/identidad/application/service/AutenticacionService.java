package es.geex.identidad.application.service;

import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.application.PublicadorEventos;
import es.geex.compartido.domain.DespachoId;
import es.geex.identidad.application.port.in.AutenticacionUseCase;
import es.geex.identidad.application.port.out.CifradorContrasenas;
import es.geex.identidad.application.port.out.EmisorTokensAcceso;
import es.geex.identidad.application.port.out.MembresiaRepository;
import es.geex.identidad.application.port.out.SesionRefrescoRepository;
import es.geex.identidad.application.port.out.UsuarioRepository;
import es.geex.identidad.domain.Membresia;
import es.geex.identidad.domain.SesionRefresco;
import es.geex.identidad.domain.Usuario;
import es.geex.identidad.domain.evento.EventosIdentidad.CuentaBloqueada;
import es.geex.identidad.domain.evento.EventosIdentidad.InicioSesionFallido;
import es.geex.identidad.domain.evento.EventosIdentidad.ReutilizacionSesionDetectada;
import es.geex.identidad.domain.evento.EventosIdentidad.SesionIniciada;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class AutenticacionService implements AutenticacionUseCase {

    private static final ResultadoSesion.Rechazada CREDENCIALES_INVALIDAS =
            new ResultadoSesion.Rechazada("CREDENCIALES_INVALIDAS", "Correo electrónico o contraseña incorrectos");
    private static final ResultadoSesion.Rechazada SESION_NO_VALIDA =
            new ResultadoSesion.Rechazada("SESION_NO_VALIDA", "Tu sesión ha caducado. Vuelve a iniciar sesión.");

    private final UsuarioRepository usuarios;
    private final MembresiaRepository membresias;
    private final SesionRefrescoRepository sesiones;
    private final CifradorContrasenas cifrador;
    private final EmisorTokensAcceso emisor;
    private final ContextoUsuario contexto;
    private final PublicadorEventos eventos;
    private final PoliticaSesiones politica;
    private final Clock reloj;
    /** Hash de una contraseña aleatoria: se compara cuando el correo no existe para que la respuesta tarde lo mismo. */
    private final String hashFicticio;

    public AutenticacionService(UsuarioRepository usuarios, MembresiaRepository membresias,
                                SesionRefrescoRepository sesiones, CifradorContrasenas cifrador,
                                EmisorTokensAcceso emisor, ContextoUsuario contexto, PublicadorEventos eventos,
                                PoliticaSesiones politica, Clock reloj) {
        this.usuarios = usuarios;
        this.membresias = membresias;
        this.sesiones = sesiones;
        this.cifrador = cifrador;
        this.emisor = emisor;
        this.contexto = contexto;
        this.eventos = eventos;
        this.politica = politica;
        this.reloj = reloj;
        this.hashFicticio = cifrador.cifrar(TokensSeguros.generar());
    }

    @Override
    public ResultadoSesion iniciarSesion(Credenciales c) {
        Instant ahora = reloj.instant();
        String email;
        try {
            email = Usuario.normalizarEmail(c.email());
        } catch (RuntimeException e) {
            return CREDENCIALES_INVALIDAS;
        }
        Optional<Usuario> encontrado = usuarios.buscarPorEmail(email);
        if (encontrado.isEmpty()) {
            cifrador.coincide(c.contrasena(), hashFicticio);
            eventos.publicar(new InicioSesionFallido(null, email, "usuario desconocido", c.origen().ip(), ahora));
            return CREDENCIALES_INVALIDAS;
        }
        Usuario usuario = encontrado.get();
        if (usuario.bloqueado(ahora)) {
            eventos.publicar(new InicioSesionFallido(usuario.id(), email, "cuenta bloqueada", c.origen().ip(), ahora));
            return new ResultadoSesion.Rechazada("CUENTA_BLOQUEADA",
                    "Demasiados intentos fallidos. Por seguridad, espera unos minutos antes de volver a intentarlo.");
        }
        if (!usuario.puedeAcceder(ahora) || !cifrador.coincide(c.contrasena(), usuario.hashContrasena())) {
            if (!usuario.pendienteActivacion()) {
                usuario.registrarAccesoFallido(ahora);
                usuarios.guardar(usuario);
                if (usuario.bloqueado(ahora)) {
                    eventos.publicar(new CuentaBloqueada(usuario.id(), email, usuario.bloqueadoHasta(), ahora));
                }
            }
            eventos.publicar(new InicioSesionFallido(usuario.id(), email, "contraseña incorrecta", c.origen().ip(), ahora));
            return CREDENCIALES_INVALIDAS;
        }

        Optional<Membresia> membresia = membresias.listarActivasDeUsuario(usuario.id()).stream()
                .max(Comparator.comparing(m -> Optional.ofNullable(m.ultimoUso()).orElse(m.creadaEn())));
        if (membresia.isEmpty()) {
            eventos.publicar(new InicioSesionFallido(usuario.id(), email, "sin acceso a ningún despacho", c.origen().ip(), ahora));
            return new ResultadoSesion.Rechazada("SIN_DESPACHO",
                    "Tu usuario no tiene acceso a ningún despacho. Contacta con el titular del despacho.");
        }

        usuario.registrarAccesoCorrecto(ahora);
        usuarios.guardar(usuario);
        membresia.get().marcarUso(ahora);
        membresias.guardar(membresia.get());
        eventos.publicar(new SesionIniciada(membresia.get().despachoId(), usuario.id(), c.origen().ip(), ahora));
        return new ResultadoSesion.Emitida(emitir(usuario, membresia.get(), UUID.randomUUID(), c.origen(), ahora));
    }

    @Override
    public ResultadoSesion refrescar(String tokenRefresco, Origen origen) {
        Instant ahora = reloj.instant();
        if (tokenRefresco == null || tokenRefresco.isBlank()) {
            return SESION_NO_VALIDA;
        }
        Optional<SesionRefresco> encontrada = sesiones.buscarPorHashParaActualizar(TokensSeguros.hash(tokenRefresco));
        if (encontrada.isEmpty()) {
            return SESION_NO_VALIDA;
        }
        SesionRefresco sesion = encontrada.get();
        if (sesion.revocada() || sesion.caducada(ahora)) {
            return SESION_NO_VALIDA;
        }
        if (sesion.usada()) {
            sesiones.revocarFamilia(sesion.familiaId(), ahora);
            eventos.publicar(new ReutilizacionSesionDetectada(sesion.despachoId(), sesion.usuarioId(), origen.ip(), ahora));
            return SESION_NO_VALIDA;
        }

        Optional<Usuario> usuario = usuarios.buscar(sesion.usuarioId());
        Optional<Membresia> membresia = membresias.buscar(sesion.usuarioId(), sesion.despachoId()).filter(Membresia::activa);
        if (usuario.isEmpty() || !usuario.get().activo() || usuario.get().versionToken() != sesion.versionToken()
                || membresia.isEmpty()) {
            sesiones.revocarFamilia(sesion.familiaId(), ahora);
            return SESION_NO_VALIDA;
        }

        sesion.marcarUsada(ahora);
        sesiones.guardar(sesion);
        return new ResultadoSesion.Emitida(emitir(usuario.get(), membresia.get(), sesion.familiaId(), origen, ahora));
    }

    @Override
    public void cerrarSesion(String tokenRefresco) {
        if (tokenRefresco == null || tokenRefresco.isBlank()) {
            return;
        }
        sesiones.buscarPorHash(TokensSeguros.hash(tokenRefresco))
                .ifPresent(s -> sesiones.revocarFamilia(s.familiaId(), reloj.instant()));
    }

    @Override
    public ResultadoSesion cambiarDespacho(DespachoId destino, String tokenRefrescoActual, Origen origen) {
        Instant ahora = reloj.instant();
        Optional<Usuario> usuario = usuarios.buscar(contexto.usuarioId()).filter(Usuario::activo);
        Optional<Membresia> membresia = membresias.buscar(contexto.usuarioId(), destino).filter(Membresia::activa);
        if (usuario.isEmpty() || membresia.isEmpty()) {
            return new ResultadoSesion.Rechazada("SIN_ACCESO_DESPACHO", "No tienes acceso a ese despacho");
        }
        cerrarSesion(tokenRefrescoActual);
        membresia.get().marcarUso(ahora);
        membresias.guardar(membresia.get());
        eventos.publicar(new SesionIniciada(destino, usuario.get().id(), origen.ip(), ahora));
        return new ResultadoSesion.Emitida(emitir(usuario.get(), membresia.get(), UUID.randomUUID(), origen, ahora));
    }

    private SesionEmitida emitir(Usuario usuario, Membresia membresia, UUID familia, Origen origen, Instant ahora) {
        var acceso = emisor.emitir(new EmisorTokensAcceso.DatosToken(usuario.id(), membresia.despachoId(),
                membresia.rol(), usuario.superadmin(), usuario.versionToken()), ahora);
        String refresco = TokensSeguros.generar();
        var nueva = SesionRefresco.nueva(familia, usuario.id(), membresia.despachoId(), TokensSeguros.hash(refresco),
                usuario.versionToken(), ahora.plus(politica.duracionRefresco()), ahora, origen.ip(), origen.agente());
        sesiones.guardar(nueva);
        return new SesionEmitida(acceso.valor(), acceso.expiraEn(), refresco, nueva.expiraEn());
    }
}
