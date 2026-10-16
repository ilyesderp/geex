package es.geex.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Reglas de la arquitectura hexagonal. Si alguien las rompe, el build falla.
 *
 * <ul>
 *   <li>El dominio es Java puro: sin frameworks, sin aplicación, sin adaptadores.</li>
 *   <li>La aplicación (casos de uso y puertos) no conoce Spring, JPA, la web ni los adaptadores.</li>
 *   <li>Solo los adaptadores dependen de la tecnología.</li>
 * </ul>
 */
@AnalyzeClasses(packages = "es.geex", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaHexagonalTest {

    private static final String[] TECNOLOGIA = {
            "org.springframework..", "jakarta.persistence..", "jakarta.servlet..", "jakarta.validation..",
            "org.hibernate..", "com.fasterxml..", "tools.jackson..", "io.swagger..", "java.sql.."
    };

    /**
     * Los {@code package-info} solo llevan metadatos de módulo de Spring Modulith
     * ({@code @NamedInterface}); no son código ejecutable, así que se excluyen.
     */
    private static final String METADATOS_DE_PAQUETE = "package-info";

    @ArchTest
    static final ArchRule el_dominio_es_java_puro = noClasses()
            .that().resideInAPackage("..domain..")
            .and().doNotHaveSimpleName(METADATOS_DE_PAQUETE)
            .should().dependOnClassesThat().resideInAnyPackage(TECNOLOGIA)
            .because("el dominio no debe depender de ningún framework");

    @ArchTest
    static final ArchRule el_dominio_no_depende_de_capas_exteriores = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..adapter..");

    @ArchTest
    static final ArchRule la_aplicacion_no_depende_de_la_tecnologia = noClasses()
            .that().resideInAPackage("..application..")
            .and().doNotHaveSimpleName(METADATOS_DE_PAQUETE)
            .should().dependOnClassesThat().resideInAnyPackage(TECNOLOGIA)
            .because("los casos de uso son Java plano; la tecnología vive en los adaptadores");

    @ArchTest
    static final ArchRule la_aplicacion_no_depende_de_adaptadores = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..");

    @ArchTest
    static final ArchRule los_puertos_son_interfaces = classes()
            .that().resideInAPackage("..application.port.out..")
            .and().areTopLevelClasses()
            .should().beInterfaces();

    @ArchTest
    static final ArchRule los_controladores_viven_en_adaptadores_web = classes()
            .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().resideInAPackage("..adapter.in.web..");

    @ArchTest
    static final ArchRule las_entidades_jpa_viven_en_adaptadores_de_persistencia = classes()
            .that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().resideInAPackage("..adapter.out.persistence..")
            .andShould().notBePublic();
}
