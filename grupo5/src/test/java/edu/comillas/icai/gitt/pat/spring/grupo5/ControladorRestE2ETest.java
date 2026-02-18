package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.Map;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test E2E del Controlador REST
 * Pruebas para:
 *  - POST /pistaPadel/auth/register (registro de usuarios)
 *  - PATCH /pistaPadel/users (actualización de usuarios)
 *  - POST /pistaPadel/reservations (reservas)
 * 
 * Configuración:
 *  - RANDOM_PORT
 *  - TestRestTemplate
 *  - ActiveProfiles("test")
 *  - DirtiesContext (limpia después de cada test)
 *  - spring.task.scheduling.enabled=false (desactiva tareas programadas)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.task.scheduling.enabled=false"})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ControladorRestE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ControladorREST controladorREST;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String REGISTER = "/pistaPadel/auth/register";

    @BeforeEach
    void setup() {
        controladorREST.reset();
        baseUrl = "http://localhost:" + port + "/pistaPadel";
        logger.info("=== SETUP: Base URL = {} ===", baseUrl);
                // Preparar estado interno similar a ControladorRestTestE2E_1
                controladorREST.pistas.clear();

                @SuppressWarnings("unchecked")
                Map<Long, Reserva> reservasMap = (Map<Long, Reserva>) ReflectionTestUtils.getField(controladorREST, "reservas");
                if (reservasMap != null) reservasMap.clear();

                ReflectionTestUtils.setField(controladorREST, "idPistaContador", 0L);
                ReflectionTestUtils.setField(controladorREST, "idReservaContador", 0L);

                Pista p1 = new Pista(
                                1L,
                                "Central",
                                "Exterior",
                                2000,
                                true,
                                LocalDate.now().toString()
                );
                controladorREST.pistas.put(1L, p1);
                ReflectionTestUtils.setField(controladorREST, "idPistaContador", 2L);
    }

        @Test
        void availability_sin_courtId_devuelve_disponible() {
                ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/availability?date=2026-03-21", String.class);
                logger.info("TEST E2E availability - Status: {}", response.getStatusCode());
                Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
                String body = response.getBody();
                Assertions.assertNotNull(body);
                Assertions.assertTrue(body.contains("\"disponible\":true") || body.contains("\"disponible\": true"));
                Assertions.assertTrue(body.contains("Hay disponibilidad"));
        }

    // ========== TESTS DE REGISTRO ==========

    @Test
    void registro_ok_201() {
        String body = """
            {
              "idUsuario": null,
              "nombre": "Ana",
              "apellidos": "Ramos",
              "email": "ana.e2e@test.com",
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

        logger.info("TEST: registro_ok_201 - Status: {}", response.getStatusCode());
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void registro_emailFormatoErroneo_400() {
        String body = """
            {
              "idUsuario": null,
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

        logger.info("TEST: registro_emailFormatoErroneo_400 - Status: {}", response.getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void registro_emailDuplicado_409() {
        String body = """
            {
              "idUsuario": null,
              "nombre": "Ana",
              "apellidos": "Ramos",
              "email": "dup.e2e@test.com",
              "password": "123",
              "telefono": "666",
              "rol": "USER",
              "fechaRegistro": null,
              "activo": true
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
        
        logger.info("TEST: registro_emailDuplicado_409 - Status: {}", r2.getStatusCode());
        Assertions.assertEquals(HttpStatus.CONFLICT, r2.getStatusCode());
    }

    // ========== TESTS DE ACTUALIZACIÓN DE USUARIOS ==========

    @Test
    void actualizarUsuario_EmailDuplicado_409() {
        logger.info("=== INICIO TEST: actualizarUsuario_EmailDuplicado_409 ===");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Creamos el primer usuario:
        ResponseEntity<String> response1 = restTemplate.exchange(
                REGISTER,
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
        logger.info("Primer usuario registrado, status: {}", response1.getStatusCode());

        // Creamos un segundo usuario:
        ResponseEntity<String> response2 = restTemplate.exchange(
                REGISTER,
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
        logger.info("Segundo usuario registrado, status: {}", response2.getStatusCode());

        // Intentamos asignar el email del segundo usuario al primero:
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

        logger.info("PATCH email duplicado, status: {}", response.getStatusCode());
        logger.info("PATCH response body: {}", response.getBody());

        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // ========== TESTS DE RESERVAS ==========

    @Test
    void crearPistaComoAdmin() {
        logger.info("=== INICIO TEST: crearPistaComoAdmin ===");

        // Crear pista con credenciales admin
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "clave");

        Pista pista = new Pista(0, "Pista Test E2E", "Indoor", 3500, true, "2026-01-01");

        ResponseEntity<Pista> response = restTemplate.exchange(
                baseUrl + "/courts",
                HttpMethod.POST,
                new HttpEntity<>(pista, headers),
                Pista.class
        );

        logger.info("Pista creada, status: {}", response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void flujoCompletoReservaComoUser() {
        logger.info("=== INICIO TEST: flujoCompletoReservaComoUser ===");

        // Primero, creamos una pista como ADMIN
        HttpHeaders adminHeaders = new HttpHeaders();
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);
        adminHeaders.setBasicAuth("admin", "clave");

        Pista pista = new Pista(0, "Pista Reservas E2E", "Indoor", 3500, true, "2026-01-01");

        ResponseEntity<Pista> pistaResp = restTemplate.exchange(
                baseUrl + "/courts",
                HttpMethod.POST,
                new HttpEntity<>(pista, adminHeaders),
                Pista.class
        );

        assertThat(pistaResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long courtId = pistaResp.getBody().idPista();
        logger.info("Pista creada con ID: {}", courtId);

        // Ahora, crear una reserva como USER
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setContentType(MediaType.APPLICATION_JSON);
        userHeaders.setBasicAuth("usuario", "clave");

        Reserva reservaNueva = new Reserva(
                0,
                courtId,
                "usuario",
                LocalDateTime.of(2026, 5, 20, 16, 0),
                LocalDateTime.of(2026, 5, 20, 17, 0)
        );

        // Crear reserva
        ResponseEntity<Reserva> createResp = restTemplate.exchange(
                baseUrl + "/reservations",
                HttpMethod.POST,
                new HttpEntity<>(reservaNueva, userHeaders),
                Reserva.class
        );

        logger.info("Reserva creada, status: {}", createResp.getStatusCode());
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long reservationId = createResp.getBody().reservationId();

        // Obtener detalle de la reserva
        ResponseEntity<Reserva> getResp = restTemplate.exchange(
                baseUrl + "/reservations/" + reservationId,
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                Reserva.class
        );

        logger.info("Reserva obtenida, status: {}", getResp.getStatusCode());
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().inicio()).isEqualTo(reservaNueva.inicio());

        // Cancelar reserva
        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                baseUrl + "/reservations/" + reservationId,
                HttpMethod.DELETE,
                new HttpEntity<>(userHeaders),
                Void.class
        );

        logger.info("Reserva cancelada, status: {}", deleteResp.getStatusCode());
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}

