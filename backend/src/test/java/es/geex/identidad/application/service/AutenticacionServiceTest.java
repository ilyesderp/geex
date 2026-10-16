package es.geex.identidad.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.application.port.in.AutenticacionUseCase.Credenciales;
import es.geex.identidad.application.port.in.AutenticacionUseCase.Origen;
import es.geex.identidad.application.port.in.AutenticacionUseCase.ResultadoSesion;
import es.geex.identidad.application.port.out.CifradorContrasenas;
import es.geex.identidad.application.port.out.EmisorTokensAcceso;
import es.geex.identidad.application.port.out.MembresiaRepository;
import es.geex.identidad.application.port.out.SesionRefrescoRepository;
import es.geex.identidad.application.port.out.UsuarioRepository;
import es.geex.identidad.domain.Membresia;
import es.geex.identidad.domain.SesionRefresco;
import es.geex.identidad.domain.Usuario;
import es.geex.identidad.domain.evento.EventosIdentidad;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prueba del caso de uso con dobles en memoria de todos sus puertos: sin Spring,
 * sin base de datos. Es lo que permite la arquitectura hexagonal.
 */
class AutenticacionServiceTest {

    private static final Instant AHORA = Instant.parse("2026-10-16T09:00:00Z");
    private static final Origen ORIGEN = new Origen("127.0.0.1", "test");
    private static final String CONTRASENA = "una contraseña larga";

    private final Usuarios usuarios = new Usuarios();
    private final Membresias membresias = new Membresias();
    private final Sesiones sesiones = new Sesiones();
    private final List<Object> eventos = new ArrayList<>();
    private AutenticacionService servicio;
    private Usuario ana;

    @BeforeEach
    void preparar() {
        servicio = new AutenticacionService(usuarios, membresias, sesiones, new CifradorFalso(), new EmisorFalso(),
                new SinContexto(), eventos::add,
                new PoliticaSesiones(Duration.ofHours(8), Duration.ofHours(48), Duration.ofHours(1)),
                Clock.fixed(AHORA, ZoneOffset.UTC));
        ana = Usuario.conContrasena("ana@despacho.es", "Ana", "García", "h:" + CONTRASENA, false, AHORA);
        usuarios.guardar(ana);
        membresias.guardar(Membresia.nueva(ana.id(), DespachoId.nuevo(), Rol.TITULAR, AHORA));
    }

    @Test
    void inicia_sesion_y_emite_token_de_acceso_y_de_refresco() {
        var resultado = servicio.iniciarSesion(new Credenciales("ANA@despacho.es", CONTRASENA, ORIGEN));

        assertThat(resultado).isInstanceOf(ResultadoSesion.Emitida.class);
        var sesion = ((ResultadoSesion.Emitida) resultado).sesion();
        assertThat(sesion.tokenAcceso()).startsWith("jwt:");
        assertThat(sesion.tokenRefresco()).isNotBlank();
        assertThat(sesiones.porHash).containsKey(TokensSeguros.hash(sesion.tokenRefresco()));
        assertThat(eventos).hasAtLeastOneElementOfType(EventosIdentidad.SesionIniciada.class);
    }

    @Test
    void con_contrasena_incorrecta_rechaza_y_cuenta_el_intento() {
        var resultado = servicio.iniciarSesion(new Credenciales("ana@despacho.es", "incorrecta", ORIGEN));

        assertThat(resultado).isEqualTo(new ResultadoSesion.Rechazada("CREDENCIALES_INVALIDAS",
                "Correo electrónico o contraseña incorrectos"));
        assertThat(usuarios.buscar(ana.id()).orElseThrow().intentosFallidos()).isEqualTo(1);
    }

    @Test
    void un_correo_desconocido_da_el_mismo_error_que_una_contrasena_incorrecta() {
        var desconocido = servicio.iniciarSesion(new Credenciales("nadie@despacho.es", CONTRASENA, ORIGEN));
        var incorrecta = servicio.iniciarSesion(new Credenciales("ana@despacho.es", "incorrecta", ORIGEN));

        assertThat(desconocido).isEqualTo(incorrecta);
    }

    @Test
    void al_renovar_rota_el_token_y_reutilizar_el_antiguo_revoca_toda_la_familia() {
        var primera = ((ResultadoSesion.Emitida) servicio.iniciarSesion(
                new Credenciales("ana@despacho.es", CONTRASENA, ORIGEN))).sesion();

        var renovada = servicio.refrescar(primera.tokenRefresco(), ORIGEN);
        assertThat(renovada).isInstanceOf(ResultadoSesion.Emitida.class);
        String tokenNuevo = ((ResultadoSesion.Emitida) renovada).sesion().tokenRefresco();

        // Alguien reutiliza el token antiguo (robado): se rechaza y se revoca todo
        assertThat(servicio.refrescar(primera.tokenRefresco(), ORIGEN)).isInstanceOf(ResultadoSesion.Rechazada.class);
        assertThat(servicio.refrescar(tokenNuevo, ORIGEN)).isInstanceOf(ResultadoSesion.Rechazada.class);
        assertThat(eventos).hasAtLeastOneElementOfType(EventosIdentidad.ReutilizacionSesionDetectada.class);
    }

