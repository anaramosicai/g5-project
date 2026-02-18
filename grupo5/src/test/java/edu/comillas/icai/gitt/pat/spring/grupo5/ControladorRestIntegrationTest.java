package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

/**
 * Test de integración del Controlador REST
 * Pruebas para:
 *  - POST /pistaPadel/auth/register (registro de usuarios)
 *  - GET /pistaPadel/users (obtención de usuarios)
 *  - POST /pistaPadel/courts (creación de pistas)
 *  - POST /pistaPadel/reservations (creación de reservas)
 * 
 * Configuración:
 *  - @WebMvcTest para tests sin arrancar contexto completo
 *  - MockMvc para realizar requests simuladas
 *  - @AutoConfigureMockMvc para configuración automática
 *  - @WithMockUser para simular usuarios autenticados
 */
@WebMvcTest(ControladorREST.class)
@Import(ConfigSeguridad.class)
@AutoConfigureMockMvc
class ControladorRestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ControladorREST controladorREST;

    private static final String REGISTER = "/pistaPadel/auth/register";

    @BeforeEach
    void setup() {
        controladorREST.reset();
    }

    // ========== TESTS DE REGISTRO ==========

    @Test
    void registro_ok_201() throws Exception {
        String body = """
            {
              "idUsuario": null,
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

    @Test
    void registro_emailFormatoErroneo_400() throws Exception {
        String body = """
            {
              "idUsuario": null,
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

    @Test
    void registro_emailDuplicado_409() throws Exception {
        String body = """
            {
              "idUsuario": null,
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

    // ========== TESTS DE USUARIOS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerUsuarioporIdTest_OK() throws Exception {
        String usuario = """
                {
                "idUsuario": "5",
                "nombre": "Martina",
                "apellidos": "Ortiz",
                "email": "mod@ejemplo.com",
                "password": "123456",
                "telefono": "123456789",
                "rol": "USER",
                "fechaRegistro": null,
                "activo": true
                }
                """;
        // Simulo el POST previo al GET
        this.mockMvc
                .perform(post(REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuario))
                .andExpect(status().isCreated());

        // Simulo el GET al id 1:
        this.mockMvc
                .perform(get("/pistaPadel/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Martina"))
                .andExpect(jsonPath("$.email").value("mod@ejemplo.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerUsuarioporId_NoExistente() throws Exception {
        mockMvc.perform(get("/pistaPadel/users/33"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerUsuarioporId_SinPermiso() throws Exception {
        String usuario = """
            {
                "idUsuario": "0",
                "nombre": "Carla",
                "apellidos": "Col",
                "email": "caracol@ejemplo.com",
                "password": "123456",
                "telefono": "123456789",
                "rol": "USER",
                "fechaRegistro": null,
                "activo": true
                }
            """;

        // POST sin usuario autenticado
        mockMvc.perform(post(REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuario))
                .andExpect(status().isCreated());

        // GET con usuario sin permisos
        mockMvc.perform(get("/pistaPadel/users/1")
                        .with(user("test").roles("USER")))
                .andExpect(status().isForbidden());
    }

    // ========== TESTS DE PISTAS ==========
    
    /**
     * Método privado para crear una pista con rol de admin
     */
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
    @DisplayName("Usuario normal crea reserva OK")
    void crearReserva_OK() throws Exception {
        Long courtId = crearPistaPrueba();

        var reserva = new ReservaDTO(
                courtId,
                "user1",
                LocalDateTime.of(2026, 9, 10, 16, 0),
                LocalDateTime.of(2026, 9, 10, 17, 0)
        );

        mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserva))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("Solapamiento → 409 Conflict")
    void solapamiento_da_409() throws Exception {
        Long courtId = crearPistaPrueba();

        var r1 = new ReservaDTO(courtId, "user1", LocalDateTime.of(2026, 10, 5, 10, 0), LocalDateTime.of(2026, 10, 5, 11, 30));
        mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r1))
                        .with(csrf()))
                .andExpect(status().isCreated());

        var r2 = new ReservaDTO(courtId, "user2", LocalDateTime.of(2026, 10, 5, 10, 45), LocalDateTime.of(2026, 10, 5, 11, 45));
        mockMvc.perform(post("/pistaPadel/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r2))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin-test", roles = "ADMIN")
    @DisplayName("Admin lista todas las reservas")
    void admin_lista_reservas() throws Exception {
        mockMvc.perform(get("/pistaPadel/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * DTO para representar una reserva en los tests
     */
    private record ReservaDTO(Long courtId, String userId, LocalDateTime inicio, LocalDateTime fin) {}
}

