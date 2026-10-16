package es.geex.calendario.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.ReglaNegocioException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalendarioLaboralTest {

    private final DespachoId despacho = DespachoId.nuevo();

    /** Alicante 2026: fiesta nacional del 12 de octubre (lunes) y Santa Faz (jueves 16 de abril). */
    private final CalendarioLaboral alicante = new CalendarioLaboral(List.of(
            DiaInhabil.global(AmbitoDia.NACIONAL, null, LocalDate.of(2026, 10, 12), "Fiesta Nacional de España", false),
            DiaInhabil.local(despacho, LocalDate.of(2026, 4, 16), "Santa Faz")));

    @Test
    void sabados_domingos_y_festivos_son_inhabiles() {
        assertThat(alicante.esHabil(LocalDate.of(2026, 10, 10))).as("sábado").isFalse();
        assertThat(alicante.esHabil(LocalDate.of(2026, 10, 11))).as("domingo").isFalse();
        assertThat(alicante.esHabil(LocalDate.of(2026, 10, 12))).as("festivo nacional").isFalse();
        assertThat(alicante.esHabil(LocalDate.of(2026, 4, 16))).as("festivo local").isFalse();
        assertThat(alicante.esHabil(LocalDate.of(2026, 10, 13))).as("martes laborable").isTrue();
    }

    @Test
    void si_el_dia_es_inhabil_se_pasa_al_primer_habil_siguiente() {
        // Sábado 10/10 → domingo → lunes festivo → martes 13/10
        assertThat(alicante.primerHabilDesde(LocalDate.of(2026, 10, 10))).isEqualTo(LocalDate.of(2026, 10, 13));
        assertThat(alicante.primerHabilDesde(LocalDate.of(2026, 10, 13))).isEqualTo(LocalDate.of(2026, 10, 13));
    }

    @Test
    void no_se_pueden_anadir_festivos_en_fin_de_semana() {
        assertThatThrownBy(() -> DiaInhabil.local(despacho, LocalDate.of(2026, 10, 10), "Sábado"))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void un_festivo_autonomico_exige_comunidad() {
        assertThatThrownBy(() -> DiaInhabil.global(AmbitoDia.AUTONOMICO, null, LocalDate.of(2026, 3, 19), "San José", false))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(DiaInhabil.global(AmbitoDia.AUTONOMICO, ComunidadAutonoma.COMUNITAT_VALENCIANA,
                LocalDate.of(2026, 3, 19), "San José", false).esGlobal()).isTrue();
    }
}
