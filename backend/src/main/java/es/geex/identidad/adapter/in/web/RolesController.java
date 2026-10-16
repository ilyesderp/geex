package es.geex.identidad.adapter.in.web;

import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.Rol;
import es.geex.identidad.application.port.in.GestionPermisosUseCase;
import es.geex.identidad.application.port.in.GestionPermisosUseCase.PermisosDeRol;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Roles y permisos")
class RolesController {

    private final GestionPermisosUseCase permisos;

    RolesController(GestionPermisosUseCase permisos) {
        this.permisos = permisos;
    }

    @GetMapping
    @Operation(operationId = "obtenerMatriz", summary = "Matriz de permisos de cada rol")
    List<PermisosDeRol> matriz() {
        return permisos.obtenerMatriz();
    }

    @PutMapping("/{rol}/permisos")
    @Operation(operationId = "actualizarPermisosRol", summary = "Modificar los permisos de un rol")
    PermisosDeRol actualizar(@PathVariable Rol rol, @Valid @RequestBody PermisosPeticion p) {
        return permisos.actualizarPermisos(rol, p.permisos());
    }

    @GetMapping("/permisos")
    @Operation(summary = "Catálogo de permisos existentes, con su grupo y descripción")
    List<DescripcionPermiso> catalogoPermisos() {
        return Arrays.stream(Permiso.values())
                .map(p -> new DescripcionPermiso(p, p.grupo(), p.descripcion()))
                .toList();
    }

    record PermisosPeticion(@NotNull Set<Permiso> permisos) {
    }

    record DescripcionPermiso(Permiso permiso, String grupo, String descripcion) {
    }
}
