package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ControladorRestTestE2E {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ControladorREST controller;  // ← inyecta el controlador

    @BeforeEach
    void limpiarEstado() {
        // Obtenemos o creamos los mapas con reflexión para evitar NPE
        @SuppressWarnings("unchecked")
        Map<Long, Pista> pistasMap = (Map<Long, Pista>) ReflectionTestUtils.getField(controller, "pistas");
        if (pistasMap == null) {
            pistasMap = new ConcurrentHashMap<>();
            ReflectionTestUtils.setField(controller, "pistas", pistasMap);
        }
        pistasMap.clear();

        @SuppressWarnings("unchecked")
        Map<Long, Reserva> reservasMap = (Map<Long, Reserva>) ReflectionTestUtils.getField(controller, "reservas");
        if (reservasMap == null) {
            reservasMap = new ConcurrentHashMap<>();
            ReflectionTestUtils.setField(controller, "reservas", reservasMap);
        }
        reservasMap.clear();

        ReflectionTestUtils.setField(controller, "idPistaContador", 0L);
        ReflectionTestUtils.setField(controller, "idReservaContador", 0L);

        // Creamos una pista de prueba para que haya al menos una disponible
        Pista p1 = new Pista(
                1L,
                "Central",
                "Exterior",
                2000L,
                true,
                LocalDate.now().toString()
        );
        pistasMap.put(1L, p1);
        ReflectionTestUtils.setField(controller, "idPistaContador", 2L);
    }

    @Test
    void availability_sin_courtId_devuelve_disponible() throws Exception {
        mockMvc.perform(get("/pistaPadel/availability")
                        .param("date", "2026-03-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.mensaje").value(containsString("Hay disponibilidad")));
    }

}