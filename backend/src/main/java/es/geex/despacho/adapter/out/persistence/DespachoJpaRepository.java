package es.geex.despacho.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DespachoJpaRepository extends JpaRepository<DespachoEntity, UUID> {
}
