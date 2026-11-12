package bo.upsa.biblioteca.prestamo;

import bo.upsa.biblioteca.libro.Libro;
import bo.upsa.biblioteca.socio.Socio;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "prestamo")
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Libro libro;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Socio socio;

    @Column(nullable = false)
    private LocalDate fechaPrestamo;

    @Column(nullable = false)
    private LocalDate fechaLimite;

    private LocalDate fechaDevolucion;

    @Column(nullable = false)
    private int renovaciones;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal multa = BigDecimal.ZERO;

    protected Prestamo() { }

    Prestamo(Libro libro, Socio socio, LocalDate hoy) {
        this.libro = libro;
        this.socio = socio;
        this.fechaPrestamo = hoy;
        this.fechaLimite = hoy.plusDays(socio.getTipo().diasPlazo());
    }

    public boolean activo() { return fechaDevolucion == null; }

    public boolean vencido(LocalDate hoy) { return activo() && hoy.isAfter(fechaLimite); }

    void cerrar(LocalDate hoy, BigDecimal multa) {
        this.fechaDevolucion = hoy;
        this.multa = multa;
    }

    void renovar() {
        this.renovaciones++;
        this.fechaLimite = fechaLimite.plusDays(socio.getTipo().diasPlazo());
    }

    public Long getId() { return id; }
    public Libro getLibro() { return libro; }
    public Socio getSocio() { return socio; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public int getRenovaciones() { return renovaciones; }
    public BigDecimal getMulta() { return multa; }
}
