package es.geex.identidad.adapter.out.persistence;

import es.geex.identidad.domain.TokenUnUso;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TokenUnUsoJpaRepository extends JpaRepository<TokenUnUsoEntity, UUID> {

    Optional<TokenUnUsoEntity> findByTokenHash(String hash);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update TokenUnUsoEntity t set t.usadoEn = :ahora
            where t.usuarioId = :usuario and t.tipo = :tipo and t.usadoEn is null""")
    int invalidarPendientes(@Param("usuario") UUID usuario, @Param("tipo") TokenUnUso.Tipo tipo,
                            @Param("ahora") Instant ahora);
}
