package es.geex.identidad.application.service;

import es.geex.compartido.application.PublicadorEventos;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.ReglaNegocioException;
import es.geex.compartido.domain.Rol;
import es.geex.identidad.application.port.in.InstalacionUseCase;
import es.geex.identidad.application.port.out.CerrojoInstalacion;
import es.geex.identidad.application.port.out.CifradorContrasenas;
import es.geex.identidad.application.port.out.DespachosPort;
import es.geex.identidad.application.port.out.MembresiaRepository;
import es.geex.identidad.application.port.out.UsuarioRepository;
import es.geex.identidad.domain.Membresia;
import es.geex.identidad.domain.PoliticaContrasenas;
import es.geex.identidad.domain.Usuario;
import es.geex.identidad.domain.evento.EventosIdentidad.InstalacionCompletada;
import java.time.Clock;
import java.time.Instant;

public class InstalacionService implements InstalacionUseCase {

    private final DespachosPort despachos;
    private final UsuarioRepository usuarios;
    private final MembresiaRepository membresias;
    private final CifradorContrasenas cifrador;
    private final CerrojoInstalacion cerrojo;
    private final PublicadorEventos eventos;
    private final Clock reloj;

    public InstalacionService(DespachosPort despachos, UsuarioRepository usuarios, MembresiaRepository membresias,
                              CifradorContrasenas cifrador, CerrojoInstalacion cerrojo, PublicadorEventos eventos,
                              Clock reloj) {
        this.despachos = despachos;
        this.usuarios = usuarios;
        this.membresias = membresias;
        this.cifrador = cifrador;
        this.cerrojo = cerrojo;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    @Override
    public boolean consultarSiInstalado() {
        return despachos.existeAlguno();
    }

    @Override
    public void instalar(DatosInstalacion d) {
        cerrojo.adquirir();
        if (despachos.existeAlguno()) {
            throw new ReglaNegocioException("YA_INSTALADO", "GEEX ya está instalado. Inicia sesión.");
        }
        PoliticaContrasenas.validar(d.contrasena());
        Instant ahora = reloj.instant();

        DespachoId despacho = despachos.crear(d.nombreDespacho(), d.municipio(), d.provincia(), d.comunidadAutonoma());
        Usuario administrador = Usuario.conContrasena(d.email(), d.nombre(), d.apellidos(),
                cifrador.cifrar(d.contrasena()), true, ahora);
        usuarios.guardar(administrador);
        membresias.guardar(Membresia.nueva(administrador.id(), despacho, Rol.TITULAR, ahora));
        eventos.publicar(new InstalacionCompletada(despacho, administrador.id(), administrador.email(), ahora));
    }
}
