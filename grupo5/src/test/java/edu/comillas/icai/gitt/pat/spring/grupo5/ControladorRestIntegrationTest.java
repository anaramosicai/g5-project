package edu.comillas.icai.gitt.pat.spring.grupo5;

import edu.comillas.icai.gitt.pat.spring.grupo5.controlador.ControladorREST;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Reserva;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoReserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.test.context.support.WithMockUser;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;



/**
 * Test de integración del endpoint POST /pistaPadel/auth/register
 * Verifica:
 *  - 201 Created con datos válidos
 *  - 400 Bad Request con email inválido
 *  - 409 Conflict con email duplicado
 * 
 * Test de integración para RESERVAS con persistencia en BD
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ControladorRestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepoReserva repoReserva;

    private static final String REGISTER = "/pistaPadel/auth/register";

    @Autowired
    private ControladorREST controladorREST;

    @BeforeEach
    void setup_user() {
        controladorREST.reset();
        repoReserva.deleteAll();
    }

    @Test
    void registro_ok_201() throws Exception {
        String body = """
            {
              "idUsuario": 1,
              "nombre": "Ana",
              "apellidos": "Ramos",
              "email": "ana.integration@test.com",
              "password": "123",
              "telefono": "666",
              "rol": "USER",
              "fechaRegistro": null,
              "activo": true
            }
        """;

        this.mockMvc
                .perform(post(REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
/*
    @Test
    void registro_emailFormatoErroneo_400() throws Exception {
        String body = """
            {
              "idUsuario": 1,
              "nombre": "Ana",
              "apellidos": "Ramos",
              "email": "no-es-un-email",
              "password": "123",
              "telefono": "666",
              "rol": "USER",
              "fechaRegistro": null,
              "activo": true
            }
        """;

        this.mockMvc
                .perform(post(REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest()); // 400 por @Email/@NotBlank en Usuario
    }
*/
    @Test
    void registro_emailDuplicado_409() throws Exception {
        String body = """
            {
              "idUsuario": 1,
              "nombre": "Ana",
              "apellidos": "Ramos",
              "email": "dup.integration@test.com",
              "password": "123",
              "telefono": "666",
              "rol": "USER",
              "fechaRegistro": null,
              "activo": true
            }
        """;

        // 1ª vez -> 201
        this.mockMvc
                .perform(post(REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // 2ª vez mismo email -> 409 Conflict
        this.mockMvc
                .perform(post(REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    /**
     * Test de integración del endpoint PISTAS
     */
    @Test
    void creaPistaOkTest() throws Exception{
        Pista pista = new Pista(
                        1,
                        "Madrid central 1",
                        "Madrid",
                        10,
                        true,
                        "2026-02-15");

        mockMvc.perform(post("/pistaPadel/courts")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(pista)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Madrid central 1"));
    }

    @Test
    void creaPistaIncorrectoTest() throws Exception{
        Pista pista = new Pista(
                        1,
                        "Madrid central 1",
                        "Madrid",
                        10,
                        true,
                        "2026-02-15");

        mockMvc.perform(post("/pistaPadel/courts")
                    .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                    .content(String.valueOf(pista)))
                .andExpect(status().isBadRequest());
    }


    /**
     * Test de integración del endpoint RESERVAS
     * Prueba la persistencia en BD
     */

    
    // Metodo privado para crear una pista con rol de admin
    private Long crearPistaPrueba() throws Exception {
        Pista nuevaPista = new Pista(
                0L,
                "Pista-Test-" + System.currentTimeMillis(),
                "Test Location",
                2800L,
                true,
                "2025-02-01"
        );
    
        MvcResult result = mockMvc.perform(post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaPista))
                        .with(user("admin-temp").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPista").isNumber())
                .andReturn();
    
        String json = result.getResponse().getContentAsString();
        Pista creada = objectMapper.readValue(json, Pista.class);
        return creada.idPista();
    }
    
    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("Crear reserva persiste en BD")
    void crearReserva_persisteEnBD() throws Exception {
        Long courtId = crearPistaPrueba();
        assertThat(repoReserva.count()).isEqualTo(0);
    
        var reserva = new ReservaDTO(
                courtId,
                "user1",
                LocalDateTime.of(2026, 9, 10, 16, 0),
                LocalDateTime.of(2026, 9, 10, 17, 0)
        );
    
        MvcResult result = mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserva))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        // Verificar que la reserva está en la BD
        assertThat(repoReserva.count()).isEqualTo(1);
        
        // Extraer el ID de la respuesta y verificar datos
        String json = result.getResponse().getContentAsString();
        Reserva reservaCreada = objectMapper.readValue(json, Reserva.class);
        
        Optional<Reserva> reservaEnBD = repoReserva.findById(reservaCreada.getReservationId());
        assertThat(reservaEnBD).isPresent();
        assertThat(reservaEnBD.get().getUserId()).isEqualTo("user1");
        assertThat(reservaEnBD.get().getCourtId()).isEqualTo(courtId);
        assertThat(reservaEnBD.get().getInicio()).isEqualTo(LocalDateTime.of(2026, 9, 10, 16, 0));
    }
    
    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("Obtener reserva devuelve datos persistidos")
    void obtenerReserva_devuelveDatosPersistidos() throws Exception {
        Long courtId = crearPistaPrueba();
        
        // Crear una reserva
        var reserva = new ReservaDTO(
                courtId,
                "user1",
                LocalDateTime.of(2026, 8, 15, 10, 0),
                LocalDateTime.of(2026, 8, 15, 11, 0)
        );
    
        MvcResult createResult = mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserva))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
        
        String json = createResult.getResponse().getContentAsString();
        Reserva reservaCreada = objectMapper.readValue(json, Reserva.class);
        Long reservaId = reservaCreada.getReservationId();
        
        // Obtener la reserva y verificar que los datos se recuperan de la BD
        mockMvc.perform(get("/pistaPadel/reservations/" + reservaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.courtId").value(courtId))
                .andExpect(jsonPath("$.reservationId").value(reservaId));
    }
    
    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("Reprogramar reserva actualiza persistencia")
    void reprogramarReserva_actualizaPersistencia() throws Exception {
        Long courtId = crearPistaPrueba();
        
        // Crear reserva original
        var reservaOriginal = new ReservaDTO(
                courtId,
                "user1",
                LocalDateTime.of(2026, 7, 20, 14, 0),
                LocalDateTime.of(2026, 7, 20, 15, 0)
        );
    
        MvcResult createResult = mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservaOriginal))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
        
        String json = createResult.getResponse().getContentAsString();
        Reserva reservaCreada = objectMapper.readValue(json, Reserva.class);
        Long reservaId = reservaCreada.getReservationId();
        
        // Reprogramar la reserva
        var reservaNueva = new ReservaDTO(
                courtId,
                "user1",
                LocalDateTime.of(2026, 7, 20, 16, 0),
                LocalDateTime.of(2026, 7, 20, 17, 0)
        );
        
        mockMvc.perform(patch("/pistaPadel/reservations/" + reservaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservaNueva))
                        .with(csrf()))
                .andExpect(status().isOk());
        
        // Verificar que la BD tiene los datos actualizados
        Optional<Reserva> reservaActualizada = repoReserva.findById(reservaId);
        assertThat(reservaActualizada).isPresent();
        assertThat(reservaActualizada.get().getInicio()).isEqualTo(LocalDateTime.of(2026, 7, 20, 16, 0));
        assertThat(reservaActualizada.get().getFin()).isEqualTo(LocalDateTime.of(2026, 7, 20, 17, 0));
    }
    
    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("Cancelar reserva elimina de persistencia")
    void cancelarReserva_eliminaDePersistencia() throws Exception {
        Long courtId = crearPistaPrueba();
        
        // Crear una reserva
        var reserva = new ReservaDTO(
                courtId,
                "user1",
                LocalDateTime.of(2026, 6, 10, 12, 0),
                LocalDateTime.of(2026, 6, 10, 13, 0)
        );
    
        MvcResult createResult = mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserva))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
        
        String json = createResult.getResponse().getContentAsString();
        Reserva reservaCreada = objectMapper.readValue(json, Reserva.class);
        Long reservaId = reservaCreada.getReservationId();
        
        // Verificar que existe en la BD
        assertThat(repoReserva.findById(reservaId)).isPresent();
        
        // Cancelar la reserva
        mockMvc.perform(delete("/pistaPadel/reservations/" + reservaId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        // Verificar que fue eliminada de la BD
        assertThat(repoReserva.findById(reservaId)).isEmpty();
    }
    
    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("Solapamiento de reservas → 409 Conflict")
    void solapamiento_da_409() throws Exception {
        Long courtId = crearPistaPrueba();
    
        var r1 = new ReservaDTO(courtId, "user1", LocalDateTime.of(2026, 10, 5, 10, 0), LocalDateTime.of(2026, 10, 5, 11, 30));
        mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r1))
                        .with(csrf()))
                .andExpect(status().isCreated());
        
        assertThat(repoReserva.count()).isEqualTo(1);
    
        var r2 = new ReservaDTO(courtId, "user2", LocalDateTime.of(2026, 10, 5, 10, 45), LocalDateTime.of(2026, 10, 5, 11, 45));
        mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r2))
                        .with(csrf()))
                .andExpect(status().isConflict());
        
        // La segunda no se guardó, sigue habiendo solo 1
        assertThat(repoReserva.count()).isEqualTo(1);
    }
    
    @Test
    @WithMockUser(username = "admin-test", roles = "ADMIN")
    @DisplayName("Admin lista todas las reservas persistidas")
    void admin_lista_reservas_persistidas() throws Exception {
        assertThat(repoReserva.count()).isEqualTo(0);
        
        mockMvc.perform(get("/pistaPadel/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private record ReservaDTO(Long courtId, String userId, LocalDateTime inicio, LocalDateTime fin) {}
    }




