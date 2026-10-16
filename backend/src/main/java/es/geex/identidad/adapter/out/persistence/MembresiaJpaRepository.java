package es.geex.identidad.adapter.out.persistence;

import es.geex.compartido.domain.Rol;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface MembresiaJpaRepository extends JpaRepository<MembresiaEntity, UUID> {

    Optional<MembresiaEntity> findByUsuarioIdAndDespachoId(UUID usuarioId, UUID despachoId);

    List<MembresiaEntity> findByDespachoId(UUID despachoId);

    List<MembresiaEntity> findByUsuarioIdAndActivaTrue(UUID usuarioId);

    long countByDespachoIdAndRolAndActivaTrue(UUID despachoId, Rol rol);
}
