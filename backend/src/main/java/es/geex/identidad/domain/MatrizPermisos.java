package es.geex.identidad.domain;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.ReglaNegocioException;
import es.geex.compartido.domain.Rol;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Permisos de cada rol en un despacho. El titular siempre tiene todos. */
public class MatrizPermisos {

    private final DespachoId despachoId;
    private final Map<Rol, Set<Permiso>> permisos;

    private MatrizPermisos(DespachoId despachoId, Map<Rol, Set<Permiso>> permisos) {
        this.despachoId = Objects.requireNonNull(despachoId);
        this.permisos = new EnumMap<>(Rol.class);
        for (Rol rol : Rol.values()) {
            Set<Permiso> conjunto = permisos.getOrDefault(rol, Set.of());
            this.permisos.put(rol, conjunto.isEmpty() ? EnumSet.noneOf(Permiso.class) : EnumSet.copyOf(conjunto));
        }
    }

    public static MatrizPermisos predeterminada(DespachoId despacho) {
        Map<Rol, Set<Permiso>> mapa = new EnumMap<>(Rol.class);
        for (Rol rol : Rol.values()) {
            mapa.put(rol, rol.permisosPredeterminados());
        }
        return new MatrizPermisos(despacho, mapa);
    }

    public static MatrizPermisos reconstruir(DespachoId despacho, Map<Rol, Set<Permiso>> permisos) {
        return new MatrizPermisos(despacho, permisos);
    }

    public Set<Permiso> permisosDe(Rol rol) {
        if (rol == Rol.TITULAR) {
            return Collections.unmodifiableSet(EnumSet.allOf(Permiso.class));
        }
        return Collections.unmodifiableSet(permisos.get(rol));
    }

    public void asignar(Rol rol, Set<Permiso> nuevos) {
        if (!rol.permisosEditables()) {
            throw new ReglaNegocioException("PERMISOS_TITULAR_FIJOS",
                    "Los permisos del titular no se pueden modificar: siempre tiene acceso total");
        }
        permisos.put(rol, nuevos.isEmpty() ? EnumSet.noneOf(Permiso.class) : EnumSet.copyOf(nuevos));
    }

    public DespachoId despachoId() {
        return despachoId;
    }

    public Map<Rol, Set<Permiso>> comoMapa() {
        Map<Rol, Set<Permiso>> copia = new EnumMap<>(Rol.class);
        for (Rol rol : Rol.values()) {
            copia.put(rol, permisosDe(rol));
        }
        return copia;
    }
}
