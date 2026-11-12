package bo.upsa.biblioteca.socio;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "socio")
public class Socio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String ci;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TipoSocio tipo;

    /** Multas sin pagar en bolivianos. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deuda = BigDecimal.ZERO;

    protected Socio() { }

    public Socio(String ci, String nombre, String email, TipoSocio tipo) {
        this.ci = ci;
        this.nombre = nombre;
        this.email = email;
        this.tipo = tipo;
    }

    public void sumarMulta(BigDecimal monto) { deuda = deuda.add(monto); }

    public BigDecimal pagar(BigDecimal monto) {
        BigDecimal pago = monto.min(deuda);
        deuda = deuda.subtract(pago);
        return pago;
    }

    public Long getId() { return id; }
    public String getCi() { return ci; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public TipoSocio getTipo() { return tipo; }
    public BigDecimal getDeuda() { return deuda; }
}
