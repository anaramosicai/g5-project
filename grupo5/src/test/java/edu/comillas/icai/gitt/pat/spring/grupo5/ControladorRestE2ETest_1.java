package edu.comillas.icai.gitt.pat.spring.grupo5;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.boot.test.web.client.TestRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Assertions;

// Al arrancar el test, este no puede levantar todas las dependencias de tareas programas.
// Por ello se debe poner lo siguiente:
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, // Por si ejecuto los test y la app, defino puerto random para evitar que el puerto concreto solo pueda escuchar uno
        properties = { // Con esto, al arrancar, no tiene en cuenta las tareas programadas (scheduling). Solo queremos probar los endpoint
                "spring.task.scheduling.enabled=false"
                //, "spring.profiles.active=test"
}

) // El profile tiene una seguridad por defecto
@ActiveProfiles("test") // Mi prueba tendrá dicha configuración de seguridad y no la normal
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

public class ControladorRestE2ETest {
    @Autowired
    private  TestRestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ControladorREST controladorREST;


    ///  ========== PARTE USERS ========== ///

    // Antes de cada test, limpio el estado automáticamente:
    @BeforeEach
    void setup() {
        controladorREST.reset();
    }

    @Test
    public void actualizarUsuario_EmailDuplicado_Test(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        logger.info("=== INICIO TEST: actualizarUsuario_EmailDuplicado_Test ===");

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
/*
        logger.info("Primer usuario registrado, status: {}", response1.getStatusCode());
        logger.info("Primer usuario body: {}", response1.getBody());

        Long idUsuario1 = response1.getBody().idUsuario();
        logger.info("ID del primer usuario: {}", idUsuario1);*/

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
/*
        logger.info("Segundo usuario registrado, status: {}", response2.getStatusCode());
        logger.info("Segundo usuario body: {}", response2.getBody());
*/
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

        logger.info("PATCH email duplicado, status: {}", response.getStatusCode());
        logger.info("PATCH response body: {}", response.getBody());

        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());



    }
}
