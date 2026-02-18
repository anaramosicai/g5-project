package edu.comillas.icai.gitt.pat.spring.grupo5;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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

    private static final String REGISTER = "/pistaPadel/auth/register";

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
}