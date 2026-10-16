package es.geex.calendario.adapter.in.web;

import es.geex.calendario.application.port.in.CalendarioUseCase;
import es.geex.calendario.application.port.in.CalendarioUseCase.CalendarioAnual;
import es.geex.calendario.application.port.in.CalendarioUseCase.DiaInhabilVista;
import es.geex.calendario.domain.AmbitoDia;
import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import es.geex.compartido.domain.ComunidadAutonoma;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calendario")
@Tag(name = "Calendario", description = "Calendario laboral: días inhábiles para el cómputo de plazos")
class CalendarioController {

    private final CalendarioUseCase calendario;

    CalendarioController(CalendarioUseCase calendario) {
        this.calendario = calendario;
    }

    @GetMapping("/{anio}")
    @Operation(operationId = "obtenerCalendario", summary = "Días inhábiles del año para el despacho actual")
    CalendarioAnual obtener(@PathVariable int anio) {
        return calendario.obtenerCalendario(anio);
    }

    @PostMapping("/festivos-locales")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Añadir un festivo local del municipio del despacho")
    DiaInhabilVista anadirLocal(@Valid @RequestBody FestivoLocalPeticion p) {
        return calendario.anadirFestivoLocal(p.fecha(), p.descripcion());
    }

    @DeleteMapping("/festivos-locales/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un festivo local")
    void eliminarLocal(@PathVariable UUID id) {
        calendario.eliminarFestivoLocal(id);
    }

    /** Los festivos globales son comunes a todos los despachos: se escriben en modo sistema tras comprobar que es superadmin. */
    @PostMapping("/festivos-globales")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Añadir un festivo nacional o autonómico (administrador de la plataforma)")
    DiaInhabilVista anadirGlobal(@Valid @RequestBody FestivoGlobalPeticion p) {
        return ContextoEjecucion.comoSistema(() -> calendario.anadirFestivoGlobal(p.ambito(), p.comunidadAutonoma(),
                p.fecha(), p.descripcion(), p.provisional()));
    }

    @DeleteMapping("/festivos-globales/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un festivo nacional o autonómico (administrador de la plataforma)")
    void eliminarGlobal(@PathVariable UUID id) {
        ContextoEjecucion.comoSistema(() -> calendario.eliminarFestivoGlobal(id));
    }

    record FestivoLocalPeticion(@NotNull LocalDate fecha, @NotBlank @Size(max = 200) String descripcion) {
    }

    record FestivoGlobalPeticion(@NotNull AmbitoDia ambito, ComunidadAutonoma comunidadAutonoma,
                                 @NotNull LocalDate fecha, @NotBlank @Size(max = 200) String descripcion,
                                 boolean provisional) {
    }
}
