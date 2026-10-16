package es.geex.identidad.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sesiones_refresco")
class SesionRefrescoEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    UUID familiaId;

    @Column(nullable = false)
    UUID usuarioId;

    @Column(nullable = false)
    UUID despachoId;

    @Column(nullable = false)
    String tokenHash;

    int versionToken;

    @Column(nullable = false)
    Instant expiraEn;

    Instant usadoEn;
    Instant revocadoEn;

    @Column(nullable = false)
    Instant creadoEn;

    String ip;
    String agente;
}
