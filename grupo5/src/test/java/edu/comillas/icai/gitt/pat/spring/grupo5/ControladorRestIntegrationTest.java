package edu.comillas.icai.gitt.pat.spring.grupo5;

import edu.comillas.icai.gitt.pat.spring.grupo5.controlador.ControladorREST;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.NombreRol;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoUsuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.PistaService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.LocalDateTime;



/**
 * Test de integración de Registros/ Usuarios
 * Verifica:
 *  - 201 Created con datos válidos
 *  - 409 Conflict con email duplicado
 */
//@WebMvcTest(ControladorREST.class)
//@AutoConfigureMockMvc(addFilters = false)



@DataJpaTest
class ControladorRestIntegrationTest {

    //@Autowired
    //private MockMvc mockMvc;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepoReserva repoReserva;

    @Autowired
    private RepoUsuario repoUsuario;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String REGISTER = "/pistaPadel/auth/register";

    @Autowired
    private ControladorREST controladorREST;

    @Autowired
    private PistaService pistaService;

    /*
    @BeforeEach
    void setup_user() {
        controladorREST.reset();
    }
    */


    @Test
    void registro_ok_201() throws Exception {

        Usuario user = new Usuario(1L, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, null,true);
        repoUsuario.save(user);
        assertNotNull(repoUsuario.findById(user.getId()));
    }


    @Test
    void registro_emailDuplicado_409() throws Exception {
        Usuario user = new Usuario(1L, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, null,true);

        // Guardo por primera vez:
        repoUsuario.save(user);
        // Guardo por segunda vez:
        DataIntegrityViolationException error = null;
        try{
            repoUsuario.save(user);
        } catch (DataIntegrityViolationException e) {
            error = e;
        }
        // Nos aseguramos que salte el error:
        assertNotNull(error);
    }

   
/*
     * Test de integración del endpoint PISTAS
     */

    @Test
    void createAndReadPista() {

        Pista pista = new Pista(
                1L,
                "Central",
                "Madrid",
                20,
                true,
                null
        );

        pistaService.crea(pista);

        Pista encontrada = pistaService.lee(1L);

        assertThat(encontrada).isNotNull();
        assertThat(encontrada.nombre).isEqualTo("Central");
    }

    @Test
    void createPistaWithDuplicateNameShouldFail(){

        Pista pista1 = new Pista(1L,"Central","Madrid",20,true,null);
        Pista pista2 = new Pista(2L,"central","Barcelona",25,true,null);

        pistaService.crea(pista1);

        Assertions.assertThrows(ResponseStatusException.class, () -> {
            pistaService.crea(pista2);
        });
    
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
