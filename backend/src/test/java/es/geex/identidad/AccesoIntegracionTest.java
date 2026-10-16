package es.geex.identidad;

import static org.assertj.core.api.Assertions.assertThat;

import es.geex.soporte.PruebaIntegracion;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Recorridos completos de acceso, invitación, permisos y bloqueo, por HTTP y contra PostgreSQL real. */
class AccesoIntegracionTest extends PruebaIntegracion {

    private Credenciales titular;
    private Sesion sesionTitular;

    @BeforeEach
    void preparar() {
        reiniciarBaseDeDatos();
        titular = instalar("Despacho Pruebas");
        sesionTitular = iniciarSesion(titular);
    }

    @Test
    void tras_instalar_no_se_puede_volver_a_instalar() {
        assertThat(get("/api/instalacion", null).json().get("instalado").asBoolean()).isTrue();

        var otraVez = post("/api/instalacion", null, """
                {"nombreDespacho":"Otro","municipio":"Madrid","provincia":"Madrid","comunidadAutonoma":"MADRID",
                 "nombre":"X","apellidos":"Y","email":"otro@despacho.test","contrasena":"%s"}""".formatted(contrasenaNueva()));
        assertThat(otraVez.estado()).isEqualTo(422);
        assertThat(otraVez.codigoError()).isEqualTo("YA_INSTALADO");
    }

    @Test
    void el_titular_ve_su_perfil_con_todos_los_permisos() {
        var yo = get("/api/yo", sesionTitular.token());

        assertThat(yo.estado()).isEqualTo(200);
        assertThat(yo.json().get("email").asString()).isEqualTo(titular.email());
        assertThat(yo.json().get("despachoActual").get("rol").asString()).isEqualTo("TITULAR");
        assertThat(yo.json().get("permisos").size()).isEqualTo(es.geex.compartido.domain.Permiso.values().length);
    }

    @Test
    void sin_token_o_con_token_manipulado_no_hay_acceso() {
        assertThat(get("/api/yo", null).estado()).isEqualTo(401);
        String manipulado = sesionTitular.token().substring(0, sesionTitular.token().length() - 4) + "AAAA";
        assertThat(get("/api/yo", manipulado).estado()).isEqualTo(401);
    }

    @Test
    void invitacion_activacion_y_permisos_del_gestor() {
        var invitacion = post("/api/usuarios", sesionTitular.token(),
                "{\"email\":\"Gestor@Despacho.test\",\"nombre\":\"Luis\",\"apellidos\":\"Gestor\",\"rol\":\"GESTOR\"}");
        assertThat(invitacion.estado()).isEqualTo(201);
        assertThat(invitacion.json().get("estado").asString()).isEqualTo("PENDIENTE_ACTIVACION");

        String token = correos.ultimoToken("INVITACION", "gestor@despacho.test");
        var enlace = post("/api/cuenta/enlaces/consultar", null, "{\"token\":\"" + token + "\"}");
        assertThat(enlace.json().get("email").asString()).isEqualTo("gestor@despacho.test");

        String contrasena = contrasenaNueva();
        assertThat(post("/api/cuenta/activar", null,
                "{\"token\":\"%s\",\"contrasena\":\"%s\"}".formatted(token, contrasena)).estado()).isEqualTo(204);
        assertThat(post("/api/cuenta/activar", null,
                "{\"token\":\"%s\",\"contrasena\":\"%s\"}".formatted(token, contrasena)).codigoError())
                .as("el enlace es de un solo uso").isEqualTo("ENLACE_NO_VALIDO");

        Sesion gestor = iniciarSesion(new Credenciales("gestor@despacho.test", contrasena));
        var usuarios = get("/api/usuarios", gestor.token());
        assertThat(usuarios.estado()).isEqualTo(403);
        assertThat(usuarios.codigoError()).isEqualTo("ACCESO_DENEGADO");

        // El titular concede el permiso al rol Gestor: surte efecto de inmediato (la caché se invalida)
        var matriz = get("/api/roles", sesionTitular.token()).json();
        var permisosGestor = new java.util.ArrayList<String>();
        for (var rol : matriz) {
            if (rol.get("rol").asString().equals("GESTOR")) {
                rol.get("permisos").forEach(p -> permisosGestor.add("\"" + p.asString() + "\""));
            }
        }
        permisosGestor.add("\"USUARIOS_GESTIONAR\"");
        assertThat(put("/api/roles/GESTOR/permisos", sesionTitular.token(),
                "{\"permisos\":" + permisosGestor + "}").estado()).isEqualTo(200);
        assertThat(get("/api/usuarios", gestor.token()).estado()).isEqualTo(200);
    }

    @Test
    void el_despacho_nunca_se_queda_sin_titular() {
        String idTitular = get("/api/yo", sesionTitular.token()).json().get("usuarioId").asString();

        var degradarse = put("/api/usuarios/" + idTitular + "/rol", sesionTitular.token(), "{\"rol\":\"GESTOR\"}");
        assertThat(degradarse.estado()).isEqualTo(422);
        assertThat(degradarse.codigoError()).isEqualTo("NO_AUTOCAMBIO_ROL");

        var desactivarse = post("/api/usuarios/" + idTitular + "/desactivar", sesionTitular.token(), null);
        assertThat(desactivarse.codigoError()).isEqualTo("NO_AUTODESACTIVAR");
    }

