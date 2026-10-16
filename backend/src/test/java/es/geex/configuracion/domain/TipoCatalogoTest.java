package es.geex.configuracion.domain;

import static org.assertj.core.api.Assertions.assertThat;

import es.geex.configuracion.domain.TipoCatalogo.ValorInicial;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TipoCatalogoTest {

    @ParameterizedTest
    @EnumSource(TipoCatalogo.class)
    void los_codigos_de_cada_lista_son_unicos_y_validos(TipoCatalogo tipo) {
        List<String> codigos = tipo.valoresIniciales().stream().map(ValorInicial::codigo).toList();

        assertThat(codigos).isNotEmpty().doesNotHaveDuplicates();
        assertThat(codigos).allSatisfy(c -> assertThat(c).matches("[A-Z0-9_]{1,60}"));
    }

    @Test
    void los_regimenes_tienen_los_codigos_que_usaran_las_reglas_de_renovacion() {
        assertThat(TipoCatalogo.REGIMEN.valoresIniciales()).extracting(ValorInicial::codigo)
                .containsExactly("COMUNITARIO", "GENERAL", "INVERSORES", "PROTECCION_INTERNACIONAL", "OTROS");
    }

    @Test
    void la_lista_de_nacionalidades_incluye_todos_los_paises_con_nombre_en_espanol() {
        List<ValorInicial> nacionalidades = TipoCatalogo.NACIONALIDAD.valoresIniciales();

        assertThat(nacionalidades).hasSizeGreaterThan(230);
        assertThat(nacionalidades).contains(
                new ValorInicial("MAR", "Marruecos"),
                new ValorInicial("COL", "Colombia"),
                new ValorInicial("UKR", "Ucrania"),
                new ValorInicial("APATRIDA", "Apátrida"));
    }

    @Test
    void genera_codigos_estables_a_partir_de_las_etiquetas() {
        assertThat(CodigoCatalogo.desdeEtiqueta("Hijo/a menor de 18 años")).isEqualTo("HIJO_A_MENOR_DE_18_ANOS");
        assertThat(CodigoCatalogo.desdeEtiqueta("  Régimen  General ")).isEqualTo("REGIMEN_GENERAL");
    }
}
