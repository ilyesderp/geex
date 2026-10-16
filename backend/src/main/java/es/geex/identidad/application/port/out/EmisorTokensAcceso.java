package es.geex.identidad.application.port.out;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;

/** Emite el token de acceso (JWT firmado). La aplicación no conoce el formato concreto. */
public interface EmisorTokensAcceso {

    TokenAcceso emitir(DatosToken datos, Instant ahora);

    record DatosToken(UsuarioId usuario, DespachoId despacho, Rol rol, boolean superadmin, int versionToken) {
    }

    record TokenAcceso(String valor, Instant expiraEn) {
    }
}
