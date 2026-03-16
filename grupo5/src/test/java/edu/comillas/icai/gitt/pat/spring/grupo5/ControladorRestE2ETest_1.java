package edu.comillas.icai.gitt.pat.spring.grupo5;


import edu.comillas.icai.gitt.pat.spring.grupo5.controlador.ControladorREST;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoPista;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

public class ControladorRestE2ETest_1 {
    @Autowired
    private  TestRestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ControladorREST controladorREST;


    ///  ========== PARTE USERS ========== ///

    // Antes de cada test, limpio el estado automáticamente:

    @Autowired
    private RepoPista repoPista;

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
    void getPistaByIdE2E() {

        Pista pista = new Pista(1L,"Central","Madrid",20,true,null);
        repoPista.save(pista);

        ResponseEntity<Pista> response =
                restTemplate.getForEntity("/pistas/1", Pista.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().nombre).isEqualTo("Central");
    }

    @Test
    void getAllPistasE2E(){

        repoPista.save(new Pista(1L,"Central","Madrid",20,true,null));
        repoPista.save(new Pista(2L,"Norte","Madrid",15,true,null));

        ResponseEntity<Pista[]> response =
                restTemplate.getForEntity("/pistas", Pista[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(2);
    }

    @Test
    void deletePistaE2E(){

        repoPista.save(new Pista(1L,"Central","Madrid",20,true,null));

        restTemplate.delete("/pistas/1");

        boolean exists = repoPista.existsById(1L);

        assertThat(exists).isFalse();
    }

}
