package es.geex.auditoria.adapter.out.persistence;

import es.geex.auditoria.application.port.out.RegistroAuditoriaRepository;
import es.geex.auditoria.domain.RegistroAuditoria;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.UsuarioId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class RegistroAuditoriaJdbcAdapter implements RegistroAuditoriaRepository {

    private static final String FILTRO = """
            where despacho_id = :d
              and (cast(:accion as varchar) is null or accion = :accion)
              and (cast(:desde as timestamptz) is null or ocurrido_en >= :desde)
              and (cast(:hasta as timestamptz) is null or ocurrido_en < :hasta)""";

    private final NamedParameterJdbcTemplate jdbc;

    RegistroAuditoriaJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(RegistroAuditoria r) {
        jdbc.update("""
                        insert into registros_auditoria
                            (id, despacho_id, actor_id, accion, descripcion, entidad_tipo, entidad_id, ocurrido_en)
                        values (:id, :d, :actor, :accion, :descripcion, :tipo, :entidad, :ocurrido)""",
                new MapSqlParameterSource()
                        .addValue("id", r.id())
                        .addValue("d", r.despachoId() == null ? null : r.despachoId().valor())
                        .addValue("actor", r.actorId() == null ? null : r.actorId().valor())
                        .addValue("accion", r.accion())
                        .addValue("descripcion", r.descripcion())
                        .addValue("tipo", r.entidadTipo())
                        .addValue("entidad", r.entidadId())
                        .addValue("ocurrido", Timestamp.from(r.ocurridoEn())));
    }

    @Override
    public List<RegistroAuditoria> buscar(DespachoId despacho, String accion, Instant desde, Instant hasta,
                                          int desplazamiento, int limite) {
        return jdbc.query("select * from registros_auditoria " + FILTRO
                        + " order by ocurrido_en desc limit :limite offset :desplazamiento",
                parametros(despacho, accion, desde, hasta)
                        .addValue("limite", limite)
                        .addValue("desplazamiento", desplazamiento),
                RegistroAuditoriaJdbcAdapter::mapear);
    }

    @Override
    public long contar(DespachoId despacho, String accion, Instant desde, Instant hasta) {
        Long total = jdbc.queryForObject("select count(*) from registros_auditoria " + FILTRO,
                parametros(despacho, accion, desde, hasta), Long.class);
        return total == null ? 0 : total;
    }

    private static MapSqlParameterSource parametros(DespachoId despacho, String accion, Instant desde, Instant hasta) {
        return new MapSqlParameterSource()
                .addValue("d", despacho.valor())
                .addValue("accion", accion)
                .addValue("desde", desde == null ? null : Timestamp.from(desde))
                .addValue("hasta", hasta == null ? null : Timestamp.from(hasta));
    }

    private static RegistroAuditoria mapear(ResultSet rs, int fila) throws SQLException {
        UUID despacho = rs.getObject("despacho_id", UUID.class);
        UUID actor = rs.getObject("actor_id", UUID.class);
        return new RegistroAuditoria(
                rs.getObject("id", UUID.class),
                despacho == null ? null : new DespachoId(despacho),
                actor == null ? null : new UsuarioId(actor),
                rs.getString("accion"),
                rs.getString("descripcion"),
                rs.getString("entidad_tipo"),
                rs.getString("entidad_id"),
                rs.getTimestamp("ocurrido_en").toInstant());
    }
}
