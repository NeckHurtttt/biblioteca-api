package bo.upsa.biblioteca;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneId;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Biblioteca API", version = "1.0",
        description = "Libros, socios y préstamos con reglas de negocio: límites por tipo de socio, vencimientos, multas y renovaciones."))
public class BibliotecaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaApplication.class, args);
    }

    /** Reloj inyectable: en las pruebas se reemplaza para viajar en el tiempo. */
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/La_Paz"));
    }
}
