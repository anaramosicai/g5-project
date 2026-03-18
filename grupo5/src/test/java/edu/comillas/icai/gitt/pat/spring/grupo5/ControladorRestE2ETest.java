package edu.comillas.icai.gitt.pat.spring.grupo5;

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

    private static final String REGISTER = "/auth/register";

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
                "/pistas",
                pista,
                Pista.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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
/*
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
*/

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
    public void actualizarUsuario_EmailDuplicado_Test(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Creamos el primer usuario:
        //ResponseEntity<Usuario> response1 =
        restTemplate.exchange(
                "/pistaPadel/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(
                        """
                           {
                            "idUsuario": "1",
                            "nombre": "Juan",
                            "apellidos": "Lovato",
                            "email": "juan@ejemplo.com",
                            "password": "123456",
                            "telefono": "123456789",
                            "rol": "USER",
                            "fechaRegistro": null,
                            "activo": true
                           }
                        """, headers),
                String.class
        );


        // Creamos un segundo usuario:
        //ResponseEntity<Usuario> response2 =
        restTemplate.exchange(
                "/pistaPadel/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(
                        """
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
                        """, headers),
                String.class
        );

        // Intentamos poner el email de uno al otro para ver si salta error:
        String cambios = """
                {
                "email": "mod@ejemplo.com"
                }
                """;
        ResponseEntity<String> response = restTemplate.exchange(
                "/pistaPadel/users/1",
                HttpMethod.PATCH,
                new HttpEntity<>(cambios, headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

    }

    // ============== RESERVAS ==============
        /**
         * Test E2E del endpoint real  RESERVAS
         */
        
        
        private String baseUrl;
        private Long courtId;

        @BeforeEach
        void setup() {
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
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("usuario", "clave");
        
        Reserva reservaNueva = new Reserva();
        reservaNueva.pista = new Pista(courtId, "Pista Test E2E", "Indoor", 3500, true, "2026-01-01");
        reservaNueva.inicio = LocalDateTime.of(2026, 5, 20, 16, 0);
        reservaNueva.fin = LocalDateTime.of(2026, 5, 20, 17, 0);
        
        // Crear reserva
        ResponseEntity<Reserva> createResp = restTemplate.exchange(
                baseUrl + "/reservations",
                HttpMethod.POST,
                new HttpEntity<>(reservaNueva, headers),
                Reserva.class
        );
        
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long reservationId = createResp.getBody().reservationId;
        
        // Obtener detalle
        ResponseEntity<Reserva> getResp = restTemplate.exchange(
                baseUrl + "/reservations/" + reservationId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Reserva.class
        );
        
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().inicio).isEqualTo(reservaNueva.inicio);
        
        // Cancelar
        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                baseUrl + "/reservations/" + reservationId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );
        
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
}

