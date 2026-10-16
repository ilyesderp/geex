package es.geex.identidad.application.port.in;

/** Activación de cuentas invitadas y gestión de la contraseña propia. */
public interface CuentaUseCase {

    InfoEnlace consultarEnlace(String token);

    void activarCuenta(String token, String contrasena);

    /** Siempre termina sin error, exista o no el correo, para no revelar qué cuentas existen. */
    void solicitarRestablecimiento(String email);

    void restablecerContrasena(String token, String contrasena);

    void cambiarContrasena(String actual, String nueva);

    record InfoEnlace(String tipo, String email, String nombre) {
    }
}
