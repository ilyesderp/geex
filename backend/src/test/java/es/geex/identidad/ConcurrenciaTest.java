package es.geex.identidad;

import static org.assertj.core.api.Assertions.assertThat;

import es.geex.soporte.PruebaIntegracion;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Condiciones de carrera reales contra PostgreSQL: varias peticiones idénticas
 * salen a la vez (hilos virtuales sincronizados con un pistoletazo de salida).
 */
class ConcurrenciaTest extends PruebaIntegracion {

    private static final int PETICIONES = 8;

    @RepeatedTest(3)
    void renovaciones_simultaneas_con_el_mismo_token_solo_una_tiene_exito() throws Exception {
        reiniciarBaseDeDatos();
        Sesion sesion = iniciarSesion(instalar("Despacho Concurrencia"));

        List<Integer> estados = enParalelo(() -> refrescar(sesion.cookieRefresco()).estado());

        assertThat(estados).filteredOn(e -> e == 200).hasSize(1);
        assertThat(estados).filteredOn(e -> e == 401).hasSize(PETICIONES - 1);
    }

    @Test
    void instalaciones_simultaneas_solo_crean_un_despacho() throws Exception {
        reiniciarBaseDeDatos();

        List<Integer> estados = enParalelo(() -> post("/api/instalacion", null, """
                {"nombreDespacho":"Carrera","municipio":"Alicante","provincia":"Alicante",
                 "comunidadAutonoma":"COMUNITAT_VALENCIANA","nombre":"A","apellidos":"B",
                 "email":"carrera-%s@despacho.test","contrasena":"%s"}"""
                .formatted(Thread.currentThread().threadId(), contrasenaNueva())).estado());

        assertThat(estados).filteredOn(e -> e == 201).hasSize(1);
        assertThat(estados).filteredOn(e -> e == 422).hasSize(PETICIONES - 1);
        assertThat(contar("despachos")).isEqualTo(1);
        assertThat(contar("usuarios")).isEqualTo(1);
    }

    private <T> List<T> enParalelo(Callable<T> tarea) throws Exception {
        var salida = new CountDownLatch(1);
        try (var ejecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<T>> futuros = new ArrayList<>();
            for (int i = 0; i < PETICIONES; i++) {
                futuros.add(ejecutor.submit(() -> {
                    salida.await();
                    return tarea.call();
                }));
            }
            salida.countDown();
            List<T> resultados = new ArrayList<>();
            for (Future<T> futuro : futuros) {
                resultados.add(futuro.get());
            }
            return resultados;
        }
    }

    private static long contar(String tabla) throws SQLException {
        try (var c = conexionAdministrador(); var s = c.createStatement();
             var rs = s.executeQuery("select count(*) from " + tabla)) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
