
package edu.comillas.icai.gitt.pat.spring.grupo5;

import edu.comillas.icai.gitt.pat.spring.grupo5.controlador.ControladorREST;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.*;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.*;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.*;
import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.PistaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;



/**
 * Test de integración de Registros/ Usuarios
 * Verifica:
 *  - 201 Created con datos válidos
 *  - 409 Conflict con email duplicado
 */
//@WebMvcTest(ControladorREST.class)
//@AutoConfigureMockMvc(addFilters = false)



@DataJpaTest
class IntegrationTest {

    //@Autowired
    //private MockMvc mockMvc;

    @Autowired
    private RepoReserva repoReserva;

    @Autowired
    private RepoUsuario repoUsuario;

    private static final String REGISTER = "/pistaPadel/auth/register";


    @Autowired
    private RepoPista repoPista;

    /*
    @BeforeEach
    void setup_user() {
        controladorREST.reset();
    }
    */


    @Test
    void registro_ok_201() throws Exception {

        Usuario user = new Usuario(1L, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, null, true);
        repoUsuario.save(user);
        assertNotNull(repoUsuario.findById(user.getId()));
    }


    @Test
    void registro_emailDuplicado_409() throws Exception {
        Usuario user = new Usuario(1L, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, null, true);

        // Guardo por primera vez:
        repoUsuario.save(user);
        // Guardo por segunda vez:
        DataIntegrityViolationException error = null;
        try {
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
    void createPista() {

        Pista pista = new Pista(
                "Central",
                "Madrid",
                20,
                true,
                "2026-03-08"
        );

        repoPista.save(pista);
        Pista encontrada = repoPista.findById(pista.id).orElse(null);

        assertNotNull(encontrada);
        assertThat(encontrada.nombre).isEqualTo("Central");
    }

    @Test
    void createPistaWithDuplicateNameShouldFail() {
        Pista pista1 = new Pista("central", "Madrid", 20, true, "2026-03-08");

        Pista pista2 = new Pista("central", "Barcelona", 25, true, "2026-03-09");

        repoPista.save(pista1);
        DataIntegrityViolationException error = null;
        try {
            repoPista.save(pista2);
        } catch (DataIntegrityViolationException e) {
            error = e;
        }
        assertNotNull(error);
    }



    // Metodo privado para crear una pista con rol de admin
    private Long crearPistaPrueba() throws Exception {
        Pista nuevaPista = new Pista(

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
        return creada.id;
    }
    

     * Test de integración del endpoint RESERVAS
     *//*

    
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
        
        // Extraer el ID de la respuesta y verificar datos
        String json = result.getResponse().getContentAsString();
        Reserva reservaCreada = objectMapper.readValue(json, Reserva.class);
        
        Optional<Reserva> reservaEnBD = repoReserva.findById(reservaCreada.getReservationId());
        assertThat(reservaEnBD).isPresent();
        assertThat(reservaEnBD.get().getUsuario().getId()).isEqualTo("user1");
        assertThat(reservaEnBD.get().getPista().id).isEqualTo(courtId);
        assertThat(reservaEnBD.get().getInicio()).isEqualTo(LocalDateTime.of(2026, 9, 10, 16, 0));
    }
    
    @Test
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
                        .with(user("user1").roles("USER"))
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
                        .with(user("user1").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk());
        
        // Verificar que la BD tiene los datos actualizados
        Optional<Reserva> reservaActualizada = repoReserva.findById(reservaId);
        assertThat(reservaActualizada).isPresent();
        assertThat(reservaActualizada.get().getInicio()).isEqualTo(LocalDateTime.of(2026, 7, 20, 16, 0));
        assertThat(reservaActualizada.get().getFin()).isEqualTo(LocalDateTime.of(2026, 7, 20, 17, 0));
    }
    
    @Test
    @DisplayName("Solapamiento de reservas → 409 Conflict")
    void solapamiento_da_409() throws Exception {
        Long courtId = crearPistaPrueba();
    
        var r1 = new ReservaDTO(courtId, "user1", LocalDateTime.of(2026, 10, 5, 10, 0), LocalDateTime.of(2026, 10, 5, 11, 30));
        mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r1))
                        .with(user("user1").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isCreated());
        
        assertThat(repoReserva.count()).isEqualTo(1);
    
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

