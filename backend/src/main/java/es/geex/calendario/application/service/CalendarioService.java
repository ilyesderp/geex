package es.geex.calendario.application.service;

import es.geex.calendario.application.port.in.CalendarioUseCase;
import es.geex.calendario.application.port.in.InicializarCalendarioUseCase;
import es.geex.calendario.application.port.out.ComunidadDespachoPort;
import es.geex.calendario.application.port.out.DiaInhabilRepository;
import es.geex.calendario.domain.AmbitoDia;
import es.geex.calendario.domain.DiaInhabil;
import es.geex.calendario.domain.evento.CalendarioModificado;
import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.application.PublicadorEventos;
import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.NoEncontradoException;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.ReglaNegocioException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.UUID;

public class CalendarioService implements CalendarioUseCase, InicializarCalendarioUseCase {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DiaInhabilRepository dias;
    private final ComunidadDespachoPort comunidades;
    private final ContextoUsuario contexto;
    private final PublicadorEventos eventos;
    private final Clock reloj;

    public CalendarioService(DiaInhabilRepository dias, ComunidadDespachoPort comunidades, ContextoUsuario contexto,
                             PublicadorEventos eventos, Clock reloj) {
        this.dias = dias;
        this.comunidades = comunidades;
        this.contexto = contexto;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    @Override
    public CalendarioAnual obtenerCalendario(int anio) {
        if (anio < 2000 || anio > 2100) {
            throw new ReglaNegocioException("ANIO_NO_VALIDO", "Año no válido");
        }
        DespachoId despacho = contexto.despachoId();
        ComunidadAutonoma comunidad = comunidades.comunidadDe(despacho);
        boolean puedeEditarLocales = contexto.tienePermiso(Permiso.CONFIGURACION_GESTIONAR);
        boolean superadmin = contexto.esSuperadmin();
        var lista = dias.listar(despacho, comunidad, LocalDate.of(anio, 1, 1), LocalDate.of(anio, 12, 31)).stream()
                .sorted(Comparator.comparing(DiaInhabil::fecha))
                .map(d -> aVista(d, d.esGlobal() ? superadmin : puedeEditarLocales))
                .toList();
        return new CalendarioAnual(anio, comunidad, comunidad.nombre(), lista, superadmin);
    }

    @Override
    public DiaInhabilVista anadirFestivoLocal(LocalDate fecha, String descripcion) {
        contexto.exigir(Permiso.CONFIGURACION_GESTIONAR);
        DiaInhabil dia = DiaInhabil.local(contexto.despachoId(), fecha, descripcion);
        guardarSinDuplicar(dia);
        publicar("Añadido festivo local " + FECHA.format(fecha) + " (" + dia.descripcion() + ")", dia.id());
        return aVista(dia, true);
    }

    @Override
    public void eliminarFestivoLocal(UUID id) {
        contexto.exigir(Permiso.CONFIGURACION_GESTIONAR);
        DiaInhabil dia = dias.buscar(id)
                .filter(d -> d.ambito() == AmbitoDia.LOCAL && contexto.despachoId().equals(d.despachoId()))
                .orElseThrow(() -> new NoEncontradoException("FESTIVO_NO_ENCONTRADO", "Festivo no encontrado"));
        dias.eliminar(id);
        publicar("Eliminado festivo local " + FECHA.format(dia.fecha()) + " (" + dia.descripcion() + ")", id);
    }

    @Override
    public DiaInhabilVista anadirFestivoGlobal(AmbitoDia ambito, ComunidadAutonoma comunidad, LocalDate fecha,
                                               String descripcion, boolean provisional) {
        contexto.exigirSuperadmin();
        DiaInhabil dia = DiaInhabil.global(ambito, comunidad, fecha, descripcion, provisional);
        guardarSinDuplicar(dia);
        publicar("Añadido festivo " + ambito.etiqueta().toLowerCase()
                + (comunidad == null || ambito == AmbitoDia.NACIONAL ? "" : " de " + comunidad.nombre())
                + " " + FECHA.format(fecha) + " (" + dia.descripcion() + ")", dia.id());
        return aVista(dia, true);
    }

    @Override
    public void eliminarFestivoGlobal(UUID id) {
        contexto.exigirSuperadmin();
        DiaInhabil dia = dias.buscar(id).filter(DiaInhabil::esGlobal)
                .orElseThrow(() -> new NoEncontradoException("FESTIVO_NO_ENCONTRADO", "Festivo no encontrado"));
        dias.eliminar(id);
        publicar("Eliminado festivo " + dia.ambito().etiqueta().toLowerCase() + " " + FECHA.format(dia.fecha())
                + " (" + dia.descripcion() + ")", id);
    }

    @Override
    public void inicializar(DespachoId despacho, String municipio, String provincia) {
        for (var festivo : dias.festivosLocalesConocidos(municipio, provincia)) {
            DiaInhabil dia = DiaInhabil.local(despacho, festivo.fecha(), festivo.descripcion());
            if (!dias.existe(dia)) {
                dias.guardar(dia);
            }
        }
    }

    private void guardarSinDuplicar(DiaInhabil dia) {
        if (dias.existe(dia)) {
            throw new ReglaNegocioException("FESTIVO_DUPLICADO", "Ese día ya figura como inhábil");
        }
        dias.guardar(dia);
    }

    private void publicar(String descripcion, UUID id) {
        eventos.publicar(new CalendarioModificado(contexto.despachoId(), contexto.usuarioId(), descripcion,
                id.toString(), reloj.instant()));
    }

    private static DiaInhabilVista aVista(DiaInhabil d, boolean editable) {
        return new DiaInhabilVista(d.id(), d.fecha(), d.ambito(), d.ambito().etiqueta(), d.comunidad(),
                d.descripcion(), d.provisional(), editable);
    }
}
