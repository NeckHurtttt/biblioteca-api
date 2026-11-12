package bo.upsa.biblioteca.common;

/** El recurso pedido no existe (responde 404 Not Found). */
public class NoEncontradoException extends RuntimeException {
    public NoEncontradoException(String recurso, Object id) {
        super(recurso + " " + id + " no existe");
    }
}
