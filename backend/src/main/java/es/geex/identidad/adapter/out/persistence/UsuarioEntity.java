package es.geex.identidad.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
class UsuarioEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    String email;

    @Column(nullable = false)
    String nombre;

    @Column(nullable = false)
    String apellidos;

    String hashContrasena;
    boolean superadmin;
    boolean activo;
    int intentosFallidos;
    Instant bloqueadoHasta;
    int versionToken;
    Instant ultimoAcceso;

    @Column(nullable = false)
    Instant creadoEn;
}
