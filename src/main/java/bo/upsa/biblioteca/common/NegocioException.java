package bo.upsa.biblioteca.common;

/** Una regla de negocio no se cumple (responde 409 Conflict). */
public class NegocioException extends RuntimeException {
    private final String codigo;

    public NegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
