package edu.comillas.icai.gitt.pat.spring.grupo5;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.comillas.icai.gitt.pat.spring.grupo5.controlador.ControladorREST;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Reserva;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.web.client.TestRestTemplate; //<-- no va y la dependency está incluída ¯_ (ツ)_/¯
//import org.springframework.boot.resttestclient.TestRestTemplate;

import org.springframework.boot.test.web.client.TestRestTemplate;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test E2E del endpoint real POST /pistaPadel/auth/register
 *  - RANDOM_PORT
 *  - TestRestTemplate
 *  - ActiveProfiles("test")
 *  - DirtiesContext
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.task.scheduling.enabled=false",
                "spring.profiles.active=test"
        })
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ControladorRestE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ControladorREST controladorREST;

    private static final String REGISTER = "/pistaPadel/auth/register";

    private static final String COURT = "/pistaPadel/courts";

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/pistaPadel/courts";
    }



    // ============== PISTAS ==============
    @Test
    void createPistaE2E() {

        Pista pista = new Pista(
                1L,
                "Pista Central",
                "Madrid",
                20,
                true,
                null
        );

        ResponseEntity<Pista> response = restTemplate.postForEntity(
                "/pistaPadel/courts",
                pista,
                Pista.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().nombre).isEqualTo("Pista Central");
    }

    @Test
    void creaPistaOkTest() {
        Pista pista = new Pista(
                1L,
                "Madrid central 1",
                "Madrid",
                10,
                true,
                "2026-02-15");

        ResponseEntity<Pista> response = restTemplate.withBasicAuth("admin", "clave")
                .postForEntity(getBaseUrl(),
                        pista,
                        Pista.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().nombre).isEqualTo("Madrid central 1");
    }

    @Test
    void creaPistaIncorrectoTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("{ invalid json }", headers);

        ResponseEntity<String> response = restTemplate.withBasicAuth("admin", "clave")
                .postForEntity(getBaseUrl(),
                        request,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ============== REGISTROS Y USUARIOS ==============

    /*
    @BeforeEach
    void setup_user() {
        controladorREST.reset();
    }*/

    // Verificar que el login es correcto
    @Test
    void login_ok_200() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Registro previo
        restTemplate.exchange(
                REGISTER,
                HttpMethod.POST,
                new HttpEntity<>("""
            {
              "nombre": "Ana",
              "apellidos": "Ramos",
              "email": "login@test.com",
              "password": "123456",
              "telefono": "666"
            }
        """, headers),
                String.class
        );

        // Login
        ResponseEntity<String> response = restTemplate.exchange(
                "/pistaPadel/auth/login",
                HttpMethod.POST,
                new HttpEntity<>("""
                {
                  "email": "login@test.com",
                  "password": "123456"
                }""", headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("token"));
    }

    // Login incorrecto (contraseña distinta):
    @Test
    void login_passwordIncorrecta_401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Registro
        restTemplate.exchange(REGISTER, HttpMethod.POST,
                new HttpEntity<>("""
        {
          "nombre": "Ana",
          "apellidos": "Ramos",
          "email": "wrong@test.com",
          "password": "123456",
          "telefono": "666"
        }
        """, headers), String.class);

        // Login incorrecto
        ResponseEntity<String> response = restTemplate.exchange(
                "/pistaPadel/auth/login",
                HttpMethod.POST,
                new HttpEntity<>("""
        {
          "email": "wrong@test.com",
          "password": "xxx"
        }
        """, headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // Verificar que la autenticación del usuario (con Token) es correcta:
    @Test
    void me_conToken_200() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Registro
        restTemplate.exchange(REGISTER, HttpMethod.POST,
                new HttpEntity<>("""
        {
          "nombre": "Ana",
          "apellidos": "Ramos",
          "email": "me@test.com",
          "password": "123456",
          "telefono": "666"
        }
        """, headers), String.class);

        // Login
        ResponseEntity<String> login = restTemplate.exchange(
                "/pistaPadel/auth/login",
                HttpMethod.POST,
                new HttpEntity<>("""
        {
          "email": "me@test.com",
          "password": "123456"
        }
        """, headers), String.class);

        String token = new ObjectMapper()
                .readTree(login.getBody())
                .get("token").asText();

        headers.set("Authorization", "Bearer " + token);

        ResponseEntity<String> me = restTemplate.exchange(
                "/pistaPadel/auth/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        Assertions.assertEquals(HttpStatus.OK, me.getStatusCode());
    }

    // Verificar que salta UNAUTHORIZED cuando intento autenticarme sin Token:
    @Test
    void me_sinToken_401() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/pistaPadel/auth/me",
                HttpMethod.GET,
                null,
                String.class
        );

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // Verificar al cerrar sesión/ hacer logout:
    @Test
    void logout_ok_204() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Registro al usuario
        String registerBody = """
        {
          "nombre": "Ana",
          "apellidos": "Ramos",
          "email": "logout@test.com",
          "password": "123456",
          "telefono": "666"
        }
    """;

        ResponseEntity<String> r1 = restTemplate.exchange(
                REGISTER,
                HttpMethod.POST,
                new HttpEntity<>(registerBody, headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.CREATED, r1.getStatusCode());

        // Hacemos el login del usuario
        String loginBody = """
        {
          "email": "logout@test.com",
          "password": "123456"
        }
    """;

        ResponseEntity<String> login = restTemplate.exchange(
                "/pistaPadel/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginBody, headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.OK, login.getStatusCode());

        // Extraemos el Token del login:
        String token = new ObjectMapper()
                .readTree(login.getBody())
                .get("token").asText();

        // Añado el Token a la cabecera:
        headers.set("Authorization", "Bearer " + token);

        // Verifico el logout:
        ResponseEntity<Void> logout = restTemplate.exchange(
                "/pistaPadel/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Void.class
        );

        Assertions.assertEquals(HttpStatus.NO_CONTENT, logout.getStatusCode());
    }


    @Test
    void registro_ok_201() {
        String body = """
        {
          "nombre": "Ana",
          "apellidos": "Ramos",
          "email": "ana.e2e@test.com",
          "password": "123456",
          "telefono": "666"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                REGISTER, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void registro_emailFormatoErroneo_400() {
        String body = """
            {
              "idUsuario": 1,
              "nombre": "Ana",
              "apellidos": "Ramos",
              "email": "email-malo",
              "password": "123",
              "telefono": "666",
              "rol": "USER",
              "fechaRegistro": null,
              "activo": true
            }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                REGISTER, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    void registro_emailDuplicado_409() {
        String body = """
        {
          "nombre": "Ana",
          "apellidos": "Ramos",
          "email": "ana.e2e@test.com",
          "password": "123456",
          "telefono": "666"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1ª -> 201
        ResponseEntity<String> r1 = restTemplate.exchange(
                REGISTER, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        Assertions.assertEquals(HttpStatus.CREATED, r1.getStatusCode());

        // 2ª mismo email -> 409
        ResponseEntity<String> r2 = restTemplate.exchange(
                REGISTER, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        Assertions.assertEquals(HttpStatus.CONFLICT, r2.getStatusCode());
    }


    @Test
    public void actualizarUsuario_EmailDuplicado_Test() throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Creamos el primer usuario:
        ResponseEntity<String> response1 =
                restTemplate.exchange(
                        REGISTER,
                        HttpMethod.POST,
                        new HttpEntity<>(
                                """
                                   {
                                    "nombre": "Juan",
                                    "apellidos": "Lovato",
                                    "email": "juan@ejemplo.com",
                                    "password": "123456",
                                    "telefono": "123456789"
                                   }
                                """, headers),
                        String.class
                );

        Assertions.assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        // Extraer ID del JSON devuelto
        ObjectMapper mapper = new ObjectMapper();
        long idUser1 = mapper.readTree(response1.getBody()).get("id").asLong();

        // Creamos un segundo usuario:
        ResponseEntity<String> response2 =
                restTemplate.exchange(
                        REGISTER,
                        HttpMethod.POST,
                        new HttpEntity<>(
                                """
                                    {
                                    "nombre": "Martina",
                                    "apellidos": "Ortiz",
                                    "email": "mod@ejemplo.com",
                                    "password": "123456",
                                    "telefono": "123456789"
                                    }
                                """, headers),
                        String.class
                );

        Assertions.assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        long idUser2 = mapper.readTree(response2.getBody()).get("id").asLong();

        // Intentamos poner el email de uno al otro para ver si salta error:
        String cambios = """
                {
                "email": "mod@ejemplo.com"
                }
                """;
        ResponseEntity<String> response = restTemplate.exchange(
                "/pistaPadel/users/"+idUser1,
                HttpMethod.PATCH,
                new HttpEntity<>(cambios, headers),
                String.class
        );
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

    }


    // Para obtener un usuario por Id (teniendo en cuenta su Token)
    @Test
    void getUserById_ok_200() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> register = restTemplate.exchange(
                REGISTER,
                HttpMethod.POST,
                new HttpEntity<>("""
        {
          "nombre": "Ana",
          "apellidos": "Ramos",
          "email": "get@test.com",
          "password": "123456",
          "telefono": "666"
        }
        """, headers),
                String.class
        );

        long id = new ObjectMapper()
                .readTree(register.getBody())
                .get("id").asLong();

        // Hacemos el login:
        ResponseEntity<String> login = restTemplate.exchange(
                "/pistaPadel/auth/login",
                HttpMethod.POST,
                new HttpEntity<>("""
        {
          "email": "get@test.com",
          "password": "123456"
        }
        """, headers),
                String.class
        );
        // Guardo el token:
        String token = new ObjectMapper()
                .readTree(login.getBody())
                .get("token").asText();

        headers.set("Authorization", "Bearer " + token);

        // Hago el GET ya con el Token:
        ResponseEntity<String> response = restTemplate.exchange(
                "/pistaPadel/users/" + id,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // Intento acceder al GET por id sin estar autorizado:
    @Test
    void getUserById_sinAuth_401() {

        ResponseEntity<String> response = restTemplate.exchange(
                "/pistaPadel/users/1",
                HttpMethod.GET,
                null,
                String.class
        );

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }



    @Test
    void listarUsuarios_userNoAdmin_ok() throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Creo un usuario y hago el login:
        ResponseEntity<String> register = restTemplate.exchange(
                REGISTER,
                HttpMethod.POST,
                new HttpEntity<>("""
        {
          "nombre": "Martina",
          "apellidos": "Ortiz",
          "email": "get@test.com",
          "password": "123456",
          "telefono": "123"
        }
        """, headers),
                String.class
        );

        long id = new ObjectMapper()
                .readTree(register.getBody())
                .get("id").asLong();

        // Hacemos el login:
        ResponseEntity<String> login = restTemplate.exchange(
                "/pistaPadel/auth/login",
                HttpMethod.POST,
                new HttpEntity<>("""
        {
          "email": "get@test.com",
          "password": "123456"
        }
        """, headers),
                String.class
        );
        // Guardo el token:
        String token = new ObjectMapper()
                .readTree(login.getBody())
                .get("token").asText();

        headers.set("Authorization", "Bearer " + token);

        ResponseEntity<String> response = restTemplate.exchange(
        "/pistaPadel/users", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        // Devolverá 200 porque @PreAuthorize no está activo en este contexto de test:
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }




    // ============== RESERVAS ==============
    /**
     * Test E2E del endpoint real  RESERVAS
     */


    private String baseUrl;
    private Long courtId;

    @BeforeEach
    void setup() {
        baseUrl = getBaseUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "clave");

        Pista pista = new Pista(null, "Pista Test E2E", "Indoor", 3500, true, "2026-01-01");

        ResponseEntity<Pista> response = restTemplate.exchange(
                "/pistaPadel/courts",
                HttpMethod.POST,
                new HttpEntity<>(pista, headers),
                Pista.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        courtId = response.getBody().id;
    }

    @Test
    void flujoCompletoReservaComoUser() {

        // 1. Registrar usuario
        String registroJson = """
            {
              "nombre": "Test",
              "apellidos": "User",
              "email": "test.reserva@test.com",
              "password": "123456",
              "telefono": "666"
            }
            """;
        HttpHeaders registroHeaders = new HttpHeaders();
        registroHeaders.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(
                REGISTER,
                HttpMethod.POST,
                new HttpEntity<>(registroJson, registroHeaders),
                String.class
        );

        // 2. Login para obtener token
        String loginJson = """
            {
              "email": "test.reserva@test.com",
              "password": "123456"
            }
            """;
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> loginResp = restTemplate.exchange(
                "/pistaPadel/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginJson, loginHeaders),
                String.class
        );

        // Extraer token del JSON response
        String loginBody = loginResp.getBody();
        String token = loginBody.substring(loginBody.indexOf("\"token\":\"") + 9, loginBody.lastIndexOf("\""));


        // 3. Crear reserva con token
        HttpHeaders reservaHeaders = new HttpHeaders();
        reservaHeaders.setBearerAuth(token);

        Reserva reservaNueva = new Reserva();
        reservaNueva.pista = new Pista(courtId, "Pista Test E2E", "Indoor", 3500, true, "2026-01-01");
        reservaNueva.inicio = LocalDateTime.of(2026, 5, 20, 16, 0);
        reservaNueva.fin = LocalDateTime.of(2026, 5, 20, 17, 0);

        ResponseEntity<Reserva> createResp = restTemplate.exchange(
                "/pistaPadel/reservations",
                HttpMethod.POST,
                new HttpEntity<>(reservaNueva, reservaHeaders),
                Reserva.class
        );

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long reservationId = createResp.getBody().reservationId;

        // 4. Obtener detalle
        ResponseEntity<Reserva> getResp = restTemplate.exchange(
                "/pistaPadel/reservations/" + reservationId,
                HttpMethod.GET,
                new HttpEntity<>(reservaHeaders),
                Reserva.class
        );

        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().inicio).isEqualTo(reservaNueva.inicio);

        // 5. Cancelar
        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                "/pistaPadel/reservations/" + reservationId,
                HttpMethod.DELETE,
                new HttpEntity<>(reservaHeaders),
                Void.class
        );

        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}


