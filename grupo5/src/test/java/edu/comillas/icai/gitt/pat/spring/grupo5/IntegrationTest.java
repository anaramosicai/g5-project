package edu.comillas.icai.gitt.pat.spring.grupo5;

import edu.comillas.icai.gitt.pat.spring.grupo5.controlador.ControladorREST;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.*;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.*;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.*;
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

    /*
    @BeforeEach
    void setup_user() {
        controladorREST.reset();
    }
    */


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



    

    /*
     * Test de integración del endpoint RESERVAS
     */


    private record ReservaDTO(Long courtId, String userId, LocalDateTime inicio, LocalDateTime fin) {}
    }
