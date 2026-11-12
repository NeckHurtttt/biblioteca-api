package bo.upsa.biblioteca.prestamo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PrestamoDto(Long id, Long libroId, String titulo, Long socioId, String socio,
                          LocalDate fechaPrestamo, LocalDate fechaLimite, LocalDate fechaDevolucion,
                          int renovaciones, BigDecimal multa, String estado) {
    public static PrestamoDto de(Prestamo p) {
        return de(p, LocalDate.now());
    }

    public static PrestamoDto de(Prestamo p, LocalDate hoy) {
        String estado = !p.activo() ? "DEVUELTO" : p.vencido(hoy) ? "VENCIDO" : "ACTIVO";
        return new PrestamoDto(p.getId(), p.getLibro().getId(), p.getLibro().getTitulo(), p.getSocio().getId(),
                p.getSocio().getNombre(), p.getFechaPrestamo(), p.getFechaLimite(), p.getFechaDevolucion(),
                p.getRenovaciones(), p.getMulta(), estado);
    }
}
