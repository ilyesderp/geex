package es.geex.compartido.adapter.web;

import es.geex.compartido.domain.AccesoDenegadoException;
import es.geex.compartido.domain.AutenticacionFallidaException;
import es.geex.compartido.domain.ConflictoEdicionException;
import es.geex.compartido.domain.DomainException;
import es.geex.compartido.domain.NoEncontradoException;
import es.geex.compartido.domain.ReglaNegocioException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones a respuestas RFC 9457 (Problem Details) con mensajes en
 * español y un {@code codigo} estable que el frontend puede interpretar.
 */
@RestControllerAdvice
class ManejadorErrores extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(NoEncontradoException.class)
    ProblemDetail noEncontrado(NoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "No encontrado", e);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    ProblemDetail reglaNegocio(ReglaNegocioException e) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Operación no permitida", e);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    ProblemDetail accesoDenegado(AccesoDenegadoException e) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado", e);
    }

    @ExceptionHandler(AutenticacionFallidaException.class)
    ProblemDetail autenticacion(AutenticacionFallidaException e) {
        return problema(HttpStatus.UNAUTHORIZED, "No autenticado", e);
    }

    @ExceptionHandler(ConflictoEdicionException.class)
    ProblemDetail conflictoEdicion(ConflictoEdicionException e) {
        return problema(HttpStatus.CONFLICT, "Conflicto de edición", e);
    }

    @ExceptionHandler(DomainException.class)
    ProblemDetail dominio(DomainException e) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud no válida", e);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail conflicto() {
        var detalle = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Otro usuario ha modificado estos datos mientras los editabas. Recarga la página e inténtalo de nuevo.");
        detalle.setTitle("Conflicto de edición");
        detalle.setProperty("codigo", "CONFLICTO_EDICION");
        return detalle;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception e) {
        log.error("Error no controlado", e);
        var detalle = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Se ha producido un error inesperado. Inténtalo de nuevo más tarde.");
        detalle.setTitle("Error interno");
        detalle.setProperty("codigo", "ERROR_INTERNO");
        return detalle;
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders cabeceras, HttpStatusCode estado, WebRequest peticion) {
        List<Map<String, String>> errores = e.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "campo", error.getField(),
                        "mensaje", error.getDefaultMessage() == null ? "Valor no válido" : error.getDefaultMessage()))
                .toList();
        var detalle = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Revisa los datos del formulario.");
        detalle.setTitle("Datos no válidos");
        detalle.setProperty("codigo", "VALIDACION");
        detalle.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(detalle);
    }

    private static ProblemDetail problema(HttpStatus estado, String titulo, DomainException e) {
        var detalle = ProblemDetail.forStatusAndDetail(estado, e.getMessage());
        detalle.setTitle(titulo);
        detalle.setProperty("codigo", e.codigo());
        return detalle;
    }
}
