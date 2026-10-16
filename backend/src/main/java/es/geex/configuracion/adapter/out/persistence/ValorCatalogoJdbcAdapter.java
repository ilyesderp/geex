package es.geex.configuracion.adapter.out.persistence;

import es.geex.compartido.domain.DespachoId;
import es.geex.configuracion.application.port.out.ValorCatalogoRepository;
import es.geex.configuracion.domain.TipoCatalogo;
import es.geex.configuracion.domain.ValorCatalogo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * Persistencia de las listas desplegables con JDBC: son filas simples y la
 * inicialización de un despacho inserta varios cientos de una vez (por lotes).
 */
@Component
class ValorCatalogoJdbcAdapter implements ValorCatalogoRepository {

    private static final String COLUMNAS =
            "id, despacho_id, catalogo_codigo, codigo, etiqueta, orden, activo, sistema, version";

    private final NamedParameterJdbcTemplate jdbc;

    ValorCatalogoJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ValorCatalogo> listar(DespachoId despacho, TipoCatalogo tipo) {
        return jdbc.query("select " + COLUMNAS + " from valores_catalogo where despacho_id = :d and catalogo_codigo = :t"
                        + " order by orden, etiqueta",
                new MapSqlParameterSource("d", despacho.valor()).addValue("t", tipo.name()),
                ValorCatalogoJdbcAdapter::mapear);
    }

    @Override
    public Map<TipoCatalogo, long[]> contarPorTipo(DespachoId despacho) {
        Map<TipoCatalogo, long[]> resultado = new EnumMap<>(TipoCatalogo.class);
        jdbc.query("""
                        select catalogo_codigo, count(*) as total, count(*) filter (where activo) as activos
                        from valores_catalogo where despacho_id = :d group by catalogo_codigo""",
                new MapSqlParameterSource("d", despacho.valor()),
                rs -> {
                    long[] conteo = {rs.getLong("total"), rs.getLong("activos")};
                    tipo(rs.getString("catalogo_codigo")).ifPresent(t -> resultado.put(t, conteo));
                });
        return resultado;
    }

    @Override
    public Optional<ValorCatalogo> buscar(DespachoId despacho, UUID id) {
        return jdbc.query("select " + COLUMNAS + " from valores_catalogo where despacho_id = :d and id = :id",
                        new MapSqlParameterSource("d", despacho.valor()).addValue("id", id),
                        ValorCatalogoJdbcAdapter::mapear)
                .stream().findFirst();
    }

    @Override
    public void guardar(ValorCatalogo valor) {
        int actualizadas = jdbc.update("""
                update valores_catalogo set etiqueta = :etiqueta, orden = :orden, activo = :activo,
                       version = version + 1
                where id = :id and version = :version""", parametros(valor));
        if (actualizadas == 0) {
            if (buscar(valor.despachoId(), valor.id()).isPresent()) {
                throw new ObjectOptimisticLockingFailureException(ValorCatalogo.class, valor.id());
            }
            jdbc.update(INSERT, parametros(valor));
        }
    }

    @Override
    public void guardarTodos(Collection<ValorCatalogo> valores) {
        if (valores.stream().allMatch(v -> v.version() == 0) && ningunoExiste(valores)) {
            jdbc.batchUpdate(INSERT, valores.stream().map(ValorCatalogoJdbcAdapter::parametros)
                    .toArray(SqlParameterSource[]::new));
        } else {
            valores.forEach(this::guardar);
        }
    }

    @Override
    public void eliminar(ValorCatalogo valor) {
        jdbc.update("delete from valores_catalogo where id = :id", new MapSqlParameterSource("id", valor.id()));
    }

    private boolean ningunoExiste(Collection<ValorCatalogo> valores) {
        if (valores.isEmpty()) {
            return true;
        }
        Integer existentes = jdbc.queryForObject("select count(*) from valores_catalogo where id in (:ids)",
                new MapSqlParameterSource("ids", valores.stream().map(ValorCatalogo::id).toList()), Integer.class);
        return existentes == null || existentes == 0;
    }

    private static final String INSERT = "insert into valores_catalogo (" + COLUMNAS + ") values "
            + "(:id, :despacho, :tipo, :codigo, :etiqueta, :orden, :activo, :sistema, 0)";

    private static MapSqlParameterSource parametros(ValorCatalogo v) {
        return new MapSqlParameterSource()
                .addValue("id", v.id())
                .addValue("despacho", v.despachoId().valor())
                .addValue("tipo", v.tipo().name())
                .addValue("codigo", v.codigo())
                .addValue("etiqueta", v.etiqueta())
                .addValue("orden", v.orden())
                .addValue("activo", v.activo())
                .addValue("sistema", v.sistema())
                .addValue("version", v.version());
    }

    private static ValorCatalogo mapear(ResultSet rs, int fila) throws SQLException {
        return ValorCatalogo.reconstruir(
                rs.getObject("id", UUID.class),
                new DespachoId(rs.getObject("despacho_id", UUID.class)),
                TipoCatalogo.valueOf(rs.getString("catalogo_codigo")),
                rs.getString("codigo"),
                rs.getString("etiqueta"),
                rs.getInt("orden"),
                rs.getBoolean("activo"),
                rs.getBoolean("sistema"),
                rs.getLong("version"));
    }

    private static Optional<TipoCatalogo> tipo(String codigo) {
        try {
            return Optional.of(TipoCatalogo.valueOf(codigo));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
