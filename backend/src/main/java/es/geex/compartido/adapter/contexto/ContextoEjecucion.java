package es.geex.compartido.adapter.contexto;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Contexto de la ejecución actual (usuario, despacho y modo), basado en
 * {@link ScopedValue} de Java 25: inmutable, ligado a un ámbito y compatible con
 * hilos virtuales, sin las fugas típicas de un {@code ThreadLocal}.
 *
 * <p>La fuente de datos lo lee al obtener cada conexión para fijar las variables
 * de sesión que usan las políticas de Row Level Security de PostgreSQL.
 */
public final class ContextoEjecucion {

    /**
     * @param sistema operaciones internas que pueden cruzar despachos (inicio de sesión,
     *                instalación, tareas programadas). Las políticas RLS lo permiten de
     *                forma explícita; nunca se activa a partir de datos de la petición.
     */
    public record Datos(UsuarioId usuarioId, DespachoId despachoId, Rol rol, boolean superadmin, boolean sistema) {

        public static Datos deUsuario(UsuarioId usuarioId, DespachoId despachoId, Rol rol, boolean superadmin) {
            return new Datos(usuarioId, despachoId, rol, superadmin, false);
        }

        static Datos sistemaAnonimo() {
            return new Datos(null, null, null, false, true);
        }

        Datos enModoSistema() {
            return new Datos(usuarioId, despachoId, rol, superadmin, true);
        }
    }

    private static final ScopedValue<Datos> ACTUAL = ScopedValue.newInstance();

    private ContextoEjecucion() {
    }

    public static Optional<Datos> actual() {
        return ACTUAL.isBound() ? Optional.of(ACTUAL.get()) : Optional.empty();
    }

    /** Ejecuta la operación con el contexto indicado. */
    public static <T, X extends Throwable> T con(Datos datos, ScopedValue.CallableOp<T, X> operacion) throws X {
        return ScopedValue.where(ACTUAL, datos).call(operacion);
    }

    /** Ejecuta la operación en modo sistema, conservando el usuario actual si lo hay (para auditoría). */
    public static <T> T comoSistema(Supplier<T> operacion) {
        Datos datos = actual().map(Datos::enModoSistema).orElseGet(Datos::sistemaAnonimo);
        return ScopedValue.where(ACTUAL, datos).call(operacion::get);
    }

    public static void comoSistema(Runnable operacion) {
        comoSistema(() -> {
            operacion.run();
            return null;
        });
    }
}
