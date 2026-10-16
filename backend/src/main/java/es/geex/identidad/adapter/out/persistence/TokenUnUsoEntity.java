package es.geex.identidad.adapter.out.persistence;

import es.geex.identidad.domain.TokenUnUso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tokens_un_uso")
class TokenUnUsoEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    UUID usuarioId;

    UUID despachoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TokenUnUso.Tipo tipo;

    @Column(nullable = false)
    String tokenHash;

    @Column(nullable = false)
    Instant expiraEn;

    Instant usadoEn;

    @Column(nullable = false)
    Instant creadoEn;
}
