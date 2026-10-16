package es.geex.identidad.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import es.geex.compartido.domain.ReglaNegocioException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    private static final Instant AHORA = Instant.parse("2026-10-16T09:00:00Z");

    @Test
    void normaliza_el_correo_a_minusculas_sin_espacios() {
        assertThat(Usuario.normalizarEmail("  Ana.Garcia@Despacho.ES ")).isEqualTo("ana.garcia@despacho.es");
    }

    @Test
    void rechaza_correos_no_validos() {
        assertThatThrownBy(() -> Usuario.normalizarEmail("no-es-un-correo"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("correo");
    }

    @Test
    void un_invitado_no_puede_acceder_hasta_elegir_contrasena() {
        Usuario invitado = Usuario.invitado("luis@despacho.es", "Luis", "Pérez", AHORA);

        assertThat(invitado.pendienteActivacion()).isTrue();
        assertThat(invitado.puedeAcceder(AHORA)).isFalse();

        invitado.establecerContrasena("hash");
        assertThat(invitado.puedeAcceder(AHORA)).isTrue();
    }

    @Test
    void se_bloquea_tras_cinco_intentos_fallidos_durante_quince_minutos() {
        Usuario usuario = Usuario.conContrasena("ana@despacho.es", "Ana", "García", "hash", false, AHORA);

        for (int i = 0; i < Usuario.MAX_INTENTOS_FALLIDOS - 1; i++) {
            usuario.registrarAccesoFallido(AHORA);
        }
        assertThat(usuario.bloqueado(AHORA)).isFalse();

        usuario.registrarAccesoFallido(AHORA);
        assertThat(usuario.bloqueado(AHORA)).isTrue();
        assertThat(usuario.bloqueado(AHORA.plus(Usuario.DURACION_BLOQUEO).minusSeconds(1))).isTrue();
        assertThat(usuario.bloqueado(AHORA.plus(Usuario.DURACION_BLOQUEO))).isFalse();
    }

    @Test
    void un_acceso_correcto_reinicia_el_contador_de_intentos() {
        Usuario usuario = Usuario.conContrasena("ana@despacho.es", "Ana", "García", "hash", false, AHORA);
        usuario.registrarAccesoFallido(AHORA);
        usuario.registrarAccesoFallido(AHORA);

        usuario.registrarAccesoCorrecto(AHORA);

        assertThat(usuario.intentosFallidos()).isZero();
        assertThat(usuario.ultimoAcceso()).isEqualTo(AHORA);
    }

    @Test
    void cambiar_la_contrasena_invalida_las_sesiones_abiertas() {
        Usuario usuario = Usuario.conContrasena("ana@despacho.es", "Ana", "García", "hash", false, AHORA);
        int versionAnterior = usuario.versionToken();

        usuario.establecerContrasena("otro-hash");

        assertThat(usuario.versionToken()).isEqualTo(versionAnterior + 1);
    }
}
