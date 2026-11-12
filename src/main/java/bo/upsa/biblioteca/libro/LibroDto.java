package bo.upsa.biblioteca.libro;

import jakarta.validation.constraints.*;

public final class LibroDto {
    private LibroDto() { }

    public record Crear(
            @NotBlank @Pattern(regexp = "[0-9-]{10,17}", message = "ISBN de 10 o 13 dígitos (con o sin guiones)") String isbn,
            @NotBlank @Size(max = 200) String titulo,
            @NotBlank @Size(max = 120) String autor,
            @Min(1400) @Max(2100) Integer anio,
            @NotBlank String categoria,
            @Min(1) @Max(500) int ejemplares) { }

    public record Actualizar(
            @NotBlank @Size(max = 200) String titulo,
            @NotBlank @Size(max = 120) String autor,
            @Min(1400) @Max(2100) Integer anio,
            @NotBlank String categoria,
            @Min(1) @Max(500) int ejemplares) { }

    public record Respuesta(Long id, String isbn, String titulo, String autor, Integer anio, String categoria,
                            int ejemplares, int disponibles) {
        public static Respuesta de(Libro l) {
            return new Respuesta(l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(), l.getAnio(), l.getCategoria(),
                    l.getEjemplares(), l.getDisponibles());
        }
    }
}
