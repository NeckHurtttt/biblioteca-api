package bo.upsa.biblioteca.prestamo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Préstamos")
public class PrestamoController {
    private final PrestamoService service;
    private final PrestamoRepository prestamos;

    public PrestamoController(PrestamoService service, PrestamoRepository prestamos) {
        this.service = service;
        this.prestamos = prestamos;
    }

    public record Pedido(@NotNull Long libroId, @NotNull Long socioId) { }

    @PostMapping("/prestamos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Prestar un libro a un socio (aplica límites, deudas y vencidos)")
    public PrestamoDto prestar(@Valid @RequestBody Pedido p) {
        return PrestamoDto.de(service.prestar(p.libroId(), p.socioId()), service.hoy());
    }

    @PostMapping("/prestamos/{id}/devolucion")
    @Operation(summary = "Devolver: calcula la multa por atraso (Bs 2 por día)")
    public PrestamoDto devolver(@PathVariable Long id) {
        return PrestamoDto.de(service.devolver(id), service.hoy());
    }

    @PostMapping("/prestamos/{id}/renovacion")
    @Operation(summary = "Renovar una vez si no está vencido")
    public PrestamoDto renovar(@PathVariable Long id) {
        return PrestamoDto.de(service.renovar(id), service.hoy());
    }

    @GetMapping("/prestamos/{id}")
    public PrestamoDto ver(@PathVariable Long id) {
        return PrestamoDto.de(service.buscar(id), service.hoy());
    }

    @GetMapping("/prestamos")
    @Transactional(readOnly = true)
    @Operation(summary = "Préstamos activos; con ?vencidos=true solo los atrasados")
    public List<PrestamoDto> activos(@RequestParam(defaultValue = "false") boolean vencidos) {
        var hoy = service.hoy();
        var lista = vencidos ? prestamos.findByFechaDevolucionIsNullAndFechaLimiteBeforeOrderByFechaLimite(hoy)
                : prestamos.findByFechaDevolucionIsNullOrderByFechaLimite();
        return lista.stream().map(p -> PrestamoDto.de(p, hoy)).toList();
    }

    @GetMapping("/estadisticas")
    @Transactional(readOnly = true)
    @Operation(summary = "Resumen: préstamos activos, vencidos y los 5 libros más pedidos")
    public Map<String, Object> estadisticas() {
        var hoy = service.hoy();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("activos", prestamos.findByFechaDevolucionIsNullOrderByFechaLimite().size());
        m.put("vencidos", prestamos.findByFechaDevolucionIsNullAndFechaLimiteBeforeOrderByFechaLimite(hoy).size());
        m.put("masPrestados", prestamos.masPrestados().stream().map(r -> Map.of("titulo", r[0], "veces", r[1])).toList());
        return m;
    }
}
