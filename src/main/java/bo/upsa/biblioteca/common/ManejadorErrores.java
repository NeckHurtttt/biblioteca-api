package bo.upsa.biblioteca.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte las excepciones en respuestas RFC 7807 (application/problem+json). */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(NoEncontradoException.class)
    ProblemDetail noEncontrado(NoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "no-encontrado", "No encontrado", e.getMessage());
    }

    @ExceptionHandler(NegocioException.class)
    ProblemDetail negocio(NegocioException e) {
        return problema(HttpStatus.CONFLICT, e.getCodigo(), "Regla de negocio", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacion(MethodArgumentNotValidException e) {
        ProblemDetail p = problema(HttpStatus.BAD_REQUEST, "validacion", "Datos inválidos", "Revisa los campos marcados");
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> campos.put(f.getField(), f.getDefaultMessage()));
        p.setProperty("campos", campos);
        return p;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail duplicado(DataIntegrityViolationException e) {
        return problema(HttpStatus.CONFLICT, "duplicado", "Dato repetido", "Ya existe un registro con ese ISBN, CI o correo");
    }

    private ProblemDetail problema(HttpStatus status, String codigo, String titulo, String detalle) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalle);
        p.setTitle(titulo);
        p.setType(URI.create("https://github.com/NeckHurtttt/biblioteca-api#" + codigo));
        p.setProperty("codigo", codigo);
        return p;
    }
}
