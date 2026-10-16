package es.geex.identidad.application.port.out;

/** Exclusión mutua para que dos instalaciones simultáneas no creen dos despachos iniciales. */
public interface CerrojoInstalacion {

    /** Bloquea hasta el final de la transacción actual. */
    void adquirir();
}
