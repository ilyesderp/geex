package es.geex.identidad.adapter.out.persistence;

import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.application.port.out.CerrojoInstalacion;
import es.geex.identidad.application.port.out.MatrizPermisosRepository;
import es.geex.identidad.application.port.out.MembresiaRepository;
import es.geex.identidad.application.port.out.SesionRefrescoRepository;
import es.geex.identidad.application.port.out.TokenUnUsoRepository;
import es.geex.identidad.application.port.out.UsuarioRepository;
import es.geex.identidad.domain.MatrizPermisos;
import es.geex.identidad.domain.Membresia;
import es.geex.identidad.domain.SesionRefresco;
import es.geex.identidad.domain.TokenUnUso;
import es.geex.identidad.domain.Usuario;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Adaptador de persistencia del módulo Identidad: traduce entre entidades JPA y objetos de dominio. */
@Component
class IdentidadPersistenceAdapter implements UsuarioRepository, MembresiaRepository, SesionRefrescoRepository,
        TokenUnUsoRepository, MatrizPermisosRepository, CerrojoInstalacion {

    /** Clave arbitraria pero fija del cerrojo consultivo de PostgreSQL para la instalación. */
    private static final long CLAVE_CERROJO_INSTALACION = 4_201_100_001L;

    private final UsuarioJpaRepository usuarios;
    private final MembresiaJpaRepository membresias;
    private final SesionRefrescoJpaRepository sesiones;
    private final TokenUnUsoJpaRepository tokens;
    private final JdbcClient jdbc;
    private final TransactionTemplate transaccionAparte;

    IdentidadPersistenceAdapter(UsuarioJpaRepository usuarios, MembresiaJpaRepository membresias,
                                SesionRefrescoJpaRepository sesiones, TokenUnUsoJpaRepository tokens, JdbcClient jdbc,
                                PlatformTransactionManager gestorTransacciones) {
        this.usuarios = usuarios;
        this.membresias = membresias;
        this.sesiones = sesiones;
        this.tokens = tokens;
        this.jdbc = jdbc;
        this.transaccionAparte = new TransactionTemplate(gestorTransacciones);
        this.transaccionAparte.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transaccionAparte.setReadOnly(true);
    }

    // ── Usuarios ─────────────────────────────────────────────────────────────

    @Override
    public void guardar(Usuario u) {
        var e = usuarios.findById(u.id().valor()).orElseGet(UsuarioEntity::new);
        e.id = u.id().valor();
        e.email = u.email();
        e.nombre = u.nombre();
        e.apellidos = u.apellidos();
        e.hashContrasena = u.hashContrasena();
        e.superadmin = u.superadmin();
        e.activo = u.activo();
        e.intentosFallidos = u.intentosFallidos();
        e.bloqueadoHasta = u.bloqueadoHasta();
        e.versionToken = u.versionToken();
        e.ultimoAcceso = u.ultimoAcceso();
        e.creadoEn = u.creadoEn();
        usuarios.save(e);
    }

    @Override
    public Optional<Usuario> buscar(UsuarioId id) {
        return usuarios.findById(id.valor()).map(IdentidadPersistenceAdapter::aDominio);
    }

    /**
     * Los usuarios son de toda la plataforma: buscar por correo (inicio de sesión, invitar a alguien que ya
     * trabaja en otro despacho) tiene que verlos a todos. La conexión de la transacción en curso tiene fijado
     * el despacho y la política RLS solo dejaría ver a sus miembros, así que esta consulta concreta se hace en
     * modo sistema y en una transacción aparte (otra conexión, con su propio contexto).
     */
    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return ContextoEjecucion.comoSistema(() -> transaccionAparte.execute(estado ->
                usuarios.buscarPorEmail(email).map(IdentidadPersistenceAdapter::aDominio)));
    }

    @Override
    public List<Usuario> buscarVarios(Collection<UsuarioId> ids) {
        return usuarios.findAllById(ids.stream().map(UsuarioId::valor).toList()).stream()
                .map(IdentidadPersistenceAdapter::aDominio).toList();
    }

    private static Usuario aDominio(UsuarioEntity e) {
        return Usuario.reconstruir(new UsuarioId(e.id), e.email, e.nombre, e.apellidos, e.hashContrasena,
                e.superadmin, e.activo, e.intentosFallidos, e.bloqueadoHasta, e.versionToken, e.ultimoAcceso,
                e.creadoEn, 0);
    }

    // ── Membresías ───────────────────────────────────────────────────────────

    @Override
    public void guardar(Membresia m) {
        var e = membresias.findById(m.id()).orElseGet(MembresiaEntity::new);
        e.id = m.id();
        e.usuarioId = m.usuarioId().valor();
        e.despachoId = m.despachoId().valor();
        e.rol = m.rol();
        e.activa = m.activa();
        e.creadaEn = m.creadaEn();
        e.ultimoUso = m.ultimoUso();
        membresias.saveAndFlush(e); // la política RLS de usuarios mira las membresías: se vuelca ya
    }

    @Override
    public Optional<Membresia> buscar(UsuarioId usuario, DespachoId despacho) {
        return membresias.findByUsuarioIdAndDespachoId(usuario.valor(), despacho.valor())
                .map(IdentidadPersistenceAdapter::aDominio);
    }

    @Override
    public List<Membresia> listarPorDespacho(DespachoId despacho) {
        return membresias.findByDespachoId(despacho.valor()).stream().map(IdentidadPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Membresia> listarActivasDeUsuario(UsuarioId usuario) {
        return membresias.findByUsuarioIdAndActivaTrue(usuario.valor()).stream()
                .map(IdentidadPersistenceAdapter::aDominio).toList();
    }

    @Override
    public long contarTitularesActivos(DespachoId despacho) {
        jdbc.sql("select pg_advisory_xact_lock(hashtextextended(:clave, 0))")
                .param("clave", "titulares:" + despacho.valor())
                .query((rs, n) -> 1).single();
        return membresias.countByDespachoIdAndRolAndActivaTrue(despacho.valor(), Rol.TITULAR);
    }

    private static Membresia aDominio(MembresiaEntity e) {
        return Membresia.reconstruir(e.id, new UsuarioId(e.usuarioId), new DespachoId(e.despachoId), e.rol, e.activa,
                e.creadaEn, e.ultimoUso);
    }

    // ── Sesiones de refresco ─────────────────────────────────────────────────

    @Override
    public void guardar(SesionRefresco s) {
        var e = sesiones.findById(s.id()).orElseGet(SesionRefrescoEntity::new);
        e.id = s.id();
        e.familiaId = s.familiaId();
        e.usuarioId = s.usuarioId().valor();
        e.despachoId = s.despachoId().valor();
        e.tokenHash = s.tokenHash();
        e.versionToken = s.versionToken();
        e.expiraEn = s.expiraEn();
        e.usadoEn = s.usadoEn();
        e.revocadoEn = s.revocadoEn();
        e.creadoEn = s.creadoEn();
        e.ip = s.ip();
        e.agente = s.agente();
        sesiones.save(e);
    }

    @Override
    public Optional<SesionRefresco> buscarPorHashParaActualizar(String hash) {
        return sesiones.buscarPorHashBloqueando(hash).map(IdentidadPersistenceAdapter::aDominio);
    }

    @Override
    public Optional<SesionRefresco> buscarPorHash(String hash) {
        return sesiones.findByTokenHash(hash).map(IdentidadPersistenceAdapter::aDominio);
    }

    @Override
    public void revocarFamilia(UUID familia, Instant ahora) {
        sesiones.revocarFamilia(familia, ahora);
    }

    @Override
    public void revocarDeUsuario(UsuarioId usuario, Instant ahora) {
        sesiones.revocarDeUsuario(usuario.valor(), ahora);
    }

    @Override
    public void revocarDeUsuarioEnDespacho(UsuarioId usuario, DespachoId despacho, Instant ahora) {
        sesiones.revocarDeUsuarioEnDespacho(usuario.valor(), despacho.valor(), ahora);
    }

    private static SesionRefresco aDominio(SesionRefrescoEntity e) {
        return SesionRefresco.reconstruir(e.id, e.familiaId, new UsuarioId(e.usuarioId), new DespachoId(e.despachoId),
                e.tokenHash, e.versionToken, e.expiraEn, e.usadoEn, e.revocadoEn, e.creadoEn, e.ip, e.agente);
    }

    // ── Tokens de un solo uso ────────────────────────────────────────────────

    @Override
    public void guardar(TokenUnUso t) {
        var e = tokens.findById(t.id()).orElseGet(TokenUnUsoEntity::new);
        e.id = t.id();
        e.usuarioId = t.usuarioId().valor();
        e.despachoId = t.despachoId() == null ? null : t.despachoId().valor();
        e.tipo = t.tipo();
        e.tokenHash = t.tokenHash();
        e.expiraEn = t.expiraEn();
        e.usadoEn = t.usadoEn();
        e.creadoEn = t.creadoEn();
        tokens.save(e);
    }

    @Override
    public Optional<TokenUnUso> buscarEnlacePorHash(String hash) {
        return tokens.findByTokenHash(hash).map(IdentidadPersistenceAdapter::aDominio);
    }

    @Override
    public Optional<TokenUnUso> buscarEnlacePorHashParaUsar(String hash) {
        return tokens.buscarParaUsar(hash).map(IdentidadPersistenceAdapter::aDominio);
    }

    private static TokenUnUso aDominio(TokenUnUsoEntity e) {
        return TokenUnUso.reconstruir(e.id, new UsuarioId(e.usuarioId),
                e.despachoId == null ? null : new DespachoId(e.despachoId), e.tipo, e.tokenHash, e.expiraEn,
                e.usadoEn, e.creadoEn);
    }

    @Override
    public void invalidarPendientes(UsuarioId usuario, TokenUnUso.Tipo tipo, Instant ahora) {
        tokens.invalidarPendientes(usuario.valor(), tipo, ahora);
    }

    // ── Matriz de permisos (JDBC: es una tabla de relación sencilla) ─────────

    @Override
    public MatrizPermisos obtener(DespachoId despacho) {
        Map<Rol, Set<Permiso>> mapa = new EnumMap<>(Rol.class);
        jdbc.sql("select rol, permiso from permisos_rol where despacho_id = :d")
                .param("d", despacho.valor())
                .query((rs, n) -> Map.entry(rs.getString("rol"), rs.getString("permiso")))
                .list()
                .forEach(fila -> convertir(fila.getKey(), Rol.class).ifPresent(rol ->
                        convertir(fila.getValue(), Permiso.class).ifPresent(permiso ->
                                mapa.computeIfAbsent(rol, r -> EnumSet.noneOf(Permiso.class)).add(permiso))));
        return MatrizPermisos.reconstruir(despacho, mapa);
    }

    @Override
    public void guardar(MatrizPermisos matriz) {
        matriz.comoMapa().forEach((rol, permisos) -> guardarRol(matriz.despachoId(), rol, permisos));
    }

    @Override
    public void guardarRol(DespachoId despacho, Rol rol, Set<Permiso> permisos) {
        if (rol == Rol.TITULAR) {
            return; // el titular siempre tiene todos los permisos; no se guarda
        }
        jdbc.sql("delete from permisos_rol where despacho_id = :d and rol = :r")
                .param("d", despacho.valor()).param("r", rol.name()).update();
        for (Permiso permiso : permisos) {
            jdbc.sql("insert into permisos_rol (despacho_id, rol, permiso) values (:d, :r, :p)")
                    .param("d", despacho.valor()).param("r", rol.name()).param("p", permiso.name())
                    .update();
        }
    }

    /** Ignora valores antiguos que ya no existan en el código (p. ej. un permiso eliminado). */
    private static <E extends Enum<E>> Optional<E> convertir(String valor, Class<E> tipo) {
        try {
            return Optional.of(Enum.valueOf(tipo, valor));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // ── Cerrojo de instalación ───────────────────────────────────────────────

    @Override
    public void adquirir() {
        jdbc.sql("select pg_advisory_xact_lock(:clave)").param("clave", CLAVE_CERROJO_INSTALACION)
                .query((rs, n) -> 1).single();
    }
}
