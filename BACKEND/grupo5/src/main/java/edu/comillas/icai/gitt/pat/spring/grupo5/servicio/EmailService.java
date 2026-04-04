package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void enviarEmail(String to, String subject, String text) {

        // Caso: tests E2E → mailSender no existe
        if (mailSender == null) {
            System.out.println("INFO: EmailService → mailSender es null (modo test). "
                    + "Simulando envío de email a: " + to);
            return;
        }

        // Caso: producción → enviar email real
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(to);
        mensaje.setSubject(subject);
        mensaje.setText(text);

        mailSender.send(mensaje);
    }
}


/*
* Añadir en pom la siguiente dependencia:
 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
* */
