package es.geex.despacho.application.service;

import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.application.PublicadorEventos;
import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.ConflictoEdicionException;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.NoEncontradoException;
import es.geex.compartido.domain.Permiso;
import es.geex.despacho.application.port.in.ConsultaDespachos;
import es.geex.despacho.application.port.in.CrearDespachoUseCase;
import es.geex.despacho.application.port.in.GestionarDespachoUseCase;
import es.geex.despacho.application.port.out.DespachoRepository;
import es.geex.despacho.domain.DatosDespacho;
import es.geex.despacho.domain.Despacho;
import es.geex.despacho.domain.evento.DatosDespachoActualizados;
import es.geex.despacho.domain.evento.DespachoCreado;
import java.time.Clock;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class DespachoService implements CrearDespachoUseCase, GestionarDespachoUseCase, ConsultaDespachos {

    private final DespachoRepository despachos;
    private final ContextoUsuario contexto;
    private final PublicadorEventos eventos;
    private final Clock reloj;

    public DespachoService(DespachoRepository despachos, ContextoUsuario contexto, PublicadorEventos eventos, Clock reloj) {
        this.despachos = despachos;
        this.contexto = contexto;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    @Override
    public DespachoId crear(NuevoDespacho datos) {
        var despacho = Despacho.crear(
                DatosDespacho.minimos(datos.nombre(), datos.municipio(), datos.provincia(), datos.comunidadAutonoma()),
                reloj.instant());
        despachos.guardar(despacho);
        eventos.publicar(new DespachoCreado(despacho.id(), despacho.datos().nombre(), despacho.datos().municipio(),
                despacho.datos().provincia(), despacho.comunidadAutonoma(), despacho.creadoEn()));
        return despacho.id();
    }

    @Override
    public DespachoVista obtenerActual() {
        return aVista(cargar(contexto.despachoId()));
    }

    @Override
    public DespachoVista actualizar(DatosDespachoComando c) {
        contexto.exigir(Permiso.CONFIGURACION_GESTIONAR);
        var despacho = cargar(contexto.despachoId());
        if (despacho.version() != c.version()) {
            throw new ConflictoEdicionException();
        }
        despacho.actualizar(new DatosDespacho(c.nombre(), c.razonSocial(), c.nif(), c.direccion(), c.codigoPostal(),
                c.municipio(), c.provincia(), c.comunidadAutonoma(), c.telefono(), c.email(), c.web()), reloj.instant());
        despachos.guardar(despacho);
        eventos.publicar(new DatosDespachoActualizados(despacho.id(), contexto.usuarioId(), reloj.instant()));
        return aVista(cargar(despacho.id())); // con la versión nueva, para la siguiente edición
    }

    @Override
    public boolean existeAlguno() {
        return despachos.existeAlguno();
    }

    @Override
    public ComunidadAutonoma obtenerComunidadAutonoma(DespachoId despacho) {
        return cargar(despacho).comunidadAutonoma();
    }

    @Override
    public Map<DespachoId, String> obtenerNombres(Collection<DespachoId> ids) {
        return despachos.buscarVarios(ids).stream()
                .collect(Collectors.toMap(Despacho::id, d -> d.datos().nombre()));
    }

    private Despacho cargar(DespachoId id) {
        return despachos.buscar(id)
                .orElseThrow(() -> new NoEncontradoException("DESPACHO_NO_ENCONTRADO", "Despacho no encontrado"));
    }

    private static DespachoVista aVista(Despacho d) {
        var x = d.datos();
        return new DespachoVista(d.id().toString(), x.nombre(), x.razonSocial(), x.nif(), x.direccion(), x.codigoPostal(),
                x.municipio(), x.provincia(), x.comunidadAutonoma(), x.telefono(), x.email(), x.web(),
                d.creadoEn(), d.actualizadoEn(), d.version());
    }
}
