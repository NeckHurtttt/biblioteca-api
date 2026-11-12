package bo.upsa.biblioteca.libro;

import jakarta.persistence.*;

@Entity
@Table(name = "libro")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 17)
    private String isbn;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    private Integer anio;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private int ejemplares;

    @Column(nullable = false)
    private int disponibles;

    protected Libro() { }

    public Libro(String isbn, String titulo, String autor, Integer anio, String categoria, int ejemplares) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.anio = anio;
        this.categoria = categoria;
        this.ejemplares = ejemplares;
        this.disponibles = ejemplares;
    }

    public void prestarEjemplar() { disponibles--; }

    public void devolverEjemplar() { disponibles++; }

    /** Cambia el total de ejemplares sin perder la cuenta de los que están prestados. */
    public void actualizar(String titulo, String autor, Integer anio, String categoria, int ejemplares) {
        int prestados = this.ejemplares - this.disponibles;
        if (ejemplares < prestados) {
            throw new bo.upsa.biblioteca.common.NegocioException("ejemplares-prestados",
                    "Hay " + prestados + " ejemplares prestados: el total no puede ser menor");
        }
        this.titulo = titulo;
        this.autor = autor;
        this.anio = anio;
        this.categoria = categoria;
        this.ejemplares = ejemplares;
        this.disponibles = ejemplares - prestados;
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public Integer getAnio() { return anio; }
    public String getCategoria() { return categoria; }
    public int getEjemplares() { return ejemplares; }
    public int getDisponibles() { return disponibles; }
}
