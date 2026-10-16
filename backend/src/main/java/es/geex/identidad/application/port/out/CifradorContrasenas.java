package es.geex.identidad.application.port.out;

public interface CifradorContrasenas {

    String cifrar(String contrasena);

    boolean coincide(String contrasena, String hash);
}
