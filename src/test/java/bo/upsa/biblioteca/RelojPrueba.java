package bo.upsa.biblioteca;

import java.time.*;

/** Reloj que se puede adelantar en las pruebas. */
public class RelojPrueba extends Clock {
    private Instant ahora = LocalDate.of(2026, 3, 2).atStartOfDay(ZoneId.of("America/La_Paz")).toInstant();

    public void avanzarDias(long dias) { ahora = ahora.plus(Duration.ofDays(dias)); }

    @Override public ZoneId getZone() { return ZoneId.of("America/La_Paz"); }
    @Override public Clock withZone(ZoneId zone) { return this; }
    @Override public Instant instant() { return ahora; }
}
