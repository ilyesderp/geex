package es.geex.configuracion.application.port.in;

import es.geex.configuracion.domain.TipoCatalogo;
import java.util.List;
import java.util.UUID;

/** Listas desplegables del despacho (Configuración → Listas desplegables). */
public interface CatalogosUseCase {

    List<ResumenCatalogo> listarCatalogos();

    List<ValorVista> listarValores(TipoCatalogo tipo, boolean incluirInactivos);

    ValorVista anadirValor(TipoCatalogo tipo, String etiqueta);

    ValorVista modificarValor(TipoCatalogo tipo, UUID id, String etiqueta, boolean activo);

    List<ValorVista> reordenar(TipoCatalogo tipo, List<UUID> idsEnOrden);

    void eliminarValor(TipoCatalogo tipo, UUID id);

    record ResumenCatalogo(TipoCatalogo tipo, String nombre, String grupo, String descripcion, long valores,
                           long activos) {
    }

    record ValorVista(UUID id, String codigo, String etiqueta, int orden, boolean activo, boolean sistema) {
    }
}
