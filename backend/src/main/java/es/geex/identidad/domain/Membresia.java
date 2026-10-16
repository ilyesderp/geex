package es.geex.identidad.domain;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Acceso de un usuario a un despacho, con su rol en ese despacho. */
public class Membresia {

    private final UUID id;
    private final UsuarioId usuarioId;
    private final DespachoId despachoId;
    private Rol rol;
    private boolean activa;
    private final Instant creadaEn;
    private Instant ultimoUso;
    private final long version;

    private Membresia(UUID id, UsuarioId usuarioId, DespachoId despachoId, Rol rol, boolean activa,
                      Instant creadaEn, Instant ultimoUso, long version) {
        this.id = Objects.requireNonNull(id);
        this.usuarioId = Objects.requireNonNull(usuarioId);
        this.despachoId = Objects.requireNonNull(despachoId);
        this.rol = Objects.requireNonNull(rol);
        this.activa = activa;
        this.creadaEn = creadaEn;
        this.ultimoUso = ultimoUso;
        this.version = version;
    }

    public static Membresia nueva(UsuarioId usuario, DespachoId despacho, Rol rol, Instant ahora) {
        return new Membresia(UUID.randomUUID(), usuario, despacho, rol, true, ahora, null, 0);
    }

    public static Membresia reconstruir(UUID id, UsuarioId usuario, DespachoId despacho, Rol rol, boolean activa,
                                        Instant creadaEn, Instant ultimoUso, long version) {
        return new Membresia(id, usuario, despacho, rol, activa, creadaEn, ultimoUso, version);
    }

    public void cambiarRol(Rol nuevo) {
        this.rol = Objects.requireNonNull(nuevo);
    }

    public void desactivar() {
        this.activa = false;
    }

    public void reactivar() {
        this.activa = true;
    }

    public void marcarUso(Instant ahora) {
        this.ultimoUso = ahora;
    }

    public UUID id() { return id; }
    public UsuarioId usuarioId() { return usuarioId; }
    public DespachoId despachoId() { return despachoId; }
    public Rol rol() { return rol; }
    public boolean activa() { return activa; }
    public Instant creadaEn() { return creadaEn; }
    public Instant ultimoUso() { return ultimoUso; }
    public long version() { return version; }
}