    private record ReservaDTO(Long courtId, String userId, LocalDateTime inicio, LocalDateTime fin) {}
    }



/**
 * Test de integración de Registros/ Usuarios
 * Verifica:
 *  - 201 Created con datos válidos
 *  - 409 Conflict con email duplicado
 */
/*
//@WebMvcTest(ControladorREST.class)
//@AutoConfigureMockMvc(addFilters = false)



@DataJpaTest
class IntegrationTest {

    @Autowired
    private RepoReserva repoReserva;

    @Autowired
    private RepoUsuario repoUsuario;

    private static final String REGISTER = "/pistaPadel/auth/register";

    @Autowired
    private RepoPista repoPista;


/*
    @BeforeEach
    void setup_user() {
        controladorREST.reset();
    }*//*




    @Test
    void registro_ok_201() throws Exception {

        Usuario user = new Usuario(null, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, LocalDateTime.now(),true);
        repoUsuario.save(user);
        assertNotNull(repoUsuario.findById(user.getId()));
    }


    @Test
    void registro_emailDuplicado_409() throws Exception {
        Usuario user1 = new Usuario(null, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, LocalDateTime.now(),true);
        Usuario user2 = new Usuario(null, "Martina", "Ortiz", "ana.integration@test.com", "789", "030", NombreRol.USER, LocalDateTime.now(),true);

        // Guardo por primera vez:
        repoUsuario.save(user1);
        // Guardo por segunda vez:
        DataIntegrityViolationException error = null;
        try{
            repoUsuario.save(user2);
        } catch (DataIntegrityViolationException e) {
            error = e;
        }
        // Nos aseguramos que salte el error:
        assertNotNull(error);
    }

    @Test
    void borrado_registros() throws Exception{
        // Creo dos registros cualquiera para llenar mi repo:
        Usuario user1 = new Usuario(null, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, LocalDateTime.now(),true);
        Usuario user2 = new Usuario(null, "Martina", "Ortiz", "skinny_legend.integration@test.com", "789", "030", NombreRol.USER, LocalDateTime.now(),true);
        repoUsuario.save(user1);
        repoUsuario.save(user2);

        // Probamos que los datos están en el repo:
        Assertions.assertTrue(repoUsuario.count() > 0);


        // Probamos a borrar toda la tabla:
        repoUsuario.deleteAll();
        // Pruebo que está vacío el repo:
        Assertions.assertEquals(0, repoUsuario.count());
    }
   

   Test de integración del endpoint PISTAS




    
        // Metodo privado para crear una pista de prueba
    private Long crearPistaPrueba() {
        Pista nuevaPista = new Pista(
                null,
                "Pista-Test-" + System.currentTimeMillis(),
                "Test Location",
                2800L,
                true,
                "2025-02-01"
        );
        Pista guardada = repoPista.save(nuevaPista);
        return guardada.id;
    }


     * Test de integración del endpoint RESERVAS


    @Test
    @DisplayName("Crear reserva persiste en BD")
    void crearReserva_persisteEnBD() {
        // Crear pista
        Long courtId = crearPistaPrueba();
        Pista pista = repoPista.findById(courtId).orElseThrow();
        
        // Crear usuario
        Usuario usuario = new Usuario(null, "User1", "Test", "user1@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        repoUsuario.save(usuario);
        
        assertThat(repoReserva.count()).isEqualTo(0);
        
        // Crear reserva
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 16, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 9, 10, 17, 0);
        
        Reserva reserva = new Reserva();
        reserva.usuario = usuario;
        reserva.pista = pista;
        reserva.inicio = inicio;
        reserva.fin = fin;
        reserva.fechaReservada = LocalDateTime.now();
        reserva.duracionMinutos = 60;
        reserva.estado = EstadoReserva.ACTIVA;
        reserva.fechaCreacion = LocalDateTime.now();
        
        Reserva guardada = repoReserva.save(reserva);
        
        // Verificar que la reserva está en la BD
        assertThat(repoReserva.count()).isEqualTo(1);
        
        Optional<Reserva> reservaEnBD = repoReserva.findById(guardada.reservationId);
        assertThat(reservaEnBD).isPresent();
        assertThat(reservaEnBD.get().usuario.getId()).isEqualTo(1L);
        assertThat(reservaEnBD.get().pista.id).isEqualTo(courtId);
        assertThat(reservaEnBD.get().inicio).isEqualTo(inicio);
    }
    
    @Test
    @DisplayName("Reprogramar reserva actualiza persistencia")
    void reprogramarReserva_actualizaPersistencia() {
        // Crear pista
        Long courtId = crearPistaPrueba();
        Pista pista = repoPista.findById(courtId).orElseThrow();
        
        // Crear usuario
        Usuario usuario = new Usuario(null, "User2", "Test", "user2@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        repoUsuario.save(usuario);
        
        // Crear reserva original
        LocalDateTime inicioOriginal = LocalDateTime.of(2026, 7, 20, 14, 0);
        LocalDateTime finOriginal = LocalDateTime.of(2026, 7, 20, 15, 0);
        
        Reserva reservaOriginal = new Reserva();
        reservaOriginal.usuario = usuario;
        reservaOriginal.pista = pista;
        reservaOriginal.inicio = inicioOriginal;
        reservaOriginal.fin = finOriginal;
        reservaOriginal.fechaReservada = LocalDateTime.now();
        reservaOriginal.duracionMinutos = 60;
        reservaOriginal.estado = EstadoReserva.ACTIVA;
        reservaOriginal.fechaCreacion = LocalDateTime.now();
        
        Reserva reservaGuardada = repoReserva.save(reservaOriginal);
        Long reservaId = reservaGuardada.reservationId;
        
        // Reprogramar la reserva
        reservaGuardada.inicio = LocalDateTime.of(2026, 7, 20, 16, 0);
        reservaGuardada.fin = LocalDateTime.of(2026, 7, 20, 17, 0);
        repoReserva.save(reservaGuardada);
        
        // Verificar que la BD tiene los datos actualizados
        Optional<Reserva> reservaActualizada = repoReserva.findById(reservaId);
        assertThat(reservaActualizada).isPresent();
        assertThat(reservaActualizada.get().inicio).isEqualTo(LocalDateTime.of(2026, 7, 20, 16, 0));
        assertThat(reservaActualizada.get().fin).isEqualTo(LocalDateTime.of(2026, 7, 20, 17, 0));
    }
    
    @Test
    @DisplayName("Solapamiento de reservas → 409 Conflict")
    void solapamiento_da_409() {
        // Crear pista
        Long courtId = crearPistaPrueba();
        Pista pista = repoPista.findById(courtId).orElseThrow();
        
        // Crear usuarios
        Usuario usuario1 = new Usuario(null, "User10", "Test", "user10@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        Usuario usuario2 = new Usuario(null, "User11", "Test", "user11@test.com", "pwd", "123456", NombreRol.USER, LocalDateTime.now(), true);
        repoUsuario.save(usuario1);
        repoUsuario.save(usuario2);
        
        // Primera reserva: 10:00 - 11:30
        Reserva r1 = new Reserva();
        r1.usuario = usuario1;
        r1.pista = pista;
        r1.inicio = LocalDateTime.of(2026, 10, 5, 10, 0);
        r1.fin = LocalDateTime.of(2026, 10, 5, 11, 30);
        r1.fechaReservada = LocalDateTime.now();
        r1.duracionMinutos = 90;
        r1.estado = EstadoReserva.ACTIVA;
        r1.fechaCreacion = LocalDateTime.now();
        
        repoReserva.save(r1);
        assertThat(repoReserva.count()).isEqualTo(1);
        
        // Segunda reserva: 10:45 - 11:45 (SOLAPADA)
        Reserva r2 = new Reserva();
        r2.usuario = usuario2;
        r2.pista = pista;
        r2.inicio = LocalDateTime.of(2026, 10, 5, 10, 45);
        r2.fin = LocalDateTime.of(2026, 10, 5, 11, 45);
        r2.fechaReservada = LocalDateTime.now();
        r2.duracionMinutos = 60;
        r2.estado = EstadoReserva.ACTIVA;
        r2.fechaCreacion = LocalDateTime.now();
        
        repoReserva.save(r2);
        
        assertThat(repoReserva.count()).isEqualTo(2);
    }

    private record ReservaDTO(Long courtId, String userId, LocalDateTime inicio, LocalDateTime fin) {}
    }



