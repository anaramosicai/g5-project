package edu.comillas.icai.gitt.pat.spring.grupo5;

import edu.comillas.icai.gitt.pat.spring.grupo5.controlador.ControladorREST;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Disponibilidad;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Reserva;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.NombreRol;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoDisponibilidad;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoUsuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.DisponibilidadService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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
    private RepoPista repoPista;

    @Autowired
    private RepoDisponibilidad repoDisponibilidad;

    @Autowired
    private DisponibilidadService disponibilidadService;

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
    void createPista() {

        Pista pista = new Pista(
                1L,
                "Central",
                "Madrid",
                20,
                true,
                null
        );

        repoPista.save(pista);
        Pista encontrada = repoPista.findById(pista.id).orElse(null);

        assertNotNull(encontrada);
        assertThat(encontrada.nombre).isEqualTo("Central");
    }

    @Test
    void createPistaWithDuplicateNameShouldFail(){
        Pista pista1 = new Pista(1L,"Central","Madrid",20,true,null);
        Pista pista2 = new Pista(2L,"central","Barcelona",25,true,null);

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

    // ============== DISPONIBILIDAD ==============
    @Test
    @DisplayName("Disponibilidad sin reservas devuelve todas las franjas libres")
    void disponibilidadSinReservas_todasLasHorasLibres() {
        Pista pista = new Pista(null, "Pista Disp Test", "Ubicación Test", 50, true, "2026-03-18");
        pista = repoPista.save(pista);
        LocalDate fecha = LocalDate.now();

        Disponibilidad disp = disponibilidadService.disponibilidadPista(pista.id, fecha);

        assertThat(disp).isNotNull();
        assertThat(disp.getFranjasLibres()).isNotNull();
        assertThat(disp.getFranjasLibres().size()).isGreaterThan(0);
        assertThat(disp.getPista().id).isEqualTo(pista.id);
        assertThat(disp.getFecha()).isEqualTo(fecha);
    }

    @Test
    @DisplayName("Disponibilidad calcula correctamente el número de franjas (14)")
    void numeroDeFramjasCorrectas() {
        Pista pista = new Pista(null, "Pista Count Test", "Ubicación Test", 50, true, "2026-03-18");
        pista = repoPista.save(pista);
        LocalDate fecha = LocalDate.now();

        Disponibilidad disp = disponibilidadService.disponibilidadPista(pista.id, fecha);

        int esperadas = 14; // 8:00 a 22:00 = 14 bloques de 60 min
        assertThat(disp.getFranjasLibres().size()).isEqualTo(esperadas);
    }

    @Test
    @DisplayName("Disponibilidad descuenta las franjas con conflictos")
    void disponibilidadConReserva_descuentaFranja() {
        Pista pista = new Pista(null, "Pista Reserve Test", "Ubicación Test", 50, true, "2026-03-18");
        pista = repoPista.save(pista);
        LocalDate fecha = LocalDate.now();

        // Crear una reserva de 10:00 a 11:00
        LocalDateTime inicio = fecha.atTime(LocalTime.of(10, 0));
        LocalDateTime fin = fecha.atTime(LocalTime.of(11, 0));
        Reserva reserva = new Reserva(null, pista.id, "user123", inicio, fin);
        repoReserva.save(reserva);

        Disponibilidad disp = disponibilidadService.disponibilidadPista(pista.id, fecha);

        // Debe haber una franja menos (14 - 1 = 13)
        assertThat(disp.getFranjasLibres().size()).isEqualTo(13);

        // La franja de 10:00-11:00 NO debe estar en disponibles
        boolean franjaOcupada = disp.getFranjasLibres().stream()
                .anyMatch(f -> f.getInicio().equals(LocalTime.of(10, 0)));
        assertThat(franjaOcupada).isFalse();
    }

    @Test
    @DisplayName("Disponibilidad maneja múltiples reservas correctamente")
    void disponibilidadConMultiplesReservas() {
        Pista pista = new Pista(null, "Pista Multi Test", "Ubicación Test", 50, true, "2026-03-18");
        pista = repoPista.save(pista);
        LocalDate fecha = LocalDate.now();

        // 3 reservas en diferentes horas
        Reserva res1 = new Reserva(null, pista.id, "user1",
                fecha.atTime(LocalTime.of(10, 0)), fecha.atTime(LocalTime.of(11, 0)));
        Reserva res2 = new Reserva(null, pista.id, "user2",
                fecha.atTime(LocalTime.of(14, 0)), fecha.atTime(LocalTime.of(15, 0)));
        Reserva res3 = new Reserva(null, pista.id, "user3",
                fecha.atTime(LocalTime.of(20, 0)), fecha.atTime(LocalTime.of(21, 0)));
        repoReserva.save(res1);
        repoReserva.save(res2);
        repoReserva.save(res3);

        Disponibilidad disp = disponibilidadService.disponibilidadPista(pista.id, fecha);

        // Deben quedar 14 - 3 = 11 franjas libres
        assertThat(disp.getFranjasLibres().size()).isEqualTo(11);
    }

    @Test
    @DisplayName("Disponibilidad se persiste correctamente en BD")
    void disponibilidadSePersiste() {
        Pista pista = new Pista(null, "Pista Persist Test", "Ubicación Test", 50, true, "2026-03-18");
        pista = repoPista.save(pista);
        LocalDate fecha = LocalDate.now();

        Disponibilidad disp1 = disponibilidadService.disponibilidadPista(pista.id, fecha);
        Long id = disp1.getId();

        // Buscar por ID en BD
        Optional<Disponibilidad> disp2 = repoDisponibilidad.findById(id);

        assertThat(disp2).isPresent();
        assertThat(disp2.get().getId()).isEqualTo(disp1.getId());
        assertThat(disp2.get().getFecha()).isEqualTo(disp1.getFecha());
    }

    @Test
    @DisplayName("Disponibilidad general sin courtId devuelve disponibilidades de todas las pistas")
    void disponibilidadGeneralSinCourt_todasLasPistas() {
        Pista pista1 = new Pista(null, "Pista Gen 1", "Ubicación 1", 50, true, "2026-03-18");
        Pista pista2 = new Pista(null, "Pista Gen 2", "Ubicación 2", 50, true, "2026-03-18");
        pista1 = repoPista.save(pista1);
        pista2 = repoPista.save(pista2);
        LocalDate fecha = LocalDate.now();

        List<Disponibilidad> disponibilidades = disponibilidadService.disponibilidadGeneral(fecha, null);

        assertThat(disponibilidades.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Disponibilidad general con courtId devuelve solo esa pista")
    void disponibilidadGeneralConCourt_soloPista() {
        Pista pista1 = new Pista(null, "Pista Specific 1", "Ubicación 1", 50, true, "2026-03-18");
        Pista pista2 = new Pista(null, "Pista Specific 2", "Ubicación 2", 50, true, "2026-03-18");
        pista1 = repoPista.save(pista1);
        pista2 = repoPista.save(pista2);
        LocalDate fecha = LocalDate.now();

        List<Disponibilidad> disponibilidades = disponibilidadService.disponibilidadGeneral(fecha, pista1.id);

        assertThat(disponibilidades.size()).isEqualTo(1);
        assertThat(disponibilidades.get(0).getPista().id).isEqualTo(pista1.id);
    }

    @Test
    @DisplayName("Disponibilidad sin fecha lanza BadRequest")
    void disponibilidadSinFecha_lanzaBadRequest() {
        Pista pista = new Pista(null, "Pista Bad Req", "Ubicación Test", 50, true, "2026-03-18");
        pista = repoPista.save(pista);
        final Long pistaId = pista.id;

        org.springframework.web.server.ResponseStatusException exception = 
                org.junit.jupiter.api.Assertions.assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> disponibilidadService.disponibilidadPista(pistaId, null)
                );
        assertThat(exception.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Disponibilidad con courtId inválido lanza NotFound")
    void disponibilidadConCourtInvalido_lanzaNotFound() {
        Long courtIdInvalido = 99999L;
        LocalDate fecha = LocalDate.now();

        org.springframework.web.server.ResponseStatusException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> disponibilidadService.disponibilidadPista(courtIdInvalido, fecha)
                );
        assertThat(exception.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
    }
    }

