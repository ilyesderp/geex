package es.geex.despacho;

import static org.assertj.core.api.Assertions.assertThat;

import es.geex.soporte.PruebaIntegracion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Edición de los datos del despacho por dos personas a la vez (bloqueo optimista). */
class DatosDespachoIntegracionTest extends PruebaIntegracion {

    private Sesion titular;

    @BeforeEach
    void preparar() {
        reiniciarBaseDeDatos();
        titular = iniciarSesion(instalar("Despacho Edición"));
    }

    @Test
    void si_otro_usuario_guardo_antes_no_se_pisan_sus_cambios() {
        long versionCargada = get("/api/despacho", titular.token()).json().get("version").asLong();

        var primero = put("/api/despacho", titular.token(), datos("Nombre de la primera persona", versionCargada));
        assertThat(primero.estado()).isEqualTo(200);
        assertThat(primero.json().get("version").asLong()).isGreaterThan(versionCargada);

        // La segunda persona cargó la misma versión antes de que la primera guardara
        var segundo = put("/api/despacho", titular.token(), datos("Nombre de la segunda persona", versionCargada));
        assertThat(segundo.estado()).isEqualTo(409);
        assertThat(segundo.codigoError()).isEqualTo("CONFLICTO_EDICION");
        assertThat(get("/api/despacho", titular.token()).json().get("nombre").asString())
                .isEqualTo("Nombre de la primera persona");
    }

    private static String datos(String nombre, long version) {
        return """
                {"nombre":"%s","municipio":"Alicante","provincia":"Alicante",
                 "comunidadAutonoma":"COMUNITAT_VALENCIANA","version":%d}""".formatted(nombre, version);
    }
}
