package edu.comillas.icai.gitt.pat.spring.grupo5;

import edu.comillas.icai.gitt.pat.spring.grupo5.controlador.ControladorREST;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoPista;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    
    
    // ========================================
    // TESTS DE RESERVAS
    // ========================================

    @Test
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
                        .with(user("user1").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        // Verificar que la reserva está en la BD
        assertThat(repoReserva.count()).isEqualTo(1);
        
        String json = result.getResponse().getContentAsString();
        Reserva reservaCreada = objectMapper.readValue(json, Reserva.class);
        
        Optional<Reserva> reservaEnBD = repoReserva.findById(reservaCreada.reservationId);
        assertThat(reservaEnBD).isPresent();
        assertThat(reservaEnBD.get().userId).isEqualTo("user1");
        assertThat(reservaEnBD.get().courtId).isEqualTo(courtId);
        assertThat(reservaEnBD.get().inicio).isEqualTo(LocalDateTime.of(2026, 9, 10, 16, 0));
        assertThat(reservaEnBD.get().fin).isEqualTo(LocalDateTime.of(2026, 9, 10, 17, 0));
    }
    
    @Test
    @DisplayName("Obtener reserva retorna datos de BD")
    void obtenerReserva_retornaDatosDeBD() throws Exception {
        Long courtId = crearPistaPrueba();
        
        var reserva = new ReservaDTO(
                courtId,
                "user1",
                LocalDateTime.of(2026, 8, 15, 10, 0),
                LocalDateTime.of(2026, 8, 15, 11, 0)
        );
    
        MvcResult createResult = mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserva))
                        .with(user("user1").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
        
        String json = createResult.getResponse().getContentAsString();
        Reserva reservaCreada = objectMapper.readValue(json, Reserva.class);
        Long reservaId = reservaCreada.reservationId;
        
        // Obtener desde BD y verificar
        Optional<Reserva> reservaEnBD = repoReserva.findById(reservaId);
        assertThat(reservaEnBD).isPresent();
        assertThat(reservaEnBD.get().userId).isEqualTo("user1");
        assertThat(reservaEnBD.get().courtId).isEqualTo(courtId);
        assertThat(reservaEnBD.get().inicio).isEqualTo(LocalDateTime.of(2026, 8, 15, 10, 0));
    }
    
    @Test
    @DisplayName("Reprogramar reserva actualiza BD")
    void reprogramarReserva_actualizaBD() throws Exception {
        Long courtId = crearPistaPrueba();
        
        var reservaOriginal = new ReservaDTO(
                courtId,
                "user1",
                LocalDateTime.of(2026, 7, 20, 14, 0),
                LocalDateTime.of(2026, 7, 20, 15, 0)
        );
    
        MvcResult createResult = mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservaOriginal))
                        .with(user("user1").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
        
        String json = createResult.getResponse().getContentAsString();
        Reserva reservaCreada = objectMapper.readValue(json, Reserva.class);
        Long reservaId = reservaCreada.reservationId;
        
        var reservaNueva = new ReservaDTO(
                courtId,
                "user1",
                LocalDateTime.of(2026, 7, 20, 16, 0),
                LocalDateTime.of(2026, 7, 20, 17, 0)
        );
        
        mockMvc.perform(patch("/pistaPadel/reservations/" + reservaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservaNueva))
                        .with(user("user1").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk());
        
        // Verificar que la BD tiene los datos actualizados
        Optional<Reserva> reservaActualizada = repoReserva.findById(reservaId);
        assertThat(reservaActualizada).isPresent();
        assertThat(reservaActualizada.get().inicio).isEqualTo(LocalDateTime.of(2026, 7, 20, 16, 0));
        assertThat(reservaActualizada.get().fin).isEqualTo(LocalDateTime.of(2026, 7, 20, 17, 0));
    }
    
    @Test
    @DisplayName("Cancelar reserva elimina de BD")
    void cancelarReserva_eliminaDeBD() throws Exception {
        Long courtId = crearPistaPrueba();
        
        var reserva = new ReservaDTO(
                courtId,
                LocalDateTime.of(2026, 6, 10, 12, 0),
                LocalDateTime.of(2026, 6, 10, 13, 0)
        );
    
        MvcResult createResult = mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserva))
                        .with(user("user1").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
        
        String json = createResult.getResponse().getContentAsString();
        Reserva reservaCreada = objectMapper.readValue(json, Reserva.class);
        Long reservaId = reservaCreada.reservationId;
        
        // Verificar que existe en la BD
        assertThat(repoReserva.findById(reservaId)).isPresent();
        
        // Cancelar la reserva
        mockMvc.perform(delete("/pistaPadel/reservations/" + reservaId)
                        .with(user("user1").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
        
        // Verificar que fue eliminada de la BD
        assertThat(repoReserva.findById(reservaId)).isEmpty();
    }
    
    @Test
    @DisplayName("Reservas solapadas → 409 Conflict, no persisten")
    void reservasSolapadas_noPeristen() throws Exception {
        Long courtId = crearPistaPrueba();
        assertThat(repoReserva.count()).isEqualTo(0);
    
        // Primera reserva: 10:00-11:30
        var r1 = new ReservaDTO(courtId, "user1", LocalDateTime.of(2026, 10, 5, 10, 0), LocalDateTime.of(2026, 10, 5, 11, 30));
        mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r1))
                        .with(user("user1").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isCreated());
        
        assertThat(repoReserva.count()).isEqualTo(1);
    
        // Segunda reserva: 10:45-11:45 (solapada)
        var r2 = new ReservaDTO(courtId, "user2", LocalDateTime.of(2026, 10, 5, 10, 45), LocalDateTime.of(2026, 10, 5, 11, 45));
        mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r2))
                        .with(user("user2").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isConflict());
        
        // La segunda no se guardó, sigue habiendo solo 1
        assertThat(repoReserva.count()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Admin lista todas las reservas de BD")
    void admin_listaTodasReservas() throws Exception {
        Long courtId = crearPistaPrueba();
        
        // Crear un par de reservas
        var r1 = new ReservaDTO(courtId, "user1", LocalDateTime.of(2026, 5, 1, 10, 0), LocalDateTime.of(2026, 5, 1, 11, 0));
        mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r1))
                        .with(user("user1").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isCreated());
        
        var r2 = new ReservaDTO(courtId, "user2", LocalDateTime.of(2026, 5, 2, 14, 0), LocalDateTime.of(2026, 5, 2, 15, 0));
        mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r2))
                        .with(user("user2").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isCreated());
        
        // Verificar BD
        assertThat(repoReserva.count()).isEqualTo(2);
        
        // Admin obtiene todas
        mockMvc.perform(get("/pistaPadel/admin/reservations")
                .with(user("admin-test").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private record ReservaDTO(Long courtId, String userId, LocalDateTime inicio, LocalDateTime fin) {}
    }
