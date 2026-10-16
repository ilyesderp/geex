package es.geex.configuracion.adapter.in.web;

import es.geex.configuracion.application.port.in.CatalogosUseCase;
import es.geex.configuracion.application.port.in.CatalogosUseCase.ResumenCatalogo;
import es.geex.configuracion.application.port.in.CatalogosUseCase.ValorVista;
import es.geex.configuracion.domain.TipoCatalogo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogos")
@Tag(name = "Catalogos", description = "Listas desplegables parametrizables")
class CatalogosController {

    private final CatalogosUseCase catalogos;

    CatalogosController(CatalogosUseCase catalogos) {
        this.catalogos = catalogos;
    }

    @GetMapping
    @Operation(operationId = "listarCatalogos", summary = "Listas desplegables disponibles")
    List<ResumenCatalogo> listar() {
        return catalogos.listarCatalogos();
    }

    @GetMapping("/{tipo}/valores")
    @Operation(operationId = "listarValores", summary = "Valores de una lista (solo activos salvo que se pida lo contrario)")
    List<ValorVista> valores(@PathVariable TipoCatalogo tipo,
                             @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return catalogos.listarValores(tipo, incluirInactivos);
    }

    @PostMapping("/{tipo}/valores")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "anadirValor", summary = "Añadir un valor")
    ValorVista anadir(@PathVariable TipoCatalogo tipo, @Valid @RequestBody NuevoValor p) {
        return catalogos.anadirValor(tipo, p.etiqueta());
    }

    @PutMapping("/{tipo}/valores/{id}")
    @Operation(operationId = "modificarValor", summary = "Renombrar o activar/desactivar un valor")
    ValorVista modificar(@PathVariable TipoCatalogo tipo, @PathVariable UUID id, @Valid @RequestBody CambioValor p) {
        return catalogos.modificarValor(tipo, id, p.etiqueta(), p.activo());
    }

    @PutMapping("/{tipo}/orden")
    @Operation(operationId = "reordenarValores", summary = "Reordenar los valores de una lista")
    List<ValorVista> reordenar(@PathVariable TipoCatalogo tipo, @Valid @RequestBody NuevoOrden p) {
        return catalogos.reordenar(tipo, p.ids());
    }

    @DeleteMapping("/{tipo}/valores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "eliminarValor", summary = "Eliminar un valor añadido por el despacho")
    void eliminar(@PathVariable TipoCatalogo tipo, @PathVariable UUID id) {
        catalogos.eliminarValor(tipo, id);
    }

    record NuevoValor(@NotBlank @Size(max = 200) String etiqueta) {
    }

    record CambioValor(@NotBlank @Size(max = 200) String etiqueta, boolean activo) {
    }

    record NuevoOrden(@NotEmpty List<UUID> ids) {
    }
}
