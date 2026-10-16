package es.geex.identidad.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SesionRefrescoJpaRepository extends JpaRepository<SesionRefrescoEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SesionRefrescoEntity s where s.tokenHash = :hash")
    Optional<SesionRefrescoEntity> buscarPorHashBloqueando(@Param("hash") String hash);

    Optional<SesionRefrescoEntity> findByTokenHash(String hash);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update SesionRefrescoEntity s set s.revocadoEn = :ahora where s.familiaId = :familia and s.revocadoEn is null")
    int revocarFamilia(@Param("familia") UUID familia, @Param("ahora") Instant ahora);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update SesionRefrescoEntity s set s.revocadoEn = :ahora where s.usuarioId = :usuario and s.revocadoEn is null")
    int revocarDeUsuario(@Param("usuario") UUID usuario, @Param("ahora") Instant ahora);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update SesionRefrescoEntity s set s.revocadoEn = :ahora
            where s.usuarioId = :usuario and s.despachoId = :despacho and s.revocadoEn is null""")
    int revocarDeUsuarioEnDespacho(@Param("usuario") UUID usuario, @Param("despacho") UUID despacho,
                                   @Param("ahora") Instant ahora);
}
