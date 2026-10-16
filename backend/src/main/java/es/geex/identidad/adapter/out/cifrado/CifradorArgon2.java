package es.geex.identidad.adapter.out.cifrado;

import es.geex.identidad.application.port.out.CifradorContrasenas;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

/** Contraseñas con Argon2id (ganador de la Password Hashing Competition, recomendado por OWASP). */
@Component
class CifradorArgon2 implements CifradorContrasenas {

    private final Argon2PasswordEncoder codificador = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    @Override
    public String cifrar(String contrasena) {
        return codificador.encode(contrasena);
    }

    @Override
    public boolean coincide(String contrasena, String hash) {
        if (contrasena == null || hash == null) {
            return false;
        }
        return codificador.matches(contrasena, hash);
    }
}
