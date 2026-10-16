package es.geex.configuracion.application.service;

import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.application.PublicadorEventos;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.NoEncontradoException;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.ReglaNegocioException;
import es.geex.configuracion.application.port.in.CatalogosUseCase;
import es.geex.configuracion.application.port.in.InicializarCatalogosUseCase;
import es.geex.configuracion.application.port.out.ValorCatalogoRepository;
import es.geex.configuracion.domain.CodigoCatalogo;
import es.geex.configuracion.domain.TipoCatalogo;
import es.geex.configuracion.domain.ValorCatalogo;
import es.geex.configuracion.domain.evento.ListaDesplegableModificada;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CatalogosService implements CatalogosUseCase, InicializarCatalogosUseCase {

    private final ValorCatalogoRepository valores;
    private final ContextoUsuario contexto;
    private final PublicadorEventos eventos;
    private final Clock reloj;

    public CatalogosService(ValorCatalogoRepository valores, ContextoUsuario contexto, PublicadorEventos eventos,
                            Clock reloj) {
        this.valores = valores;
        this.contexto = contexto;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    @Override
    public List<ResumenCatalogo> listarCatalogos() {
        Map<TipoCatalogo, long[]> conteos = valores.contarPorTipo(contexto.despachoId());
        return Arrays.stream(TipoCatalogo.values())
                .map(t -> {
                    long[] c = conteos.getOrDefault(t, new long[]{0, 0});
                    return new ResumenCatalogo(t, t.nombre(), t.grupo(), t.descripcion(), c[0], c[1]);
                })
                .toList();
    }

    @Override
    public List<ValorVista> listarValores(TipoCatalogo tipo, boolean incluirInactivos) {
        return valores.listar(contexto.despachoId(), tipo).stream()
                .filter(v -> incluirInactivos || v.activo())
                .map(CatalogosService::aVista)
                .toList();
    }

    @Override
    public ValorVista anadirValor(TipoCatalogo tipo, String etiqueta) {
        contexto.exigir(Permiso.CONFIGURACION_GESTIONAR);
        List<ValorCatalogo> existentes = valores.listar(contexto.despachoId(), tipo);
        String codigo = CodigoCatalogo.desdeEtiqueta(etiqueta);
        if (existentes.stream().anyMatch(v -> v.codigo().equals(codigo))) {
            throw new ReglaNegocioException("VALOR_DUPLICADO", "Ya existe un valor igual en esta lista");
        }
        int orden = existentes.stream().mapToInt(ValorCatalogo::orden).max().orElse(0) + 1;
        ValorCatalogo nuevo = ValorCatalogo.personalizado(contexto.despachoId(), tipo, etiqueta, orden);
        valores.guardar(nuevo);
        publicar(tipo, "añadido \"" + nuevo.etiqueta() + "\"");
        return aVista(nuevo);
    }

    @Override
    public ValorVista modificarValor(TipoCatalogo tipo, UUID id, String etiqueta, boolean activo) {
        contexto.exigir(Permiso.CONFIGURACION_GESTIONAR);
        ValorCatalogo valor = cargar(tipo, id);
        String anterior = valor.etiqueta();
        valor.renombrar(etiqueta);
        valor.cambiarActivo(activo);
        valores.guardar(valor);
        publicar(tipo, "modificado \"" + anterior + "\"" + (anterior.equals(valor.etiqueta()) ? "" : " → \"" + valor.etiqueta() + "\"")
                + (activo ? "" : " (desactivado)"));
        return aVista(valor);
    }

    @Override
    public List<ValorVista> reordenar(TipoCatalogo tipo, List<UUID> idsEnOrden) {
        contexto.exigir(Permiso.CONFIGURACION_GESTIONAR);
        Map<UUID, ValorCatalogo> porId = valores.listar(contexto.despachoId(), tipo).stream()
                .collect(Collectors.toMap(ValorCatalogo::id, Function.identity()));
        if (idsEnOrden.size() != porId.size() || !porId.keySet().equals(new HashSet<>(idsEnOrden))) {
            throw new ReglaNegocioException("ORDEN_INCOMPLETO", "El nuevo orden debe incluir todos los valores de la lista");
        }
        List<ValorCatalogo> ordenados = new ArrayList<>();
        for (int i = 0; i < idsEnOrden.size(); i++) {
            ValorCatalogo valor = porId.get(idsEnOrden.get(i));
            valor.moverA(i + 1);
            ordenados.add(valor);
        }
        valores.guardarTodos(ordenados);
        publicar(tipo, "reordenada");
        return ordenados.stream().map(CatalogosService::aVista).toList();
    }

    @Override
    public void eliminarValor(TipoCatalogo tipo, UUID id) {
        contexto.exigir(Permiso.CONFIGURACION_GESTIONAR);
        ValorCatalogo valor = cargar(tipo, id);
        valor.comprobarQueSePuedeEliminar();
        valores.eliminar(valor);
        publicar(tipo, "eliminado \"" + valor.etiqueta() + "\"");
    }

    @Override
    public void inicializar(DespachoId despacho) {
        List<ValorCatalogo> iniciales = new ArrayList<>();
        for (TipoCatalogo tipo : TipoCatalogo.values()) {
            int orden = 1;
            for (TipoCatalogo.ValorInicial v : tipo.valoresIniciales()) {
                iniciales.add(ValorCatalogo.inicial(despacho, tipo, v.codigo(), v.etiqueta(), orden++));
            }
        }
        valores.guardarTodos(iniciales);
    }

    private ValorCatalogo cargar(TipoCatalogo tipo, UUID id) {
        return valores.buscar(contexto.despachoId(), id)
                .filter(v -> v.tipo() == tipo)
                .orElseThrow(() -> new NoEncontradoException("VALOR_NO_ENCONTRADO", "Valor no encontrado en esta lista"));
    }

    private void publicar(TipoCatalogo tipo, String cambio) {
        eventos.publicar(new ListaDesplegableModificada(contexto.despachoId(), contexto.usuarioId(), tipo, cambio,
                reloj.instant()));
    }

    private static ValorVista aVista(ValorCatalogo v) {
        return new ValorVista(v.id(), v.codigo(), v.etiqueta(), v.orden(), v.activo(), v.sistema());
    }
}
