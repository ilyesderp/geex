package es.geex.identidad.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    @Query("select u from UsuarioEntity u where lower(u.email) = lower(:email)")
    Optional<UsuarioEntity> buscarPorEmail(@Param("email") String email);
}
