package es.geex.despacho.adapter.in.web;

import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.despacho.application.port.in.GestionarDespachoUseCase;
import es.geex.despacho.application.port.in.GestionarDespachoUseCase.DatosDespachoComando;
import es.geex.despacho.application.port.in.GestionarDespachoUseCase.DespachoVista;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/despacho")
@Tag(name = "Despacho", description = "Datos generales del despacho")
class DespachoController {

    private final GestionarDespachoUseCase despacho;

    DespachoController(GestionarDespachoUseCase despacho) {
        this.despacho = despacho;
    }

    @GetMapping
    @Operation(operationId = "obtenerDespacho", summary = "Datos del despacho actual")
    DespachoVista obtener() {
        return despacho.obtenerActual();
    }

    @PutMapping
    @Operation(operationId = "actualizarDespacho", summary = "Modificar los datos del despacho")
    DespachoVista actualizar(@Valid @RequestBody DatosDespachoPeticion p) {
        return despacho.actualizar(new DatosDespachoComando(p.nombre(), p.razonSocial(), p.nif(), p.direccion(),
                p.codigoPostal(), p.municipio(), p.provincia(), p.comunidadAutonoma(), p.telefono(), p.email(), p.web(), p.version()));
    }

    record DatosDespachoPeticion(
            @NotBlank @Size(max = 150) String nombre,
            @Size(max = 200) String razonSocial,
            @Size(max = 20) String nif,
            @Size(max = 250) String direccion,
            @Size(max = 10) String codigoPostal,
            @NotBlank @Size(max = 120) String municipio,
            @NotBlank @Size(max = 120) String provincia,
            @NotNull ComunidadAutonoma comunidadAutonoma,
            @Size(max = 30) String telefono,
            @Email @Size(max = 254) String email,
            @Size(max = 250) String web,
            @NotNull @PositiveOrZero Long version) {
    }
}
