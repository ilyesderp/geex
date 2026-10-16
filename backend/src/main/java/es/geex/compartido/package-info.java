/**
 * Núcleo compartido: tipos de dominio comunes (identificadores, permisos, roles,
 * excepciones), puertos transversales y la infraestructura técnica común
 * (contexto de ejecución multi-tenant, transacciones, manejo de errores).
 *
 * <p>Es un módulo abierto: todos los demás módulos pueden depender de él.
 */
@ApplicationModule(displayName = "Compartido", type = ApplicationModule.Type.OPEN)
package es.geex.compartido;

import org.springframework.modulith.ApplicationModule;
