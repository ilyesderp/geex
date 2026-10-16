package es.geex.compartido.adapter.persistence;

import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * Envuelve la fuente de datos de la aplicación: cada vez que se entrega una
 * conexión del pool, fija las variables de sesión {@code app.despacho_id} y
 * {@code app.sistema} a partir del {@link ContextoEjecucion}.
 *
 * <p>Las políticas de Row Level Security de PostgreSQL usan esas variables, de
 * modo que una consulta nunca puede devolver filas de otro despacho aunque al
 * código se le olvide filtrar. Como se fijan siempre (también a vacío), una
 * conexión reutilizada nunca arrastra el contexto de la petición anterior.
 */
public class DataSourceMultiDespacho extends DelegatingDataSource {

    private static final String SQL =
            "select set_config('app.despacho_id', ?, false), set_config('app.sistema', ?, false)";

    public DataSourceMultiDespacho(DataSource destino) {
        super(destino);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return conContexto(super.getConnection());
    }

    @Override
    public Connection getConnection(String usuario, String contrasena) throws SQLException {
        return conContexto(super.getConnection(usuario, contrasena));
    }

    private static Connection conContexto(Connection conexion) throws SQLException {
        var datos = ContextoEjecucion.actual();
        String despacho = datos.map(ContextoEjecucion.Datos::despachoId).map(Object::toString).orElse("");
        boolean sistema = datos.map(ContextoEjecucion.Datos::sistema).orElse(false);
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL)) {
            sentencia.setString(1, despacho);
            sentencia.setString(2, sistema ? "on" : "off");
            sentencia.execute();
            return conexion;
        } catch (SQLException | RuntimeException e) {
            conexion.close();
            throw e;
        }
    }
}
