package es.geex.identidad.application.service;

import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.application.PublicadorEventos;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.application.port.in.ConsultaUsuarios;
import es.geex.identidad.application.port.in.GestionPermisosUseCase;
import es.geex.identidad.application.port.in.InicializarDespachoUseCase;
import es.geex.identidad.application.port.out.MatrizPermisosRepository;
import es.geex.identidad.application.port.out.UsuarioRepository;
import es.geex.identidad.domain.MatrizPermisos;
import es.geex.identidad.domain.Usuario;
import es.geex.identidad.domain.evento.EventosIdentidad.PermisosRolActualizados;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GestionPermisosService implements GestionPermisosUseCase, ConsultaUsuarios, InicializarDespachoUseCase {

    private final MatrizPermisosRepository matrices;
    private final UsuarioRepository usuarios;
    private final ContextoUsuario contexto;
    private final PublicadorEventos eventos;
    private final Clock reloj;

    public GestionPermisosService(MatrizPermisosRepository matrices, UsuarioRepository usuarios,
                                  ContextoUsuario contexto, PublicadorEventos eventos, Clock reloj) {
        this.matrices = matrices;
        this.usuarios = usuarios;
        this.contexto = contexto;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    @Override
    public List<PermisosDeRol> obtenerMatriz() {
        contexto.exigir(Permiso.PERMISOS_GESTIONAR);
        MatrizPermisos matriz = matrices.obtener(contexto.despachoId());
        return Arrays.stream(Rol.values()).map(rol -> aVista(rol, matriz)).toList();
    }

    @Override
    public PermisosDeRol actualizarPermisos(Rol rol, Set<Permiso> permisos) {
        contexto.exigir(Permiso.PERMISOS_GESTIONAR);
        MatrizPermisos matriz = matrices.obtener(contexto.despachoId());
        matriz.asignar(rol, permisos);
        matrices.guardar(matriz);
        eventos.publicar(new PermisosRolActualizados(contexto.despachoId(), contexto.usuarioId(), rol, reloj.instant()));
        return aVista(rol, matriz);
    }

    @Override
    public Map<UsuarioId, String> obtenerNombres(Collection<UsuarioId> ids) {
        return usuarios.buscarVarios(ids).stream().collect(Collectors.toMap(Usuario::id, Usuario::nombreCompleto));
    }

    @Override
    public Set<Permiso> obtenerPermisos(DespachoId despacho, Rol rol) {
        return matrices.obtener(despacho).permisosDe(rol);
    }

    @Override
    public void inicializar(DespachoId despacho) {
        matrices.guardar(MatrizPermisos.predeterminada(despacho));
    }

    private static PermisosDeRol aVista(Rol rol, MatrizPermisos matriz) {
        return new PermisosDeRol(rol, rol.etiqueta(), rol.descripcion(), rol.permisosEditables(), matriz.permisosDe(rol));
    }
}
