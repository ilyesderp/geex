package es.geex.calendario.application.port.out;

import es.geex.calendario.domain.DiaInhabil;
import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiaInhabilRepository {

    /** Nacionales + autonómicos de la comunidad + locales del despacho, entre dos fechas (incluidas). */
    List<DiaInhabil> listar(DespachoId despacho, ComunidadAutonoma comunidad, LocalDate desde, LocalDate hasta);

    Optional<DiaInhabil> buscar(UUID id);

    boolean existe(DiaInhabil dia);

    void guardar(DiaInhabil dia);

    void eliminar(UUID id);

    List<FestivoReferencia> festivosLocalesConocidos(String municipio, String provincia);

    record FestivoReferencia(LocalDate fecha, String descripcion) {
    }
}
