package es.geex.calendario.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calendario de un despacho: sábados, domingos y sus festivos (nacionales, de su
 * comunidad autónoma y locales) son inhábiles (art. 30.2 y 30.7 Ley 39/2015).
 *
 * <p>En la fase 1 se añadirá aquí el cómputo de plazos en días hábiles y naturales.
 */
public final class CalendarioLaboral {

    private final Set<LocalDate> festivos;

    public CalendarioLaboral(Collection<DiaInhabil> dias) {
        this.festivos = dias.stream().map(DiaInhabil::fecha).collect(Collectors.toUnmodifiableSet());
    }

    public boolean esHabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY && !festivos.contains(fecha);
    }

    /** El propio día si es hábil, o el primer hábil siguiente (art. 30.5 Ley 39/2015). */
    public LocalDate primerHabilDesde(LocalDate fecha) {
        LocalDate dia = fecha;
        while (!esHabil(dia)) {
            dia = dia.plusDays(1);
        }
        return dia;
    }
}
