package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;



/**
 * Test de integración del endpoint POST /pistaPadel/auth/register
 * Verifica:
 *  - 201 Created con datos válidos
 *  - 400 Bad Request con email inválido
 *  - 409 Conflict con email duplicado
 */
@WebMvcTest(ControladorREST.class)
@AutoConfigureMockMvc(addFilters = false)
class ControladorRestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String REGISTER = "/pistaPadel/auth/register";

    private Pista pista;

    @BeforeEach
    void setUp(){
        pista = new Pista(
        1,
        "Madrid central 1",
        "Madrid",
        10,
        true,
        "2026-02-15");
    }

    @Test
    void creaPistaOkTest() throws Exception{
        mockMvc.perform(post("/pistaPadel/courts")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(pista)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Madrid central 1"));
    }

    @Test
    void creaPistaIncorrectoTest() throws Exception{
        mockMvc.perform(post("/pistaPadel/courts")
                    .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                    .content(String.valueOf(pista)))
                .andExpect(status().isBadRequest());
    }

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

    /**
     * Test de integración del endpoint RESERVAS
     */
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Método privado para crear una pista con rol de admin
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
    
    private record ReservaDTO(Long courtId, String userId, LocalDateTime inicio, LocalDateTime fin) {}
    }
}
