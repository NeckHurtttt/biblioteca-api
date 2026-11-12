package bo.upsa.biblioteca.socio;

/** Cada tipo de socio tiene su límite de préstamos y su plazo. */
public enum TipoSocio {
    ESTUDIANTE(3, 7),
    DOCENTE(5, 14);

    private final int maxPrestamos;
    private final int diasPlazo;

    TipoSocio(int maxPrestamos, int diasPlazo) {
        this.maxPrestamos = maxPrestamos;
        this.diasPlazo = diasPlazo;
    }

    public int maxPrestamos() { return maxPrestamos; }
    public int diasPlazo() { return diasPlazo; }
}
