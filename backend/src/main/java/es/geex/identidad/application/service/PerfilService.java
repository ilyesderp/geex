package es.geex.identidad.application.service;

import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.NoEncontradoException;
import es.geex.identidad.application.port.in.PerfilUseCase;
import es.geex.identidad.application.port.out.DespachosPort;
import es.geex.identidad.application.port.out.MatrizPermisosRepository;
import es.geex.identidad.application.port.out.MembresiaRepository;
import es.geex.identidad.application.port.out.UsuarioRepository;
import es.geex.identidad.domain.Membresia;
import es.geex.identidad.domain.Usuario;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PerfilService implements PerfilUseCase {

    private final UsuarioRepository usuarios;
    private final MembresiaRepository membresias;
    private final MatrizPermisosRepository matrices;
    private final DespachosPort despachos;
    private final ContextoUsuario contexto;

    public PerfilService(UsuarioRepository usuarios, MembresiaRepository membresias, MatrizPermisosRepository matrices,
                         DespachosPort despachos, ContextoUsuario contexto) {
        this.usuarios = usuarios;
        this.membresias = membresias;
        this.matrices = matrices;
        this.despachos = despachos;
        this.contexto = contexto;
    }

    @Override
    public Perfil obtenerPerfil() {
        Usuario usuario = usuarios.buscar(contexto.usuarioId())
                .orElseThrow(() -> new NoEncontradoException("USUARIO_NO_ENCONTRADO", "Usuario no encontrado"));
        List<Membresia> activas = membresias.listarActivasDeUsuario(usuario.id());
        Map<DespachoId, String> nombres = despachos.nombres(activas.stream().map(Membresia::despachoId).toList());

        List<DespachoAcceso> accesos = activas.stream()
                .map(m -> new DespachoAcceso(m.despachoId().toString(), nombres.getOrDefault(m.despachoId(), ""),
                        m.rol(), m.rol().etiqueta()))
                .sorted(Comparator.comparing(DespachoAcceso::nombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
        DespachoAcceso actual = accesos.stream()
                .filter(a -> a.id().equals(contexto.despachoId().toString()))
                .findFirst()
                .orElseThrow(() -> new NoEncontradoException("SIN_ACCESO_DESPACHO", "No tienes acceso a este despacho"));

        return new Perfil(usuario.id().toString(), usuario.email(), usuario.nombre(), usuario.apellidos(),
                usuario.superadmin(), actual, matrices.obtener(contexto.despachoId()).permisosDe(actual.rol()), accesos);
    }
}
