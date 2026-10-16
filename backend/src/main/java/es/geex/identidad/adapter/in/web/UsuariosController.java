package es.geex.identidad.adapter.in.web;

import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.application.port.in.GestionUsuariosUseCase;
import es.geex.identidad.application.port.in.GestionUsuariosUseCase.Invitacion;
import es.geex.identidad.application.port.in.GestionUsuariosUseCase.UsuarioDespacho;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Personal del despacho")
class UsuariosController {

    private final GestionUsuariosUseCase usuarios;

    UsuariosController(GestionUsuariosUseCase usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    @Operation(operationId = "listarUsuarios", summary = "Usuarios del despacho")
    List<UsuarioDespacho> listar() {
        return usuarios.listarUsuarios();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Invitar a un usuario (recibe un correo para activar su cuenta)")
    UsuarioDespacho invitar(@Valid @RequestBody InvitacionPeticion p) {
        return usuarios.invitar(new Invitacion(p.email(), p.nombre(), p.apellidos(), p.rol()));
    }

    @PostMapping("/{id}/reenviar-invitacion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reenviar el correo de invitación")
    void reenviar(@PathVariable UUID id) {
        usuarios.reenviarInvitacion(new UsuarioId(id));
    }

    @PutMapping("/{id}/rol")
    @Operation(summary = "Cambiar el rol de un usuario")
    UsuarioDespacho cambiarRol(@PathVariable UUID id, @Valid @RequestBody CambioRolPeticion p) {
        return usuarios.cambiarRol(new UsuarioId(id), p.rol());
    }

    @PostMapping("/{id}/desactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar el acceso de un usuario al despacho")
    void desactivar(@PathVariable UUID id) {
        usuarios.desactivar(new UsuarioId(id));
    }

    @PostMapping("/{id}/reactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reactivar el acceso de un usuario al despacho")
    void reactivar(@PathVariable UUID id) {
        usuarios.reactivar(new UsuarioId(id));
    }

    record InvitacionPeticion(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 150) String apellidos,
            @NotNull Rol rol) {
    }

    record CambioRolPeticion(@NotNull Rol rol) {
    }
}
