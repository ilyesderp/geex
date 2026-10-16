package es.geex.identidad.adapter.out.persistence;

import es.geex.compartido.domain.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "membresias")
class MembresiaEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    UUID usuarioId;

    @Column(nullable = false)
    UUID despachoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Rol rol;

    boolean activa;

    @Column(nullable = false)
    Instant creadaEn;

    Instant ultimoUso;
}
