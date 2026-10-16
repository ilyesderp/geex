package es.geex.calendario.adapter.out.persistence;

import es.geex.calendario.application.port.out.DiaInhabilRepository;
import es.geex.calendario.domain.AmbitoDia;
import es.geex.calendario.domain.DiaInhabil;
import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
class DiaInhabilJdbcAdapter implements DiaInhabilRepository {

    private static final String COLUMNAS = "id, despacho_id, ambito, comunidad_autonoma, fecha, descripcion, provisional";

    private final JdbcClient jdbc;

    DiaInhabilJdbcAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<DiaInhabil> listar(DespachoId despacho, ComunidadAutonoma comunidad, LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                        select %s from dias_inhabiles
                        where fecha between :desde and :hasta
                          and (ambito = 'NACIONAL'
                               or (ambito = 'AUTONOMICO' and comunidad_autonoma = :comunidad)
                               or (ambito = 'LOCAL' and despacho_id = :despacho))
                        order by fecha""".formatted(COLUMNAS))
                .param("desde", desde)
                .param("hasta", hasta)
                .param("comunidad", comunidad.name())
                .param("despacho", despacho.valor())
                .query(DiaInhabilJdbcAdapter::mapear)
                .list();
    }

    @Override
    public Optional<DiaInhabil> buscar(UUID id) {
        return jdbc.sql("select " + COLUMNAS + " from dias_inhabiles where id = :id")
                .param("id", id)
                .query(DiaInhabilJdbcAdapter::mapear)
                .optional();
    }

    @Override
    public boolean existe(DiaInhabil dia) {
        return jdbc.sql("""
                        select count(*) from dias_inhabiles
                        where fecha = :fecha and ambito = :ambito
                          and comunidad_autonoma is not distinct from :comunidad
                          and despacho_id is not distinct from :despacho""")
                .param("fecha", dia.fecha())
                .param("ambito", dia.ambito().name())
                .param("comunidad", dia.comunidad() == null ? null : dia.comunidad().name())
                .param("despacho", dia.despachoId() == null ? null : dia.despachoId().valor())
                .query(Long.class)
                .single() > 0;
    }

    @Override
    public void guardar(DiaInhabil dia) {
        jdbc.sql("insert into dias_inhabiles (" + COLUMNAS + ") values "
                        + "(:id, :despacho, :ambito, :comunidad, :fecha, :descripcion, :provisional)")
                .param("id", dia.id())
                .param("despacho", dia.despachoId() == null ? null : dia.despachoId().valor())
                .param("ambito", dia.ambito().name())
                .param("comunidad", dia.comunidad() == null ? null : dia.comunidad().name())
                .param("fecha", dia.fecha())
                .param("descripcion", dia.descripcion())
                .param("provisional", dia.provisional())
                .update();
    }

    @Override
    public void eliminar(UUID id) {
        jdbc.sql("delete from dias_inhabiles where id = :id").param("id", id).update();
    }

    @Override
    public List<FestivoReferencia> festivosLocalesConocidos(String municipio, String provincia) {
        return jdbc.sql("""
                        select fecha, descripcion from festivos_locales_referencia
                        where lower(trim(municipio)) = lower(trim(:municipio))
                          and lower(trim(provincia)) = lower(trim(:provincia))
                        order by fecha""")
                .param("municipio", municipio)
                .param("provincia", provincia)
                .query((rs, n) -> new FestivoReferencia(rs.getObject("fecha", LocalDate.class), rs.getString("descripcion")))
                .list();
    }

    private static DiaInhabil mapear(ResultSet rs, int fila) throws SQLException {
        UUID despacho = rs.getObject("despacho_id", UUID.class);
        String comunidad = rs.getString("comunidad_autonoma");
        return new DiaInhabil(
                rs.getObject("id", UUID.class),
                despacho == null ? null : new DespachoId(despacho),
                AmbitoDia.valueOf(rs.getString("ambito")),
                comunidad == null ? null : ComunidadAutonoma.valueOf(comunidad),
                rs.getObject("fecha", LocalDate.class),
                rs.getString("descripcion"),
                rs.getBoolean("provisional"));
    }
}
