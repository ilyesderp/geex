package es.geex.identidad.adapter.in.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ManejadorErroresIdentidad {

    @ExceptionHandler(DemasiadasPeticionesException.class)
    ProblemDetail demasiadasPeticiones(DemasiadasPeticionesException e) {
        var detalle = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
        detalle.setTitle("Demasiadas peticiones");
        detalle.setProperty("codigo", "DEMASIADAS_PETICIONES");
        return detalle;
    }
}
