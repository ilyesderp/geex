package es.geex.despacho.adapter.out.persistence;

import es.geex.compartido.domain.ComunidadAutonoma;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "despachos")
class DespachoEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    String nombre;

    String razonSocial;
    String nif;
    String direccion;
    String codigoPostal;

    @Column(nullable = false)
    String municipio;

    @Column(nullable = false)
    String provincia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ComunidadAutonoma comunidadAutonoma;

    String telefono;
    String email;
    String web;

    @Column(nullable = false)
    boolean activo = true;

    @Column(nullable = false)
    Instant creadoEn;

    @Column(nullable = false)
    Instant actualizadoEn;

    @Version
    long version;
}
