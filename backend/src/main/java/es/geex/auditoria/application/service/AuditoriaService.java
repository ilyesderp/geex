package es.geex.auditoria.application.service;

import es.geex.auditoria.application.port.in.AuditoriaUseCase;
import es.geex.auditoria.application.port.out.NombresUsuariosPort;
import es.geex.auditoria.application.port.out.RegistroAuditoriaRepository;
import es.geex.auditoria.domain.RegistroAuditoria;
import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.domain.EventoAuditable;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AuditoriaService implements AuditoriaUseCase {

    private static final ZoneId MADRID = ZoneId.of("Europe/Madrid");
    private static final int TAMANO_MAXIMO = 200;

    private final RegistroAuditoriaRepository registros;
    private final NombresUsuariosPort nombres;
    private final ContextoUsuario contexto;

    public AuditoriaService(RegistroAuditoriaRepository registros, NombresUsuariosPort nombres,
                            ContextoUsuario contexto) {
        this.registros = registros;
        this.nombres = nombres;
        this.contexto = contexto;
    }

    @Override
    public void registrar(EventoAuditable evento) {
        registros.guardar(RegistroAuditoria.de(evento));
    }

    @Override
    public PaginaAuditoria consultar(FiltroAuditoria f) {
        contexto.exigir(Permiso.AUDITORIA_VER);
        int tamano = Math.clamp(f.tamano(), 1, TAMANO_MAXIMO);
        int pagina = Math.clamp(f.pagina(), 0, Integer.MAX_VALUE / TAMANO_MAXIMO);
        String accion = f.accion() == null || f.accion().isBlank() ? null : f.accion();
        Instant desde = f.desde() == null ? null : f.desde().atStartOfDay(MADRID).toInstant();
        Instant hasta = f.hasta() == null ? null : f.hasta().plusDays(1).atStartOfDay(MADRID).toInstant();

        List<RegistroAuditoria> lista = registros.buscar(contexto.despachoId(), accion, desde, hasta,
                pagina * tamano, tamano);
        Map<UsuarioId, String> nombresActores = nombres.nombres(
                lista.stream().map(RegistroAuditoria::actorId).filter(Objects::nonNull).distinct().toList());
        var entradas = lista.stream()
                .map(r -> new EntradaAuditoria(r.ocurridoEn(),
                        r.actorId() == null ? "Sistema" : nombresActores.getOrDefault(r.actorId(), "Usuario eliminado"),
                        r.accion(), r.descripcion(), r.entidadTipo(), r.entidadId()))
                .toList();
        return new PaginaAuditoria(entradas, registros.contar(contexto.despachoId(), accion, desde, hasta), pagina, tamano);
    }
}
