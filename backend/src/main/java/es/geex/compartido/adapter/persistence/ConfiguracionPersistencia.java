package es.geex.compartido.adapter.persistence;

import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class ConfiguracionPersistencia {

    /** Envuelve el DataSource principal (el pool de Hikari) con {@link DataSourceMultiDespacho}. */
    @Bean
    static BeanPostProcessor envolverDataSourceMultiDespacho() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String nombre) {
                if (bean instanceof DataSource ds
                        && !(bean instanceof DataSourceMultiDespacho)
                        && "dataSource".equals(nombre)) {
                    return new DataSourceMultiDespacho(ds);
                }
                return bean;
            }
        };
    }
}
