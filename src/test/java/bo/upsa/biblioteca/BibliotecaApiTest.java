package bo.upsa.biblioteca;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BibliotecaApiTest {

    @TestConfiguration
    static class Reloj {
        @Bean @Primary RelojPrueba relojPrueba() { return new RelojPrueba(); }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired RelojPrueba reloj;

    // En data.sql: socio 1 y 2 son ESTUDIANTES, 3 es DOCENTE. Libro 4 tiene 1 solo ejemplar.
    ResultActions prestar(long libro, long socio) throws Exception {
        return mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"libroId\":" + libro + ",\"socioId\":" + socio + "}"));
    }

    long id(ResultActions r) throws Exception {
        JsonNode n = json.readTree(r.andReturn().getResponse().getContentAsString());
        return n.get("id").asLong();
    }

    @Test
    void buscaLibrosPorTextoYCategoria() throws Exception {
        mvc.perform(get("/api/libros").param("q", "java")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Effective Java"));
        mvc.perform(get("/api/libros").param("categoria", "literatura"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void prestarDescuentaUnEjemplarYPoneFechaLimiteSegunTipo() throws Exception {
        prestar(1, 1).andExpect(status().isCreated())
                .andExpect(jsonPath("$.fechaLimite").value("2026-03-09"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
        prestar(2, 3).andExpect(jsonPath("$.fechaLimite").value("2026-03-16")); // docente: 14 días
        mvc.perform(get("/api/libros/1")).andExpect(jsonPath("$.disponibles").value(2));
    }

    @Test
    void sinEjemplaresDa409() throws Exception {
        prestar(4, 1).andExpect(status().isCreated());
        prestar(4, 2).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("sin-ejemplares"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void estudianteNoPasaDeTresLibros() throws Exception {
        prestar(1, 1); prestar(2, 1); prestar(3, 1);
        prestar(5, 1).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("limite-prestamos"));
    }

    @Test
    void noPresta2CopiasDelMismoLibro() throws Exception {
        prestar(1, 1);
        prestar(1, 1).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("libro-repetido"));
    }

    @Test
    void devolverTardeCobraMultaYBloqueaNuevosPrestamos() throws Exception {
        long p = id(prestar(1, 1));
        reloj.avanzarDias(10); // 3 días de atraso
        mvc.perform(get("/api/prestamos").param("vencidos", "true")).andExpect(jsonPath("$", hasSize(1)));
        prestar(2, 1).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("socio-con-vencidos"));
        mvc.perform(post("/api/prestamos/" + p + "/devolucion")).andExpect(status().isOk())
                .andExpect(jsonPath("$.multa").value(6.0)).andExpect(jsonPath("$.estado").value("DEVUELTO"));
        mvc.perform(get("/api/socios/1")).andExpect(jsonPath("$.deuda").value(6.0));
        prestar(2, 1).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("socio-con-deuda"));
        mvc.perform(post("/api/socios/1/pagos").contentType(MediaType.APPLICATION_JSON).content("{\"monto\":10}"))
                .andExpect(jsonPath("$.deuda").value(0));
        prestar(2, 1).andExpect(status().isCreated());
    }

    @Test
    void renovarUnaSolaVezYNoSiEstaVencido() throws Exception {
        long p = id(prestar(1, 1));
        mvc.perform(post("/api/prestamos/" + p + "/renovacion")).andExpect(jsonPath("$.fechaLimite").value("2026-03-16"));
        mvc.perform(post("/api/prestamos/" + p + "/renovacion")).andExpect(jsonPath("$.codigo").value("sin-renovaciones"));
        long q = id(prestar(2, 2));
        reloj.avanzarDias(8);
        mvc.perform(post("/api/prestamos/" + q + "/renovacion")).andExpect(jsonPath("$.codigo").value("vencido"));
    }

    @Test
    void validacionDevuelve400ConLosCampos() throws Exception {
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content("{\"isbn\":\"x\",\"titulo\":\"\",\"ejemplares\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.isbn").exists())
                .andExpect(jsonPath("$.campos.titulo").exists())
                .andExpect(jsonPath("$.campos.ejemplares").exists());
    }

    @Test
    void isbnRepetidoDa409() throws Exception {
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"978-0134685991\",\"titulo\":\"Otra\",\"autor\":\"X\",\"categoria\":\"Y\",\"ejemplares\":1}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("duplicado"));
    }

    @Test
    void noSeBorraUnLibroConHistorialNiSeBajanEjemplaresPrestados() throws Exception {
        prestar(4, 1);
        mvc.perform(delete("/api/libros/4")).andExpect(status().isConflict());
        mvc.perform(put("/api/libros/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"T\",\"autor\":\"A\",\"categoria\":\"C\",\"ejemplares\":5}")).andExpect(jsonPath("$.disponibles").value(5));
        mvc.perform(delete("/api/libros/9")).andExpect(status().isNoContent());
        mvc.perform(get("/api/libros/9")).andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("no-encontrado"));
    }

    @Test
    void estadisticasYSwagger() throws Exception {
        prestar(1, 1); prestar(1, 2); prestar(2, 3);
        mvc.perform(get("/api/estadisticas")).andExpect(jsonPath("$.activos").value(3))
                .andExpect(jsonPath("$.masPrestados[0].titulo").value("Introduction to Algorithms"))
                .andExpect(jsonPath("$.masPrestados[0].veces").value(2));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.info.title").value("Biblioteca API"));
    }
}
