package bo.upsa.biblioteca.socio;

import bo.upsa.biblioteca.common.NoEncontradoException;
import bo.upsa.biblioteca.prestamo.PrestamoDto;
import bo.upsa.biblioteca.prestamo.PrestamoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/socios")
@Tag(name = "Socios")
public class SocioController {
    private final SocioRepository socios;
    private final PrestamoRepository prestamos;

    public SocioController(SocioRepository socios, PrestamoRepository prestamos) {
        this.socios = socios;
        this.prestamos = prestamos;
    }

    public record Crear(@NotBlank @Pattern(regexp = "\\d{5,10}(-?[0-9A-Z]{1,3})?", message = "CI boliviano, ej. 9876543 o 9876543-1B") String ci,
                        @NotBlank @Size(max = 120) String nombre,
                        @NotBlank @Email String email,
                        @NotNull TipoSocio tipo) { }

    public record Respuesta(Long id, String ci, String nombre, String email, TipoSocio tipo, BigDecimal deuda,
                            int maxPrestamos, int diasPlazo) {
        static Respuesta de(Socio s) {
            return new Respuesta(s.getId(), s.getCi(), s.getNombre(), s.getEmail(), s.getTipo(), s.getDeuda(),
                    s.getTipo().maxPrestamos(), s.getTipo().diasPlazo());
        }
    }

    public record Pago(@NotNull @DecimalMin(value = "0.01") BigDecimal monto) { }

    @GetMapping
    public List<Respuesta> listar() {
        return socios.findAll().stream().map(Respuesta::de).toList();
    }

    @GetMapping("/{id}")
    public Respuesta ver(@PathVariable Long id) {
        return Respuesta.de(buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Respuesta crear(@Valid @RequestBody Crear d) {
        return Respuesta.de(socios.save(new Socio(d.ci(), d.nombre(), d.email().toLowerCase(), d.tipo())));
    }

    @GetMapping("/{id}/prestamos")
    @Operation(summary = "Historial de préstamos del socio")
    public List<PrestamoDto> historial(@PathVariable Long id) {
        buscar(id);
        return prestamos.findBySocioIdOrderByFechaPrestamoDesc(id).stream().map(PrestamoDto::de).toList();
    }

    @PostMapping("/{id}/pagos")
    @Transactional
    @Operation(summary = "Pagar multas (si paga de más, se cobra solo lo que debe)")
    public Respuesta pagar(@PathVariable Long id, @Valid @RequestBody Pago p) {
        Socio s = buscar(id);
        s.pagar(p.monto());
        return Respuesta.de(s);
    }

    private Socio buscar(Long id) {
        return socios.findById(id).orElseThrow(() -> new NoEncontradoException("Socio", id));
    }
}
