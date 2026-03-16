package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración de Disponibilidad con persistencia
 * Verifica:
 * - Guardar disponibilidad en base de datos
 * - Recuperar disponibilidad según fecha y courtId
 * - Consultar disponibilidad mediante el servicio
 * - Actualizar disponibilidad existente
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Test de Integración: Disponibilidad con Persistencia")
class DisponibilidadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Autowired
    private DisponibilidadService disponibilidadService;

    private LocalDate fechaTest;
    private Long courtIdTest = 1L;

    @BeforeEach
    void setup() {
        // Limpiar base de datos
        disponibilidadRepository.deleteAll();
        fechaTest = LocalDate.of(2026, 4, 15);
    }

    @Test
    @DisplayName("Guardar y recuperar disponibilidad desde BD")
    void testGuardarYRecuperarDisponibilidad() {
        // Arrange
        Disponibilidad disponibilidad = new Disponibilidad(
                fechaTest,
                courtIdTest,
                true,
                "Libre"
        );

        // Act
        Disponibilidad guardada = disponibilidadRepository.save(disponibilidad);

        // Assert
        assertNotNull(guardada.getId(), "La disponibilidad debe tener ID generado");
        assertEquals(fechaTest, guardada.getFecha());
        assertEquals(courtIdTest, guardada.getCourtId());
        assertTrue(guardada.isDisponible());
        assertEquals("Libre", guardada.getMensaje());
    }

    @Test
    @DisplayName("Buscar disponibilidad por fecha y courtId")
    void testBuscarPorFechaYCourtId() {
        // Arrange
        Disponibilidad disponibilidad = new Disponibilidad(
                fechaTest,
                courtIdTest,
                true,
                "Libre"
        );
        disponibilidadRepository.save(disponibilidad);

        // Act
        var resultado = disponibilidadRepository.findByFechaAndCourtId(fechaTest, courtIdTest);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(courtIdTest, resultado.get().getCourtId());
        assertEquals(fechaTest, resultado.get().getFecha());
    }

    @Test
    @DisplayName("Consultar disponibilidad mediante servicio")
    void testConsultarDisponibilidadServicio() {
        // Arrange
        Disponibilidad disponibilidad = new Disponibilidad(
                fechaTest,
                courtIdTest,
                true,
                "Libre"
        );
        disponibilidadRepository.save(disponibilidad);

        // Act
        DisponibilidadResponse respuesta = disponibilidadService.consultarDisponibilidad(fechaTest, courtIdTest);

        // Assert
        assertNotNull(respuesta);
        assertEquals(fechaTest, respuesta.fecha());
        assertEquals(courtIdTest, respuesta.courtId());
        assertTrue(respuesta.disponible());
        assertEquals("Libre", respuesta.mensaje());
    }

    @Test
    @DisplayName("Actualizar disponibilidad")
    void testActualizarDisponibilidad() {
        // Arrange
        Disponibilidad disponibilidad = new Disponibilidad(
                fechaTest,
                courtIdTest,
                true,
                "Libre"
        );
        disponibilidadRepository.save(disponibilidad);

        // Act
        DisponibilidadResponse actualizada = disponibilidadService.actualizarDisponibilidad(
                fechaTest,
                courtIdTest,
                false,
                "Ocupada"
        );

        // Assert
        assertFalse(actualizada.disponible());
        assertEquals("Ocupada", actualizada.mensaje());

        // Verificar que se actualizó en BD
        var enBD = disponibilidadRepository.findByFechaAndCourtId(fechaTest, courtIdTest);
        assertTrue(enBD.isPresent());
        assertFalse(enBD.get().isDisponible());
    }

    @Test
    @DisplayName("Crear disponibilidad automáticamente si no existe")
    void testCrearDisponibilidadAutomaticamente() {
        // Arrange - No existe disponibilidad para esa fecha/courtId
        assertTrue(disponibilidadRepository.findByFechaAndCourtId(fechaTest, courtIdTest).isEmpty());

        // Act
        DisponibilidadResponse respuesta = disponibilidadService.obtenerDisponibilidadPistaConcreta(fechaTest, courtIdTest);

        // Assert
        assertNotNull(respuesta);
        assertEquals(fechaTest, respuesta.fecha());
        assertEquals(courtIdTest, respuesta.courtId());
        assertTrue(respuesta.disponible()); // Por defecto se crea como disponible

        // Verificar que se creó en BD
        var enBD = disponibilidadRepository.findByFechaAndCourtId(fechaTest, courtIdTest);
        assertTrue(enBD.isPresent());
    }

    @Test
    @DisplayName("Buscar disponibilidades por rango de fechas")
    void testBuscarPorRangoFechas() {
        // Arrange
        LocalDate fecha1 = LocalDate.of(2026, 4, 10);
        LocalDate fecha2 = LocalDate.of(2026, 4, 15);
        LocalDate fecha3 = LocalDate.of(2026, 4, 20);

        disponibilidadRepository.save(new Disponibilidad(fecha1, courtIdTest, true, "Libre"));
        disponibilidadRepository.save(new Disponibilidad(fecha2, courtIdTest, false, "Ocupada"));
        disponibilidadRepository.save(new Disponibilidad(fecha3, courtIdTest, true, "Libre"));

        // Act
        var resultados = disponibilidadRepository.findByCourtIdAndFechaBetween(
                courtIdTest,
                fecha1,
                fecha2
        );

        // Assert
        assertEquals(2, resultados.size());
        assertTrue(resultados.stream().allMatch(d -> d.getCourtId().equals(courtIdTest)));
    }

    @Test
    @DisplayName("Endpoint GET /pistaPadel/availability con persistencia")
    void testEndpointAvailabilityConPersistencia() throws Exception {
        // Arrange
        Disponibilidad disponibilidad = new Disponibilidad(
                fechaTest,
                courtIdTest,
                true,
                "Libre"
        );
        disponibilidadRepository.save(disponibilidad);

        // Act & Assert
        mockMvc.perform(get("/pistaPadel/availability")
                .param("date", fechaTest.toString())
                .param("courtId", courtIdTest.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value(fechaTest.toString()))
                .andExpect(jsonPath("$.courtId").value(courtIdTest))
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    @DisplayName("Endpoint GET /pistaPadel/courts/{courtId}/availability con persistencia")
    void testEndpointCourtAvailabilityConPersistencia() throws Exception {
        // Arrange
        Disponibilidad disponibilidad = new Disponibilidad(
                fechaTest,
                courtIdTest,
                false,
                "Ocupada"
        );
        disponibilidadRepository.save(disponibilidad);

        // Act & Assert
        mockMvc.perform(get("/pistaPadel/courts/{courtId}/availability", courtIdTest)
                .param("date", fechaTest.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value(fechaTest.toString()))
                .andExpect(jsonPath("$.disponible").value(false))
                .andExpect(jsonPath("$.mensaje").value("Ocupada"));
    }

    @Test
    @DisplayName("Múltiples pistas con disponibilidades diferentes")
    void testMultiplesPistasDiferenteDisponibilidad() {
        // Arrange
        Long courtId1 = 1L;
        Long courtId2 = 2L;

        disponibilidadRepository.save(new Disponibilidad(fechaTest, courtId1, true, "Libre"));
        disponibilidadRepository.save(new Disponibilidad(fechaTest, courtId2, false, "Ocupada"));

        // Act
        var disp1 = disponibilidadRepository.findByFechaAndCourtId(fechaTest, courtId1);
        var disp2 = disponibilidadRepository.findByFechaAndCourtId(fechaTest, courtId2);

        // Assert
        assertTrue(disp1.isPresent());
        assertTrue(disp1.get().isDisponible());

        assertTrue(disp2.isPresent());
        assertFalse(disp2.get().isDisponible());
    }
}
