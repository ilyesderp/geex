package es.geex.calendario.domain;

import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.ReglaNegocioException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Día inhábil (festivo). Los nacionales y autonómicos son comunes a todos los
 * despachos; los locales pertenecen a un despacho.
 */
public record DiaInhabil(
        UUID id,
        DespachoId despachoId,
        AmbitoDia ambito,
        ComunidadAutonoma comunidad,
        LocalDate fecha,
        String descripcion,
        boolean provisional) {

    public DiaInhabil {
        Objects.requireNonNull(id);
        Objects.requireNonNull(ambito);
        Objects.requireNonNull(fecha);
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaNegocioException("DESCRIPCION_OBLIGATORIA", "Indica el nombre del festivo");
        }
        descripcion = descripcion.strip();
        if (descripcion.length() > 200) {
            throw new ReglaNegocioException("DESCRIPCION_LARGA", "El nombre del festivo es demasiado largo");
        }
        switch (ambito) {
            case NACIONAL -> {
                if (despachoId != null || comunidad != null) {
                    throw new IllegalArgumentException("Un festivo nacional no tiene despacho ni comunidad");
                }
            }
            case AUTONOMICO -> {
                if (despachoId != null || comunidad == null) {
                    throw new ReglaNegocioException("COMUNIDAD_OBLIGATORIA", "Indica la comunidad autónoma del festivo");
                }
            }
            case LOCAL -> Objects.requireNonNull(despachoId, "Un festivo local pertenece a un despacho");
        }
    }

    public static DiaInhabil local(DespachoId despacho, LocalDate fecha, String descripcion) {
        exigirDiaLaborable(fecha);
        return new DiaInhabil(UUID.randomUUID(), despacho, AmbitoDia.LOCAL, null, fecha, descripcion, false);
    }

    public static DiaInhabil global(AmbitoDia ambito, ComunidadAutonoma comunidad, LocalDate fecha, String descripcion,
                                    boolean provisional) {
        if (ambito == AmbitoDia.LOCAL) {
            throw new IllegalArgumentException("Los festivos locales pertenecen a un despacho");
        }
        exigirDiaLaborable(fecha);
        return new DiaInhabil(UUID.randomUUID(), null, ambito, ambito == AmbitoDia.NACIONAL ? null : comunidad,
                fecha, descripcion, provisional);
    }

    private static void exigirDiaLaborable(LocalDate fecha) {
        if (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ReglaNegocioException("FESTIVO_EN_FIN_DE_SEMANA",
                    "Los sábados y domingos ya son inhábiles; no hace falta añadirlos");
        }
    }

    public boolean esGlobal() {
        return ambito != AmbitoDia.LOCAL;
    }
}
