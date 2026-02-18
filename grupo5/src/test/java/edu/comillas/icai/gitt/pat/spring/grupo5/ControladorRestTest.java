package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(ControladorREST.class)
@AutoConfigureMockMvc(addFilters = false)
class ControladorRestTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ControladorREST controller;

    @BeforeEach
    void prepararEstado() {
        // Campos accesibles directamente (package-private)
        controller.pistas.clear();

        // Campos private → reflexión
        @SuppressWarnings("unchecked")
        Map<Long, Reserva> reservasMap = (Map<Long, Reserva>) ReflectionTestUtils.getField(controller, "reservas");
        reservasMap.clear();

        ReflectionTestUtils.setField(controller, "idPistaContador", 0L);
        ReflectionTestUtils.setField(controller, "idReservaContador", 0L);

        // Preparar datos de prueba
        Pista p1 = new Pista(
                1L,
                "Central",
                "Exterior",
                2000L,                  // long, ejemplo 20.00 → 2000 céntimos
                true,
                LocalDate.now().toString()
        );
        controller.pistas.put(1L, p1);

        ReflectionTestUtils.setField(controller, "idPistaContador", 2L);

        Reserva r = new Reserva(
                100L,
                1L,
                "u123",
                LocalDateTime.parse("2026-03-20T10:00"),
                LocalDateTime.parse("2026-03-20T11:00")
        );
        reservasMap.put(100L, r);

        ReflectionTestUtils.setField(controller, "idReservaContador", 101L);
    }

    @Test
    void availability_sin_courtId_hay_alguna_pista_libre_200() throws Exception {
        mockMvc.perform(get("/pistaPadel/availability")
                        .param("date", "2026-03-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-03-21"))
                .andExpect(jsonPath("$.courtId").isEmpty())
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.mensaje").value(containsString("Hay disponibilidad")));
    }

    @Test
    void availability_con_courtId_ocupada_200() throws Exception {
        mockMvc.perform(get("/pistaPadel/availability")
                        .param("date", "2026-03-20")
                        .param("courtId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));
    }

    @Test
    void availability_fecha_invalida_400() throws Exception {
        mockMvc.perform(get("/pistaPadel/availability")
                        .param("date", "2026-13-45"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void disponibilidad_pista_concreta_no_existe_404() throws Exception {
        mockMvc.perform(get("/pistaPadel/courts/999/availability")
                        .param("date", "2026-03-20"))
                .andExpect(status().isNotFound());
    }

    @Test
    void mis_reservas_sin_usuario_401() throws Exception {
        mockMvc.perform(get("/pistaPadel/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mis_reservas_usuario_sin_reservas_devuelve_lista_vacia() throws Exception {
        mockMvc.perform(get("/pistaPadel/reservations")
                        .header("X-User-Id", "otro_usuario_sin_nada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}