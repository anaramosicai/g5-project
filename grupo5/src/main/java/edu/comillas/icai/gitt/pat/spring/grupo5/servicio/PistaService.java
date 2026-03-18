package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Disponibilidad;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoPista;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PistaService {
    @Autowired
    private RepoPista repoPista;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Transactional
    public Pista crea(Pista pistaNuevo){
        logger.info("ServicioPista: Trying to create a pista: " + pistaNuevo.id);

        //409 conflict
        if(repoPista.existsByNombreIgnoreCase(pistaNuevo.nombre)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another Pista with the same name");
        }
        //400 name is req
        if (pistaNuevo.nombre == null || pistaNuevo.nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }

        String fecha = pistaNuevo.fechaAlta;
        if (fecha == null) {
            fecha = LocalDate.now().toString();   // o la fecha que sea por defecto
        }

        Pista pista = new Pista(

                pistaNuevo.nombre,
                pistaNuevo.ubicacion,
                pistaNuevo.precioHora,
                pistaNuevo.activa,
                fecha
        );

        return repoPista.save(pista);
    }

    public List<Pista> leeTodas(){
        List<Pista> pistas = new ArrayList<>();
        repoPista.findAll().forEach(pistas::add);
        return pistas;
    }

    public Pista lee(long id){
        logger.info("ServicioPista: Read pista with id " + id);

        //404 not found
        return repoPista.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "couldn't find pista with this id"));
    }

    @Transactional
    public Pista cambiar(Pista pistaNuevo, long id){
        //404 not found
        Pista pista = repoPista.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        //409 name duplicate
        if(pista.nombre == pistaNuevo.nombre){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another Pista with the same name exists");
        }
        Pista pistaActualizada = new Pista(

                pistaNuevo.nombre,
                pistaNuevo.ubicacion,
                pistaNuevo.precioHora,
                pistaNuevo.activa,
                pistaNuevo.fechaAlta
        );
        return repoPista.save(pistaActualizada);
    }

    /*
    @Transactional
    public void borrar(long id){
        Pista pista = repoPista.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Couldn't find pista with this id"));

        repoPista.delete(pista);
    }

    public boolean existePorId(Long id) {
        return repoPista.existsById(id);
    }
    */

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Autowired
    private EmailService emailService;

    public void enviarDisponibilidadMensual() {
        logger.info("PistaService: Generando disponibilidad mensual…");

        List<Pista> pistas = leeTodas();
        List<Usuario> usuarios = usuarioService.obtenerTodosUsuarios();

        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("¡Hola!\n\nAquí tienes la disponibilidad mensual de pistas de pádel.\n\n");

        for (Pista pista : pistas) {
            cuerpo.append("─────────────────────────────\n");
            cuerpo.append("PISTA: ").append(pista.nombre).append("\n");
            cuerpo.append("Ubicación: ").append(pista.ubicacion).append("\n");

            // Disponibilidades por día
            for (LocalDate fecha = inicioMes; !fecha.isAfter(finMes); fecha = fecha.plusDays(1)) {

                List<Disponibilidad> disp = disponibilidadService.disponibilidadGeneral(fecha, pista.id);

                if (!disp.isEmpty()) {
                    cuerpo.append("  • ").append(fecha).append(" → ");
                    for (Disponibilidad d : disp) {
                        cuerpo.append("  • ").append(d.getFecha()).append("\n");

                        if (d.getFranjasLibres() == null || d.getFranjasLibres().isEmpty()) {
                            cuerpo.append("      (Sin franjas libres)\n");
                            continue;
                        }

                        for (Disponibilidad.FranjaDisponible franja : d.getFranjasLibres()) {
                            cuerpo.append("      - [")
                                    .append(franja.getInicio())
                                    .append(" - ")
                                    .append(franja.getFin())
                                    .append("]\n");
                        }
                    }

                    cuerpo.append("\n");
                }
            }
            cuerpo.append("\n");
        }

        cuerpo.append("¡Reserva cuanto antes!\n");
        cuerpo.append("Equipo PistaPadel\n");

        String asunto = "Disponibilidad de Pistas - Mes " + hoy.getMonth();

        for (Usuario user : usuarios) {
            emailService.enviarEmail(
                    user.getEmail(),
                    asunto,
                    cuerpo.toString()
            );
        }

        logger.info("Disponibilidad mensual enviada a {} usuarios", usuarios.size());
    }

    public ResponseEntity<Void> borrar(long id){
        //404 not found
        Pista pista = repoPista.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Couldn't find pista with this id"));
        repoPista.delete(pista);
        return ResponseEntity.noContent().build();
    }

}