    @Test
    void tras_cambiar_la_contrasena_las_sesiones_anteriores_no_se_renuevan() {
        var sesion = ((ResultadoSesion.Emitida) servicio.iniciarSesion(
                new Credenciales("ana@despacho.es", CONTRASENA, ORIGEN))).sesion();

        Usuario usuario = usuarios.buscar(ana.id()).orElseThrow();
        usuario.establecerContrasena("h:otra contraseña larga");
        usuarios.guardar(usuario);

        assertThat(servicio.refrescar(sesion.tokenRefresco(), ORIGEN)).isInstanceOf(ResultadoSesion.Rechazada.class);
    }

    // ── Dobles en memoria de los puertos de salida ──────────────────────────

    private static final class Usuarios implements UsuarioRepository {
        final Map<UsuarioId, Usuario> datos = new HashMap<>();

        @Override public void guardar(Usuario u) { datos.put(u.id(), u); }
        @Override public Optional<Usuario> buscar(UsuarioId id) { return Optional.ofNullable(datos.get(id)); }
        @Override public Optional<Usuario> buscarPorEmail(String email) {
            return datos.values().stream().filter(u -> u.email().equals(email)).findFirst();
        }
        @Override public List<Usuario> buscarVarios(Collection<UsuarioId> ids) { return ids.stream().map(datos::get).toList(); }
    }

    private static final class Membresias implements MembresiaRepository {
        final List<Membresia> datos = new ArrayList<>();

        @Override public void guardar(Membresia m) { datos.removeIf(x -> x.id().equals(m.id())); datos.add(m); }
        @Override public Optional<Membresia> buscar(UsuarioId u, DespachoId d) {
            return datos.stream().filter(m -> m.usuarioId().equals(u) && m.despachoId().equals(d)).findFirst();
        }
        @Override public List<Membresia> listarPorDespacho(DespachoId d) {
            return datos.stream().filter(m -> m.despachoId().equals(d)).toList();
        }
        @Override public List<Membresia> listarActivasDeUsuario(UsuarioId u) {
            return datos.stream().filter(m -> m.usuarioId().equals(u) && m.activa()).toList();
        }
        @Override public long contarTitularesActivos(DespachoId d) {
            return listarPorDespacho(d).stream().filter(m -> m.rol() == Rol.TITULAR && m.activa()).count();
        }
    }

    private static final class Sesiones implements SesionRefrescoRepository {
        final Map<String, SesionRefresco> porHash = new HashMap<>();
        final Map<UUID, Instant> revocadas = new HashMap<>();

        @Override public void guardar(SesionRefresco s) { porHash.put(s.tokenHash(), s); }
        @Override public Optional<SesionRefresco> buscarPorHashParaActualizar(String hash) { return buscarPorHash(hash); }
        @Override public Optional<SesionRefresco> buscarPorHash(String hash) {
            return Optional.ofNullable(porHash.get(hash)).map(s -> revocadas.containsKey(s.familiaId())
                    ? SesionRefresco.reconstruir(s.id(), s.familiaId(), s.usuarioId(), s.despachoId(), s.tokenHash(),
                    s.versionToken(), s.expiraEn(), s.usadoEn(), revocadas.get(s.familiaId()), s.creadoEn(), s.ip(),
                    s.agente())
                    : s);
        }
        @Override public void revocarFamilia(UUID familia, Instant ahora) { revocadas.put(familia, ahora); }
        @Override public void revocarDeUsuario(UsuarioId u, Instant ahora) {
            porHash.values().stream().filter(s -> s.usuarioId().equals(u)).forEach(s -> revocadas.put(s.familiaId(), ahora));
        }
        @Override public void revocarDeUsuarioEnDespacho(UsuarioId u, DespachoId d, Instant ahora) { revocarDeUsuario(u, ahora); }
    }

    private static final class CifradorFalso implements CifradorContrasenas {
        @Override public String cifrar(String c) { return "h:" + c; }
        @Override public boolean coincide(String c, String hash) { return hash != null && hash.equals("h:" + c); }
    }

    private static final class EmisorFalso implements EmisorTokensAcceso {
        @Override public TokenAcceso emitir(DatosToken d, Instant ahora) {
            return new TokenAcceso("jwt:" + d.usuario() + ":" + d.rol(), ahora.plus(Duration.ofMinutes(15)));
        }
    }

    private static final class SinContexto implements ContextoUsuario {
        @Override public UsuarioId usuarioId() { throw new UnsupportedOperationException(); }
        @Override public DespachoId despachoId() { throw new UnsupportedOperationException(); }
        @Override public Rol rol() { throw new UnsupportedOperationException(); }
        @Override public boolean esSuperadmin() { return false; }
        @Override public boolean tienePermiso(Permiso permiso) { return false; }
    }
}
