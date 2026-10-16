package es.geex.identidad.domain;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Token de refresco (solo se guarda su hash). Rotación: cada uso lo invalida y
 * crea otro de la misma familia. Si un token ya usado se vuelve a presentar,
 * alguien lo ha copiado y se revoca toda la familia.
 */
public class SesionRefresco {

    private final UUID id;
    private final UUID familiaId;
    private final UsuarioId usuarioId;
    private final DespachoId despachoId;
    private final String tokenHash;
    private final int versionToken;
    private final Instant expiraEn;
    private Instant usadoEn;
    private Instant revocadoEn;
    private final Instant creadoEn;
    private final String ip;
    private final String agente;

    private SesionRefresco(UUID id, UUID familiaId, UsuarioId usuarioId, DespachoId despachoId, String tokenHash,
                           int versionToken, Instant expiraEn, Instant usadoEn, Instant revocadoEn,
                           Instant creadoEn, String ip, String agente) {
        this.id = Objects.requireNonNull(id);
        this.familiaId = Objects.requireNonNull(familiaId);
        this.usuarioId = Objects.requireNonNull(usuarioId);
        this.despachoId = Objects.requireNonNull(despachoId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.versionToken = versionToken;
        this.expiraEn = Objects.requireNonNull(expiraEn);
        this.usadoEn = usadoEn;
        this.revocadoEn = revocadoEn;
        this.creadoEn = creadoEn;
        this.ip = ip;
        this.agente = agente;
    }

    public static SesionRefresco nueva(UUID familiaId, UsuarioId usuario, DespachoId despacho, String tokenHash,
                                       int versionToken, Instant expiraEn, Instant ahora, String ip, String agente) {
        return new SesionRefresco(UUID.randomUUID(), familiaId, usuario, despacho, tokenHash, versionToken,
                expiraEn, null, null, ahora, recortar(ip, 64), recortar(agente, 300));
    }

    public static SesionRefresco reconstruir(UUID id, UUID familiaId, UsuarioId usuario, DespachoId despacho,
                                             String tokenHash, int versionToken, Instant expiraEn, Instant usadoEn,
                                             Instant revocadoEn, Instant creadoEn, String ip, String agente) {
        return new SesionRefresco(id, familiaId, usuario, despacho, tokenHash, versionToken, expiraEn, usadoEn,
                revocadoEn, creadoEn, ip, agente);
    }

    public boolean revocada() {
        return revocadoEn != null;
    }

    public boolean usada() {
        return usadoEn != null;
    }

    public boolean caducada(Instant ahora) {
        return !ahora.isBefore(expiraEn);
    }

    public void marcarUsada(Instant ahora) {
        this.usadoEn = ahora;
    }

    private static String recortar(String valor, int maximo) {
        return valor == null || valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }

    public UUID id() { return id; }
    public UUID familiaId() { return familiaId; }
    public UsuarioId usuarioId() { return usuarioId; }
    public DespachoId despachoId() { return despachoId; }
    public String tokenHash() { return tokenHash; }
    public int versionToken() { return versionToken; }
    public Instant expiraEn() { return expiraEn; }
    public Instant usadoEn() { return usadoEn; }
    public Instant revocadoEn() { return revocadoEn; }
    public Instant creadoEn() { return creadoEn; }
    public String ip() { return ip; }
    public String agente() { return agente; }
}
