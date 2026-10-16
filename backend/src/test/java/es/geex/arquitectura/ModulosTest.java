package es.geex.arquitectura;

import es.geex.GeexBackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifica los límites entre módulos con Spring Modulith: un módulo solo puede
 * usar de otro lo que este expone (paquete base o interfaces con nombre como
 * {@code api} y {@code eventos}), y no puede haber dependencias cíclicas.
 */
class ModulosTest {

    private final ApplicationModules modulos = ApplicationModules.of(GeexBackendApplication.class);

    @Test
    void los_limites_entre_modulos_se_respetan() {
        modulos.verify();
    }

    @Test
    void genera_la_documentacion_de_modulos() {
        new Documenter(modulos)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases();
    }
}
