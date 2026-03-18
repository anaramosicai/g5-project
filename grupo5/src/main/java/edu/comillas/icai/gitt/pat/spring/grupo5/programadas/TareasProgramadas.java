package edu.comillas.icai.gitt.pat.spring.grupo5.programadas;

import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.PistaService;
import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.ReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;



@Component
public class TareasProgramadas {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public PistaService servicioPista;
    @Autowired
    public ReservaService servicioReserva;

    @Scheduled(cron = "0 0 2 * * *") // En el segundo y minuto 0, a las 2am de cada día, mes.
    public void remindPista() {
        logger.info("Me ejecuto cada día a las 2 AM");

        /* Mandar correo a usuarios que tienen pista reservada para ese día */

        //servicioReserva.enviarRecordatorioDia(); // Esta clase debemos implementarla en Servicio
    }

    @Scheduled(cron = "0 0 0 1 * *") // Se ejecutará justo al empezar el día 1
    public void showDisponibilidad() {
        logger.info("Me ejecuto el día 1 de cada mes");

        /* Mandar correo a todos los usuarios con las pistas y los horarios disponibles */

        //servicioPista.enviarDisponibilidadMensual(); // Esta clase debemos implementarla en Servicio
    }

}



