package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.web.client.TestRestTemplate; //<-- no va y la dependency está incluída ¯_ (ツ)_/¯
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Assertions;

/**
 * Test E2E del endpoint real POST /pistaPadel/auth/register
 *  - RANDOM_PORT
 *  - TestRestTemplate
 *  - ActiveProfiles("test")
 *  - DirtiesContext
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.task.scheduling.enabled=false", "spring.profiles.active=test"})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ControladorRestE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String REGISTER = "/pistaPadel/auth/register";

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
        Assertions.assertEquals(HttpStatus.CONFLICT, r2.getStatusCode());
    }
}
