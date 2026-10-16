package es.geex.identidad.domain;

import es.geex.compartido.domain.ReglaNegocioException;
import es.geex.compartido.domain.UsuarioId;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Persona que trabaja en uno o varios despachos. El acceso a cada despacho y el
 * rol que tiene en él se modelan con {@link Membresia}.
 */
public class Usuario {

    /** Intentos fallidos consecutivos antes de bloquear temporalmente la cuenta. */
    public static final int MAX_INTENTOS_FALLIDOS = 5;
    public static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsuarioId id;
    private final String email;
    private String nombre;
    private String apellidos;
    private String hashContrasena;
    private final boolean superadmin;
    private boolean activo;
    private int intentosFallidos;
    private Instant bloqueadoHasta;
    private int versionToken;
    private Instant ultimoAcceso;
    private final Instant creadoEn;
    private final long version;

    private Usuario(UsuarioId id, String email, String nombre, String apellidos, String hashContrasena,
                    boolean superadmin, boolean activo, int intentosFallidos, Instant bloqueadoHasta,
                    int versionToken, Instant ultimoAcceso, Instant creadoEn, long version) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.hashContrasena = hashContrasena;
        this.superadmin = superadmin;
        this.activo = activo;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
        this.versionToken = versionToken;
        this.ultimoAcceso = ultimoAcceso;
        this.creadoEn = creadoEn;
        this.version = version;
    }

    /** Usuario invitado: aún no tiene contraseña; la elige al activar su cuenta. */
    public static Usuario invitado(String email, String nombre, String apellidos, Instant ahora) {
        return new Usuario(UsuarioId.nuevo(), normalizarEmail(email), exigirTexto(nombre, "nombre"),
                exigirTexto(apellidos, "apellidos"), null, false, true, 0, null, 0, null, ahora, 0);
    }

    /** Usuario creado con contraseña (administrador de la instalación inicial). */
    public static Usuario conContrasena(String email, String nombre, String apellidos, String hashContrasena,
                                        boolean superadmin, Instant ahora) {
        return new Usuario(UsuarioId.nuevo(), normalizarEmail(email), exigirTexto(nombre, "nombre"),
                exigirTexto(apellidos, "apellidos"), Objects.requireNonNull(hashContrasena), superadmin, true,
                0, null, 0, null, ahora, 0);
    }

    public static Usuario reconstruir(UsuarioId id, String email, String nombre, String apellidos,
                                      String hashContrasena, boolean superadmin, boolean activo,
                                      int intentosFallidos, Instant bloqueadoHasta, int versionToken,
                                      Instant ultimoAcceso, Instant creadoEn, long version) {
        return new Usuario(id, email, nombre, apellidos, hashContrasena, superadmin, activo, intentosFallidos,
                bloqueadoHasta, versionToken, ultimoAcceso, creadoEn, version);
    }

    public static String normalizarEmail(String email) {
        String limpio = email == null ? "" : email.strip().toLowerCase();
        if (!EMAIL.matcher(limpio).matches() || limpio.length() > 254) {
            throw new ReglaNegocioException("EMAIL_NO_VALIDO", "El correo electrónico no es válido");
        }
        return limpio;
    }

    public boolean pendienteActivacion() {
        return hashContrasena == null;
    }

    public boolean bloqueado(Instant ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    /** Puede iniciar sesión (sin comprobar aún la contraseña). */
    public boolean puedeAcceder(Instant ahora) {
        return activo && !pendienteActivacion() && !bloqueado(ahora);
    }

    public void registrarAccesoFallido(Instant ahora) {
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS_FALLIDOS) {
            bloqueadoHasta = ahora.plus(DURACION_BLOQUEO);
            intentosFallidos = 0;
        }
    }

    public void registrarAccesoCorrecto(Instant ahora) {
        intentosFallidos = 0;
        bloqueadoHasta = null;
        ultimoAcceso = ahora;
    }

    /**
     * Establece una contraseña nueva. Incrementa la versión de token: todas las
     * sesiones abiertas dejan de poder renovarse.
     */
    public void establecerContrasena(String nuevoHash) {
        this.hashContrasena = Objects.requireNonNull(nuevoHash);
        this.versionToken++;
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
    }

    public String nombreCompleto() {
        return nombre + " " + apellidos;
    }

    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("CAMPO_OBLIGATORIO", "El campo " + campo + " es obligatorio");
        }
        return valor.strip();
    }

    public UsuarioId id() { return id; }
    public String email() { return email; }
    public String nombre() { return nombre; }
    public String apellidos() { return apellidos; }
    public String hashContrasena() { return hashContrasena; }
    public boolean superadmin() { return superadmin; }
    public boolean activo() { return activo; }
    public int intentosFallidos() { return intentosFallidos; }
    public Instant bloqueadoHasta() { return bloqueadoHasta; }
    public int versionToken() { return versionToken; }
    public Instant ultimoAcceso() { return ultimoAcceso; }
    public Instant creadoEn() { return creadoEn; }
    public long version() { return version; }
}