    @Test
    void quien_gestiona_usuarios_sin_ser_titular_no_puede_hacerse_con_el_despacho() {
        // El titular da a los gestores la gestión de usuarios y de permisos
        concederAlGestor("USUARIOS_GESTIONAR", "PERMISOS_GESTIONAR");
        Sesion gestor = altaUsuario(sesionTitular, "GESTOR");
        String idGestor = usuarioId(gestor);
        String idTitular = usuarioId(sesionTitular);

        var ascenderse = put("/api/usuarios/" + idGestor + "/rol", gestor.token(), "{\"rol\":\"TITULAR\"}");
        assertThat(ascenderse.codigoError()).isEqualTo("NO_AUTOCAMBIO_ROL");

        var invitarTitular = post("/api/usuarios", gestor.token(),
                "{\"email\":\"complice@despacho.test\",\"nombre\":\"C\",\"apellidos\":\"C\",\"rol\":\"TITULAR\"}");
        assertThat(invitarTitular.estado()).isEqualTo(403);
        assertThat(invitarTitular.codigoError()).isEqualTo("SOLO_TITULAR");

        var degradarTitular = put("/api/usuarios/" + idTitular + "/rol", gestor.token(), "{\"rol\":\"GESTOR\"}");
        assertThat(degradarTitular.codigoError()).isEqualTo("SOLO_TITULAR");

        var desactivarTitular = post("/api/usuarios/" + idTitular + "/desactivar", gestor.token(), null);
        assertThat(desactivarTitular.codigoError()).isEqualTo("SOLO_TITULAR");

        var autoPermisos = put("/api/roles/GESTOR/permisos", gestor.token(), "{\"permisos\":[\"AUDITORIA_VER\"]}");
        assertThat(autoPermisos.estado()).isEqualTo(403);
        assertThat(autoPermisos.codigoError()).isEqualTo("NO_AUTOPERMISOS");

        // Lo que sí puede hacer: gestionar al resto del personal
        Sesion administrativo = altaUsuario(gestor, "ADMINISTRATIVO");
        assertThat(put("/api/usuarios/" + usuarioId(administrativo) + "/rol", gestor.token(),
                "{\"rol\":\"ABOGADO\"}").estado()).isEqualTo(200);
    }

    @Test
    void tras_cinco_intentos_fallidos_la_cuenta_se_bloquea_aunque_luego_acierte() {
        for (int i = 0; i < 5; i++) {
            var fallo = post("/api/auth/login", null,
                    "{\"email\":\"%s\",\"contrasena\":\"incorrecta-%d\"}".formatted(titular.email(), i));
            assertThat(fallo.codigoError()).isEqualTo("CREDENCIALES_INVALIDAS");
        }
        var correcta = post("/api/auth/login", null,
                "{\"email\":\"%s\",\"contrasena\":\"%s\"}".formatted(titular.email(), titular.contrasena()));
        assertThat(correcta.estado()).isEqualTo(401);
        assertThat(correcta.codigoError()).isEqualTo("CUENTA_BLOQUEADA");
    }

    @Test
    void restablecer_la_contrasena_cierra_las_sesiones_abiertas() {
        assertThat(post("/api/cuenta/recuperar", null, "{\"email\":\"" + titular.email() + "\"}").estado()).isEqualTo(202);
        assertThat(post("/api/cuenta/recuperar", null, "{\"email\":\"no-existe@despacho.test\"}").estado())
                .as("no revela si el correo existe").isEqualTo(202);

        String token = correos.ultimoToken("RESTABLECIMIENTO", titular.email());
        String nueva = contrasenaNueva();
        assertThat(post("/api/cuenta/restablecer", null,
                "{\"token\":\"%s\",\"contrasena\":\"%s\"}".formatted(token, nueva)).estado()).isEqualTo(204);

        assertThat(refrescar(sesionTitular.cookieRefresco()).estado()).as("la sesión anterior ya no se renueva").isEqualTo(401);
        iniciarSesion(new Credenciales(titular.email(), nueva));
    }

    @Test
    void una_peticion_con_la_cookie_de_refresco_sin_la_cabecera_propia_se_rechaza() {
        var cookie = iniciarSesion(titular).cookieRefresco();
        var sinCabecera = post("/api/auth/logout", null, null, cookie);
        assertThat(sinCabecera.estado()).as("posible CSRF: falta X-GEEX-Refresco").isEqualTo(403);
        assertThat(sinCabecera.codigoError()).isEqualTo("CABECERA_REQUERIDA");
        assertThat(refrescar(cookie).estado()).as("la sesión sigue abierta").isEqualTo(200);
    }

    @Test
    void la_actividad_queda_en_la_auditoria() {
        post("/api/usuarios", sesionTitular.token(),
                "{\"email\":\"aud@despacho.test\",\"nombre\":\"Eva\",\"apellidos\":\"Admin\",\"rol\":\"ADMINISTRATIVO\"}");

        esperarA(() -> get("/api/auditoria?accion=USUARIO_INVITADO", sesionTitular.token()).json().get("total").asLong() == 1);
        var entrada = get("/api/auditoria?accion=USUARIO_INVITADO", sesionTitular.token()).json().get("entradas").get(0);
        assertThat(entrada.get("actor").asString()).isEqualTo("Ana Titular");
        assertThat(entrada.get("descripcion").asString()).contains("aud@despacho.test");
    }

    /** Añade permisos al rol Gestor conservando los que ya tiene. */
    private void concederAlGestor(String... permisos) {
        var actuales = new LinkedHashSet<String>();
        for (var rol : get("/api/roles", sesionTitular.token()).json()) {
            if (rol.get("rol").asString().equals("GESTOR")) {
                rol.get("permisos").forEach(p -> actuales.add(p.asString()));
            }
        }
        actuales.addAll(List.of(permisos));
        String lista = actuales.stream().map(p -> "\"" + p + "\"").collect(Collectors.joining(","));
        assertThat(put("/api/roles/GESTOR/permisos", sesionTitular.token(), "{\"permisos\":[" + lista + "]}").estado())
                .isEqualTo(200);
    }
}
