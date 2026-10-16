package es.geex.identidad.application.service;

import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.application.PublicadorEventos;
import es.geex.compartido.domain.AccesoDenegadoException;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.NoEncontradoException;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.ReglaNegocioException;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.application.port.in.GestionUsuariosUseCase;
import es.geex.identidad.application.port.out.DespachosPort;
import es.geex.identidad.application.port.out.MembresiaRepository;
import es.geex.identidad.application.port.out.NotificadorCuenta;
import es.geex.identidad.application.port.out.SesionRefrescoRepository;
import es.geex.identidad.application.port.out.TokenUnUsoRepository;
import es.geex.identidad.application.port.out.UsuarioRepository;
import es.geex.identidad.domain.Membresia;
import es.geex.identidad.domain.TokenUnUso;
import es.geex.identidad.domain.Usuario;
import es.geex.identidad.domain.evento.EventosIdentidad.AccesoDesactivado;
import es.geex.identidad.domain.evento.EventosIdentidad.AccesoReactivado;
import es.geex.identidad.domain.evento.EventosIdentidad.RolCambiado;
import es.geex.identidad.domain.evento.EventosIdentidad.UsuarioInvitado;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GestionUsuariosService implements GestionUsuariosUseCase {

    private final UsuarioRepository usuarios;
    private final MembresiaRepository membresias;
    private final TokenUnUsoRepository tokens;
    private final SesionRefrescoRepository sesiones;
    private final NotificadorCuenta notificador;
    private final DespachosPort despachos;
    private final ContextoUsuario contexto;
    private final PublicadorEventos eventos;
    private final PoliticaSesiones politica;
    private final Clock reloj;

    public GestionUsuariosService(UsuarioRepository usuarios, MembresiaRepository membresias,
                                  TokenUnUsoRepository tokens, SesionRefrescoRepository sesiones,
                                  NotificadorCuenta notificador, DespachosPort despachos, ContextoUsuario contexto,
                                  PublicadorEventos eventos, PoliticaSesiones politica, Clock reloj) {
        this.usuarios = usuarios;
        this.membresias = membresias;
        this.tokens = tokens;
        this.sesiones = sesiones;
        this.notificador = notificador;
        this.despachos = despachos;
        this.contexto = contexto;
        this.eventos = eventos;
        this.politica = politica;
        this.reloj = reloj;
    }

    @Override
    public List<UsuarioDespacho> listarUsuarios() {
        contexto.exigir(Permiso.USUARIOS_GESTIONAR);
        List<Membresia> lista = membresias.listarPorDespacho(contexto.despachoId());
        Map<UsuarioId, Usuario> porId = usuarios.buscarVarios(lista.stream().map(Membresia::usuarioId).toList())
                .stream().collect(Collectors.toMap(Usuario::id, Function.identity()));
        return lista.stream()
                .filter(m -> porId.containsKey(m.usuarioId()))
                .map(m -> aVista(porId.get(m.usuarioId()), m))
                .sorted(Comparator.comparing(UsuarioDespacho::nombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(UsuarioDespacho::apellidos, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public UsuarioDespacho invitar(Invitacion inv) {
        contexto.exigir(Permiso.USUARIOS_GESTIONAR);
        Objects.requireNonNull(inv.rol(), "El rol es obligatorio");
        if (inv.rol() == Rol.TITULAR) {
            exigirSerTitular("nombrar a otro titular");
        }
        Instant ahora = reloj.instant();
        DespachoId despacho = contexto.despachoId();
        String email = Usuario.normalizarEmail(inv.email());
        String nombreDespacho = despachos.nombres(Set.of(despacho)).getOrDefault(despacho, "");

        Usuario usuario = usuarios.buscarPorEmail(email).orElse(null);
        if (usuario != null && membresias.buscar(usuario.id(), despacho).isPresent()) {
            throw new ReglaNegocioException("YA_ES_MIEMBRO", "Ese usuario ya pertenece a este despacho");
        }
        if (usuario == null) {
            usuario = Usuario.invitado(email, inv.nombre(), inv.apellidos(), ahora);
            usuarios.guardar(usuario);
        }
        Membresia membresia = Membresia.nueva(usuario.id(), despacho, inv.rol(), ahora);
        membresias.guardar(membresia);

        if (usuario.pendienteActivacion()) {
            enviarInvitacion(usuario, despacho, nombreDespacho, ahora);
        } else {
            notificador.enviarAltaEnDespacho(usuario.email(), usuario.nombre(), nombreDespacho);
        }
        eventos.publicar(new UsuarioInvitado(despacho, contexto.usuarioId(), usuario.id(), email, inv.rol(), ahora));
        return aVista(usuario, membresia);
    }

    @Override
    public void reenviarInvitacion(UsuarioId id) {
        contexto.exigir(Permiso.USUARIOS_GESTIONAR);
        cargarMembresia(id);
        Usuario usuario = cargarUsuario(id);
        if (!usuario.pendienteActivacion()) {
            throw new ReglaNegocioException("YA_ACTIVADO", "Este usuario ya ha activado su cuenta");
        }
        DespachoId despacho = contexto.despachoId();
        enviarInvitacion(usuario, despacho, despachos.nombres(Set.of(despacho)).getOrDefault(despacho, ""),
                reloj.instant());
    }

    @Override
    public UsuarioDespacho cambiarRol(UsuarioId id, Rol nuevo) {
        contexto.exigir(Permiso.USUARIOS_GESTIONAR);
        Objects.requireNonNull(nuevo, "El rol es obligatorio");
        if (id.equals(contexto.usuarioId())) {
            throw new ReglaNegocioException("NO_AUTOCAMBIO_ROL", "No puedes cambiar tu propio rol. Pídeselo a un titular.");
        }
        Membresia membresia = cargarMembresia(id);
        Rol anterior = membresia.rol();
        if (anterior == nuevo) {
            return aVista(cargarUsuario(id), membresia);
        }
        if (anterior == Rol.TITULAR || nuevo == Rol.TITULAR) {
            exigirSerTitular("nombrar o retirar titulares");
        }
        if (anterior == Rol.TITULAR && membresia.activa()) {
            exigirOtroTitular();
        }
        Instant ahora = reloj.instant();
        membresia.cambiarRol(nuevo);
        membresias.guardar(membresia);
        // El rol viaja en el token: forzamos que se renueve con el rol nuevo
        sesiones.revocarDeUsuarioEnDespacho(id, contexto.despachoId(), ahora);
        eventos.publicar(new RolCambiado(contexto.despachoId(), contexto.usuarioId(), id, anterior, nuevo, ahora));
        return aVista(cargarUsuario(id), membresia);
    }

    @Override
    public void desactivar(UsuarioId id) {
        contexto.exigir(Permiso.USUARIOS_GESTIONAR);
        if (id.equals(contexto.usuarioId())) {
            throw new ReglaNegocioException("NO_AUTODESACTIVAR", "No puedes desactivar tu propio acceso");
        }
        Membresia membresia = cargarMembresia(id);
        if (!membresia.activa()) {
            return;
        }
        if (membresia.rol() == Rol.TITULAR) {
            exigirSerTitular("desactivar a un titular");
            exigirOtroTitular();
        }
        Instant ahora = reloj.instant();
        membresia.desactivar();
        membresias.guardar(membresia);
        sesiones.revocarDeUsuarioEnDespacho(id, contexto.despachoId(), ahora);
        eventos.publicar(new AccesoDesactivado(contexto.despachoId(), contexto.usuarioId(), id, ahora));
    }

    @Override
    public void reactivar(UsuarioId id) {
        contexto.exigir(Permiso.USUARIOS_GESTIONAR);
        Membresia membresia = cargarMembresia(id);
        if (membresia.activa()) {
            return;
        }
        if (membresia.rol() == Rol.TITULAR) {
            exigirSerTitular("reactivar a un titular");
        }
        membresia.reactivar();
        membresias.guardar(membresia);
        eventos.publicar(new AccesoReactivado(contexto.despachoId(), contexto.usuarioId(), id, reloj.instant()));
    }

    private void enviarInvitacion(Usuario usuario, DespachoId despacho, String nombreDespacho, Instant ahora) {
        tokens.invalidarPendientes(usuario.id(), TokenUnUso.Tipo.INVITACION, ahora);
        String token = TokensSeguros.generar();
        Instant expira = ahora.plus(politica.duracionInvitacion());
        tokens.guardar(TokenUnUso.nuevo(usuario.id(), despacho, TokenUnUso.Tipo.INVITACION,
                TokensSeguros.hash(token), expira, ahora));
        notificador.enviarInvitacion(usuario.email(), usuario.nombre(), nombreDespacho, token, expira);
    }

    /**
     * El permiso de gestionar usuarios se puede dar a otros roles, pero lo que
     * afecta a titulares (nombrarlos, retirarlos, desactivarlos) queda reservado a
     * un titular: si no, cualquiera con ese permiso podría hacerse con el despacho.
     */
    private void exigirSerTitular(String accion) {
        if (contexto.rol() != Rol.TITULAR) {
            throw new AccesoDenegadoException("SOLO_TITULAR", "Solo un titular puede " + accion);
        }
    }

    /** Un despacho nunca puede quedarse sin un titular activo. */
    private void exigirOtroTitular() {
        if (membresias.contarTitularesActivos(contexto.despachoId()) <= 1) {
            throw new ReglaNegocioException("ULTIMO_TITULAR",
                    "El despacho debe tener al menos un titular activo. Nombra otro titular antes.");
        }
    }

    private Membresia cargarMembresia(UsuarioId id) {
        return membresias.buscar(id, contexto.despachoId())
                .orElseThrow(() -> new NoEncontradoException("USUARIO_NO_ENCONTRADO", "Usuario no encontrado en este despacho"));
    }

    private Usuario cargarUsuario(UsuarioId id) {
        return usuarios.buscar(id)
                .orElseThrow(() -> new NoEncontradoException("USUARIO_NO_ENCONTRADO", "Usuario no encontrado"));
    }

    private UsuarioDespacho aVista(Usuario u, Membresia m) {
        EstadoUsuario estado = !m.activa() || !u.activo() ? EstadoUsuario.DESACTIVADO
                : u.pendienteActivacion() ? EstadoUsuario.PENDIENTE_ACTIVACION : EstadoUsuario.ACTIVO;
        return new UsuarioDespacho(u.id().toString(), u.email(), u.nombre(), u.apellidos(), m.rol(),
                m.rol().etiqueta(), estado, u.ultimoAcceso(), u.id().equals(contexto.usuarioId()));
    }
}
