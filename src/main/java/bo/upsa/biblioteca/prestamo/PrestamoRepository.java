package bo.upsa.biblioteca.prestamo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    /** Trae el libro y el socio en la misma consulta (evita N+1 y LazyInitializationException). */
    @Override
    @EntityGraph(attributePaths = {"libro", "socio"})
    Optional<Prestamo> findById(Long id);

    long countBySocioIdAndFechaDevolucionIsNull(Long socioId);

    boolean existsBySocioIdAndFechaDevolucionIsNullAndFechaLimiteBefore(Long socioId, LocalDate hoy);

    boolean existsBySocioIdAndLibroIdAndFechaDevolucionIsNull(Long socioId, Long libroId);

    boolean existsByLibroId(Long libroId);

    @EntityGraph(attributePaths = {"libro", "socio"})
    List<Prestamo> findBySocioIdOrderByFechaPrestamoDesc(Long socioId);

    @EntityGraph(attributePaths = {"libro", "socio"})
    List<Prestamo> findByFechaDevolucionIsNullOrderByFechaLimite();

    @EntityGraph(attributePaths = {"libro", "socio"})
    List<Prestamo> findByFechaDevolucionIsNullAndFechaLimiteBeforeOrderByFechaLimite(LocalDate hoy);

    @Query("select l.titulo, count(p) from Prestamo p join p.libro l group by l.titulo order by count(p) desc limit 5")
    List<Object[]> masPrestados();
}
