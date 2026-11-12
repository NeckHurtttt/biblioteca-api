package bo.upsa.biblioteca.libro;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    @Query("""
            select l from Libro l
            where (:q is null or lower(l.titulo) like lower(concat('%', :q, '%')) or lower(l.autor) like lower(concat('%', :q, '%')))
              and (:categoria is null or lower(l.categoria) = lower(:categoria))
              and (:disponibles = false or l.disponibles > 0)
            """)
    Page<Libro> buscar(@Param("q") String q, @Param("categoria") String categoria,
                       @Param("disponibles") boolean disponibles, Pageable pageable);
}
