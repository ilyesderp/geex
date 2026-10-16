package es.geex.identidad.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.NoEncontradoException;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.ReglaNegocioException;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReglasIdentidadTest {

    private static final Instant AHORA = Instant.parse("2026-10-16T09:00:00Z");

    @Nested
    class Contrasenas {

        @Test
        void exige_al_menos_doce_caracteres() {
            assertThatThrownBy(() -> PoliticaContrasenas.validar("corta-123"))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("12");
        }

        @Test
        void rechaza_contrasenas_con_muy_pocos_caracteres_distintos() {
            assertThatThrownBy(() -> PoliticaContrasenas.validar("aaaaaaaaaaaaaaaa"))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        void acepta_frases_de_paso_largas() {
            assertThatCode(() -> PoliticaContrasenas.validar("caballo correcto batería grapa"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class Permisos {

        private final MatrizPermisos matriz = MatrizPermisos.predeterminada(DespachoId.nuevo());

        @Test
        void el_titular_siempre_tiene_todos_los_permisos() {
            assertThat(matriz.permisosDe(Rol.TITULAR)).containsExactlyInAnyOrder(Permiso.values());
        }

        @Test
        void los_permisos_del_titular_no_se_pueden_modificar() {
            assertThatThrownBy(() -> matriz.asignar(Rol.TITULAR, Set.of()))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        void un_gestor_no_gestiona_usuarios_por_defecto_pero_se_puede_cambiar() {
            assertThat(matriz.permisosDe(Rol.GESTOR)).doesNotContain(Permiso.USUARIOS_GESTIONAR);

            var nuevos = EnumSet.copyOf(matriz.permisosDe(Rol.GESTOR));
            nuevos.add(Permiso.USUARIOS_GESTIONAR);
            matriz.asignar(Rol.GESTOR, nuevos);

            assertThat(matriz.permisosDe(Rol.GESTOR)).contains(Permiso.USUARIOS_GESTIONAR);
        }

        @Test
        void un_colaborador_externo_solo_consulta() {
            assertThat(matriz.permisosDe(Rol.COLABORADOR))
                    .containsExactlyInAnyOrder(Permiso.CONTACTOS_VER, Permiso.EXPEDIENTES_VER, Permiso.DOCUMENTOS_VER);
        }
    }

    @Nested
    class EnlacesDeUnSoloUso {

        private TokenUnUso enlace(TokenUnUso.Tipo tipo) {
            return TokenUnUso.nuevo(UsuarioId.nuevo(), DespachoId.nuevo(), tipo, "hash",
                    AHORA.plus(Duration.ofHours(48)), AHORA);
        }

        @Test
        void un_enlace_valido_se_puede_usar_una_sola_vez() {
            var invitacion = enlace(TokenUnUso.Tipo.INVITACION);
            assertThatCode(() -> invitacion.validar(TokenUnUso.Tipo.INVITACION, AHORA)).doesNotThrowAnyException();

            invitacion.usar(AHORA);

            assertThatThrownBy(() -> invitacion.validar(TokenUnUso.Tipo.INVITACION, AHORA))
                    .isInstanceOf(NoEncontradoException.class);
        }

        @Test
        void un_enlace_caducado_no_es_valido() {
            var invitacion = enlace(TokenUnUso.Tipo.INVITACION);
            assertThatThrownBy(() -> invitacion.validar(TokenUnUso.Tipo.INVITACION, AHORA.plus(Duration.ofHours(48))))
                    .isInstanceOf(NoEncontradoException.class);
        }

        @Test
        void una_invitacion_no_sirve_para_restablecer_la_contrasena() {
            var invitacion = enlace(TokenUnUso.Tipo.INVITACION);
            assertThatThrownBy(() -> invitacion.validar(TokenUnUso.Tipo.RESTABLECIMIENTO, AHORA))
                    .isInstanceOf(NoEncontradoException.class);
        }
    }
}
