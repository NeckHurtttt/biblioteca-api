package bo.upsa.biblioteca.prestamo;

import bo.upsa.biblioteca.common.NegocioException;
import bo.upsa.biblioteca.common.NoEncontradoException;
import bo.upsa.biblioteca.libro.Libro;
import bo.upsa.biblioteca.libro.LibroRepository;
import bo.upsa.biblioteca.socio.Socio;
import bo.upsa.biblioteca.socio.SocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Reglas de la biblioteca:
 * <ul>
 *   <li>Estudiante: hasta 3 libros por 7 días. Docente: hasta 5 por 14 días.</li>
 *   <li>No se presta si el socio debe multas o tiene un libro vencido, ni dos copias del mismo libro.</li>
 *   <li>Multa: Bs 2 por cada día de atraso.</li>
 *   <li>Se puede renovar una sola vez y solo si no está vencido.</li>
 * </ul>
 */
@Service
@Transactional
public class PrestamoService {
    public static final BigDecimal MULTA_POR_DIA = new BigDecimal("2.00");
    public static final int MAX_RENOVACIONES = 1;

    private final PrestamoRepository prestamos;
    private final LibroRepository libros;
    private final SocioRepository socios;
    private final Clock clock;

    public PrestamoService(PrestamoRepository prestamos, LibroRepository libros, SocioRepository socios, Clock clock) {
        this.prestamos = prestamos;
        this.libros = libros;
        this.socios = socios;
        this.clock = clock;
    }

    public LocalDate hoy() {
        return LocalDate.now(clock);
    }

    public Prestamo prestar(Long libroId, Long socioId) {
        Libro libro = libros.findById(libroId).orElseThrow(() -> new NoEncontradoException("Libro", libroId));
        Socio socio = socios.findById(socioId).orElseThrow(() -> new NoEncontradoException("Socio", socioId));
        if (socio.getDeuda().signum() > 0) {
            throw new NegocioException("socio-con-deuda", "El socio debe Bs " + socio.getDeuda() + " en multas");
        }
        if (prestamos.existsBySocioIdAndFechaDevolucionIsNullAndFechaLimiteBefore(socioId, hoy())) {
            throw new NegocioException("socio-con-vencidos", "El socio tiene un libro vencido sin devolver");
        }
        long activos = prestamos.countBySocioIdAndFechaDevolucionIsNull(socioId);
        if (activos >= socio.getTipo().maxPrestamos()) {
            throw new NegocioException("limite-prestamos", "Llegó al límite de " + socio.getTipo().maxPrestamos() + " libros");
        }
        if (prestamos.existsBySocioIdAndLibroIdAndFechaDevolucionIsNull(socioId, libroId)) {
            throw new NegocioException("libro-repetido", "El socio ya tiene un ejemplar de este libro");
        }
        if (libro.getDisponibles() <= 0) {
            throw new NegocioException("sin-ejemplares", "No quedan ejemplares disponibles de «" + libro.getTitulo() + "»");
        }
        libro.prestarEjemplar();
        return prestamos.save(new Prestamo(libro, socio, hoy()));
    }

    public Prestamo devolver(Long prestamoId) {
        Prestamo p = buscar(prestamoId);
        if (!p.activo()) {
            throw new NegocioException("ya-devuelto", "Este préstamo ya fue devuelto");
        }
        long atraso = Math.max(0, ChronoUnit.DAYS.between(p.getFechaLimite(), hoy()));
        BigDecimal multa = MULTA_POR_DIA.multiply(BigDecimal.valueOf(atraso));
        p.cerrar(hoy(), multa);
        p.getLibro().devolverEjemplar();
        if (multa.signum() > 0) {
            p.getSocio().sumarMulta(multa);
        }
        return p;
    }

    public Prestamo renovar(Long prestamoId) {
        Prestamo p = buscar(prestamoId);
        if (!p.activo()) {
            throw new NegocioException("ya-devuelto", "Este préstamo ya fue devuelto");
        }
        if (p.vencido(hoy())) {
            throw new NegocioException("vencido", "Está vencido: hay que devolverlo y pagar la multa");
        }
        if (p.getRenovaciones() >= MAX_RENOVACIONES) {
            throw new NegocioException("sin-renovaciones", "Solo se puede renovar " + MAX_RENOVACIONES + " vez");
        }
        p.renovar();
        return p;
    }

    @Transactional(readOnly = true)
    public Prestamo buscar(Long id) {
        return prestamos.findById(id).orElseThrow(() -> new NoEncontradoException("Préstamo", id));
    }
}
