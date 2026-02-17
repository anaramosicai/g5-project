package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(controllers = ControladorREST.class)
// Quito addFilters = false para que la seguridad aplique
@AutoConfigureMockMvc
public class ControladorRestIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    ///  ========== PARTE USERS ========== ///

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerUsuarioporIdTest_OK() throws Exception {

        // Defino un usuario cualquiera de tipo String (así funciona MockMvc)
        String usuario = """
                {
                "nombre": "Martina",
                "apellidos": "Ortiz",
                "email": "mod@test.com",
                "password": "123",
                "telefono": "123456789"
                }
                """;
        // Simulo el POST previo al GET
        mockMvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuario))
                        .andExpect(status().isCreated());
        // Simulo el GET al id 1:
        mockMvc.perform(get("/pistaPadel/users/1"))
                .andExpect(status().isOk())
                // Ademas de verificar el 200, verifico que mi devuelve mis datos
                .andExpect(jsonPath("$.nombre").value("Martina"))
                .andExpect(jsonPath("$.email").value("mod@test.com"));
        // Para evitar ir campo por campo, verifico solo dos, los mas clave*/

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerUsuarioporId_NoExistente() throws Exception{
        // Para verificar el error 404 (Como el usuario no existe, el rol es indiferente)
        mockMvc.perform(get("/pistaPadel/users/33"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void obtenerUsuarioporId_SinPermiso() throws Exception{
        String usuario = """
                {
                "nombre": "Martina",
                "apellidos": "Ortiz",
                "email": "mod@test.com",
                "password": "123",
                "telefono": "123456789"
                }
                """;
        // POST para crear el usuario
        mockMvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuario))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/pistaPadel/users/1"))
                .andExpect(status().isForbidden());
    }

    }

