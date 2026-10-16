package es.geex.multidespacho;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import es.geex.despacho.application.port.in.CrearDespachoUseCase;
import es.geex.soporte.PruebaIntegracion;
import java.sql.SQLException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Multi-tenant: un despacho nunca ve ni modifica datos de otro, ni por la API
 * ni directamente en la base de datos (Row Level Security de PostgreSQL).
 */
class AislamientoDespachosTest extends PruebaIntegracion {

    @Autowired
    CrearDespachoUseCase crearDespacho;

    private Sesion sesionA;
    private Sesion sesionB;
    private DespachoId despachoA;
    private DespachoId despachoB;

    @BeforeEach
    void preparar() throws SQLException {
        reiniciarBaseDeDatos();
        Credenciales titular = instalar("Despacho A (Alicante)");
        sesionA = iniciarSesion(titular);
        despachoA = DespachoId.de(get("/api/yo", sesionA.token()).json().get("despachoActual").get("id").asString());

        // Segundo despacho, en Madrid; el mismo titular también trabaja en él
        despachoB = ContextoEjecucion.comoSistema(() -> crearDespacho.crear(
                new CrearDespachoUseCase.NuevoDespacho("Despacho B (Madrid)", "Madrid", "Madrid", ComunidadAutonoma.MADRID)));
        try (var c = conexionAdministrador(); var s = c.prepareStatement("""
                insert into membresias (id, usuario_id, despacho_id, rol, activa, creada_en)
                select gen_random_uuid(), u.id, ?, 'TITULAR', true, now() from usuarios u where u.email = ?""")) {
            s.setObject(1, despachoB.valor());
            s.setString(2, titular.email());
            s.executeUpdate();
        }
        var cambio = enviarCambioDespacho(despachoB);
        sesionB = new Sesion(cambio.json().get("tokenAcceso").asString(), cambio.cookieRefresco());
    }

    @Test
    void el_usuario_ve_sus_dos_despachos_y_cada_token_apunta_a_uno() {
        var yoB = get("/api/yo", sesionB.token()).json();
        assertThat(yoB.get("despachoActual").get("id").asString()).isEqualTo(despachoB.toString());
        assertThat(yoB.get("despachos").size()).isEqualTo(2);
        assertThat(get("/api/despacho", sesionA.token()).json().get("nombre").asString()).isEqualTo("Despacho A (Alicante)");
        assertThat(get("/api/despacho", sesionB.token()).json().get("nombre").asString()).isEqualTo("Despacho B (Madrid)");
    }

    @Test
    void cada_despacho_tiene_su_calendario_segun_su_comunidad_y_municipio() {
        var calendarioA = get("/api/calendario/2026", sesionA.token()).json();
        var calendarioB = get("/api/calendario/2026", sesionB.token()).json();

        assertThat(calendarioA.get("comunidadAutonoma").asString()).isEqualTo("COMUNITAT_VALENCIANA");
        assertThat(calendarioA.toString()).contains("Santa Faz").contains("Día de la Comunitat Valenciana");
        assertThat(calendarioB.get("comunidadAutonoma").asString()).isEqualTo("MADRID");
        assertThat(calendarioB.toString()).doesNotContain("Santa Faz").doesNotContain("Comunitat Valenciana");

        var festivoB = post("/api/calendario/festivos-locales", sesionB.token(),
                "{\"fecha\":\"2026-05-15\",\"descripcion\":\"San Isidro\"}");
        assertThat(festivoB.estado()).isEqualTo(201);
        assertThat(get("/api/calendario/2026", sesionA.token()).cuerpo()).doesNotContain("San Isidro");

        var borrarDesdeA = delete("/api/calendario/festivos-locales/" + festivoB.json().get("id").asString(), sesionA.token());
        assertThat(borrarDesdeA.estado()).as("A no puede borrar un festivo de B").isEqualTo(404);
    }

    @Test
    void un_despacho_no_puede_modificar_las_listas_de_otro() {
        String idValorB = get("/api/catalogos/ESTADO_CIVIL/valores", sesionB.token()).json().get(0).get("id").asString();

        var intento = put("/api/catalogos/ESTADO_CIVIL/valores/" + idValorB, sesionA.token(),
                "{\"etiqueta\":\"Modificado por A\",\"activo\":true}");
        assertThat(intento.estado()).isEqualTo(404);
        assertThat(get("/api/catalogos/ESTADO_CIVIL/valores", sesionB.token()).cuerpo()).doesNotContain("Modificado por A");
    }

    @Test
    void la_base_de_datos_impide_leer_o_escribir_filas_de_otro_despacho() throws SQLException {
        try (var c = conexionAplicacion()) {
            try (var contexto = c.prepareStatement("select set_config('app.despacho_id', ?, false)")) {
                contexto.setString(1, despachoA.toString());
                contexto.execute();
            }
            try (var consulta = c.prepareStatement("select count(*) from valores_catalogo where despacho_id = ?")) {
                consulta.setObject(1, despachoB.valor());
                try (var rs = consulta.executeQuery()) {
                    rs.next();
                    assertThat(rs.getLong(1)).as("filas de B visibles con el contexto de A").isZero();
                }
            }
            try (var s = c.createStatement(); var rs = s.executeQuery("select count(*) from valores_catalogo")) {
                rs.next();
                assertThat(rs.getLong(1)).as("A sí ve sus propias listas").isPositive();
            }
            try (var insercion = c.prepareStatement("""
                    insert into valores_catalogo (id, despacho_id, catalogo_codigo, codigo, etiqueta, orden)
                    values (?, ?, 'ESTADO_CIVIL', 'INTRUSO', 'Intruso', 99)""")) {
                insercion.setObject(1, UUID.randomUUID());
                insercion.setObject(2, despachoB.valor());
                assertThatThrownBy(insercion::executeUpdate)
                        .as("insertar en otro despacho viola la política RLS")
                        .isInstanceOf(SQLException.class)
                        .hasMessageContaining("row-level security");
            }
        }
    }

    @Test
    void sin_contexto_el_rol_de_la_aplicacion_no_ve_nada() throws SQLException {
        try (var c = conexionAplicacion(); var s = c.createStatement();
             var rs = s.executeQuery("select (select count(*) from despachos), (select count(*) from valores_catalogo)")) {
            rs.next();
            assertThat(rs.getLong(1)).isZero();
            assertThat(rs.getLong(2)).isZero();
        }
    }

    private Respuesta enviarCambioDespacho(DespachoId destino) {
        return post("/api/auth/cambiar-despacho", sesionA.token(), "{\"despachoId\":\"" + destino + "\"}");
    }
}
