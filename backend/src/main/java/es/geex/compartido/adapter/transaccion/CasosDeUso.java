package es.geex.compartido.adapter.transaccion;

import java.util.List;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Envuelve los casos de uso (clases Java planas de la capa de aplicación) en un
 * proxy transaccional, desde la configuración de cada módulo.
 *
 * <p>Así la capa de aplicación no necesita anotaciones de Spring: cada método del
 * puerto de entrada es una transacción. Los métodos de consulta
 * ({@code consultar*}, {@code listar*}, {@code obtener*}, {@code buscar*}) son de
 * solo lectura.
 */
public final class CasosDeUso {

    private CasosDeUso() {
    }

    public static <T> T transaccional(Class<T> puerto, T implementacion, PlatformTransactionManager gestor) {
        var escritura = new RuleBasedTransactionAttribute();
        escritura.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        escritura.setRollbackRules(List.of(new RollbackRuleAttribute(Exception.class)));

        var lectura = new RuleBasedTransactionAttribute(escritura);
        lectura.setReadOnly(true);

        var atributos = new NameMatchTransactionAttributeSource();
        atributos.addTransactionalMethod("*", escritura);
        for (String prefijo : List.of("consultar", "listar", "obtener", "buscar")) {
            atributos.addTransactionalMethod(prefijo + "*", lectura);
        }

        var fabrica = new ProxyFactory(implementacion);
        fabrica.setInterfaces(puerto);
        fabrica.addAdvice(new TransactionInterceptor((TransactionManager) gestor, atributos));
        return puerto.cast(fabrica.getProxy());
    }
}
