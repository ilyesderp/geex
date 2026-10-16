package es.geex.compartido.application;

/**
 * Puerto de salida para publicar eventos de dominio.
 *
 * <p>Los eventos se guardan en la misma transacción que el cambio que los
 * origina (registro de publicaciones de Spring Modulith), de modo que no se
 * pierden aunque la aplicación se detenga antes de procesarlos.
 */
public interface PublicadorEventos {

    void publicar(Object evento);
}
