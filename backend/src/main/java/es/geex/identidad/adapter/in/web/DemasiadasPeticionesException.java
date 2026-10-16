package es.geex.identidad.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
class DemasiadasPeticionesException extends RuntimeException {

    DemasiadasPeticionesException() {
        super("Demasiados intentos. Espera unos minutos antes de volver a intentarlo.");
    }
}
