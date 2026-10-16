package es.geex.identidad.domain;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.NoEncontradoException;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Enlace de un solo uso enviado por correo (invitación o restablecimiento de contraseña). */
public class TokenUnUso {

    public enum Tipo { INVITACION, RESTABLECIMIENTO }

    private final UUID id;
    private final UsuarioId usuarioId;
    private final DespachoId despachoId;
    private final Tipo tipo;
    private final String tokenHash;
    private final Instant expiraEn;
    private Instant usadoEn;
    private final Instant creadoEn;

    private TokenUnUso(UUID id, UsuarioId usuarioId, DespachoId despachoId, Tipo tipo, String tokenHash,
                       Instant expiraEn, Instant usadoEn, Instant creadoEn) {
        this.id = Objects.requireNonNull(id);
        this.usuarioId = Objects.requireNonNull(usuarioId);
        this.despachoId = despachoId;
        this.tipo = Objects.requireNonNull(tipo);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.expiraEn = Objects.requireNonNull(expiraEn);
        this.usadoEn = usadoEn;
        this.creadoEn = creadoEn;
    }

    public static TokenUnUso nuevo(UsuarioId usuario, DespachoId despacho, Tipo tipo, String tokenHash,
                                   Instant expiraEn, Instant ahora) {
        return new TokenUnUso(UUID.randomUUID(), usuario, despacho, tipo, tokenHash, expiraEn, null, ahora);
    }

    public static TokenUnUso reconstruir(UUID id, UsuarioId usuario, DespachoId despacho, Tipo tipo,
                                         String tokenHash, Instant expiraEn, Instant usadoEn, Instant creadoEn) {
        return new TokenUnUso(id, usuario, despacho, tipo, tokenHash, expiraEn, usadoEn, creadoEn);
    }

    /** Comprueba que el enlace es del tipo esperado, no se ha usado y no ha caducado. */
    public void validar(Tipo esperado, Instant ahora) {
        if (tipo != esperado || usadoEn != null || !ahora.isBefore(expiraEn)) {
            throw enlaceNoValido();
        }
    }

    public void usar(Instant ahora) {
        this.usadoEn = ahora;
    }

    public static NoEncontradoException enlaceNoValido() {
        return new NoEncontradoException("ENLACE_NO_VALIDO",
                "El enlace no es válido o ha caducado. Solicita uno nuevo.");
    }

    public UUID id() { return id; }
    public UsuarioId usuarioId() { return usuarioId; }
    public DespachoId despachoId() { return despachoId; }
    public Tipo tipo() { return tipo; }
    public String tokenHash() { return tokenHash; }
    public Instant expiraEn() { return expiraEn; }
    public Instant usadoEn() { return usadoEn; }
    public Instant creadoEn() { return creadoEn; }
}
