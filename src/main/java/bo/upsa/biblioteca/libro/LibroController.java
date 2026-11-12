package bo.upsa.biblioteca.libro;

import bo.upsa.biblioteca.common.NegocioException;
import bo.upsa.biblioteca.common.NoEncontradoException;
import bo.upsa.biblioteca.prestamo.PrestamoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/libros")
@Tag(name = "Libros")
public class LibroController {
    private final LibroRepository libros;
    private final PrestamoRepository prestamos;

    public LibroController(LibroRepository libros, PrestamoRepository prestamos) {
        this.libros = libros;
        this.prestamos = prestamos;
    }

    @GetMapping
    @Operation(summary = "Buscar libros por título o autor, categoría y disponibilidad (paginado)")
    public Page<LibroDto.Respuesta> buscar(@RequestParam(required = false) String q,
                                           @RequestParam(required = false) String categoria,
                                           @RequestParam(defaultValue = "false") boolean disponibles,
                                           @PageableDefault(size = 20, sort = "titulo") Pageable pageable) {
        return libros.buscar(blank(q), blank(categoria), disponibles, pageable).map(LibroDto.Respuesta::de);
    }

    @GetMapping("/{id}")
    public LibroDto.Respuesta ver(@PathVariable Long id) {
        return LibroDto.Respuesta.de(buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un libro")
    public LibroDto.Respuesta crear(@Valid @RequestBody LibroDto.Crear d) {
        return LibroDto.Respuesta.de(libros.save(new Libro(d.isbn(), d.titulo(), d.autor(), d.anio(), d.categoria(), d.ejemplares())));
    }

    @PutMapping("/{id}")
    @Transactional
    public LibroDto.Respuesta actualizar(@PathVariable Long id, @Valid @RequestBody LibroDto.Actualizar d) {
        Libro l = buscarPorId(id);
        l.actualizar(d.titulo(), d.autor(), d.anio(), d.categoria(), d.ejemplares());
        return LibroDto.Respuesta.de(l);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un libro (solo si nunca se prestó)")
    public void eliminar(@PathVariable Long id) {
        Libro l = buscarPorId(id);
        if (prestamos.existsByLibroId(id)) {
            throw new NegocioException("libro-con-historial", "El libro tiene préstamos registrados: no se puede borrar");
        }
        libros.delete(l);
    }

    private Libro buscarPorId(Long id) {
        return libros.findById(id).orElseThrow(() -> new NoEncontradoException("Libro", id));
    }

    private static String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
