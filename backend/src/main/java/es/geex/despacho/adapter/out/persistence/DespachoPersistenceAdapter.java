package es.geex.despacho.adapter.out.persistence;

import es.geex.compartido.domain.DespachoId;
import es.geex.despacho.application.port.out.DespachoRepository;
import es.geex.despacho.domain.DatosDespacho;
import es.geex.despacho.domain.Despacho;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
class DespachoPersistenceAdapter implements DespachoRepository {

    private final DespachoJpaRepository jpa;

    DespachoPersistenceAdapter(DespachoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Despacho despacho) {
        var entidad = jpa.findById(despacho.id().valor()).orElseGet(() -> {
            var nueva = new DespachoEntity();
            nueva.id = despacho.id().valor();
            nueva.creadoEn = despacho.creadoEn();
            nueva.version = despacho.version();
            return nueva;
        });
        if (entidad.version != despacho.version()) {
            throw new ObjectOptimisticLockingFailureException(DespachoEntity.class, despacho.id().valor());
        }
        var d = despacho.datos();
        entidad.nombre = d.nombre();
        entidad.razonSocial = d.razonSocial();
        entidad.nif = d.nif();
        entidad.direccion = d.direccion();
        entidad.codigoPostal = d.codigoPostal();
        entidad.municipio = d.municipio();
        entidad.provincia = d.provincia();
        entidad.comunidadAutonoma = d.comunidadAutonoma();
        entidad.telefono = d.telefono();
        entidad.email = d.email();
        entidad.web = d.web();
        entidad.actualizadoEn = despacho.actualizadoEn();
        jpa.saveAndFlush(entidad);
    }

    @Override
    public Optional<Despacho> buscar(DespachoId id) {
        return jpa.findById(id.valor()).map(DespachoPersistenceAdapter::aDominio);
    }

    @Override
    public List<Despacho> buscarVarios(Collection<DespachoId> ids) {
        return jpa.findAllById(ids.stream().map(DespachoId::valor).toList()).stream()
                .map(DespachoPersistenceAdapter::aDominio)
                .toList();
    }

    @Override
    public boolean existeAlguno() {
        return jpa.count() > 0;
    }

    private static Despacho aDominio(DespachoEntity e) {
        return Despacho.reconstruir(new DespachoId(e.id),
                new DatosDespacho(e.nombre, e.razonSocial, e.nif, e.direccion, e.codigoPostal, e.municipio,
                        e.provincia, e.comunidadAutonoma, e.telefono, e.email, e.web),
                e.creadoEn, e.actualizadoEn, e.version);
    }
}
