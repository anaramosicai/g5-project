package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import edu.comillas.icai.gitt.pat.spring.grupo5.Disponibilidad;
import edu.comillas.icai.gitt.pat.spring.grupo5.DisponibilidadRepository;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.DisponibilidadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DisponibilidadService {

    @Autowired
    DisponibilidadRepository disponibilidadRepository;

    /**
     * Consulta disponibilidad para una fecha y courtId (opcional)
     * Si courtId es null, busca si hay alguna pista disponible
     */
    public DisponibilidadResponse consultarDisponibilidad(LocalDate fecha, Long courtId) {
        boolean disponible = false;
        String mensaje;

        if (courtId != null) {
            // Buscamos disponibilidad de UNA pista concreta
            Optional<Disponibilidad> disp = disponibilidadRepository.findByFechaAndCourtId(fecha, courtId);
            if (disp.isPresent()) {
                disponible = disp.get().isDisponible();
                mensaje = disponible ? "Hay disponibilidad" : "Completo / No disponible";
                return disp.get().toResponse();
            } else {
                // Si no existe registro, crear uno nuevo
                Disponibilidad nuevaDisp = new Disponibilidad(fecha, courtId, true, "Libre");
                Disponibilidad guardada = disponibilidadRepository.save(nuevaDisp);
                return guardada.toResponse();
            }
        } else {
            // Buscamos si hay ALGUNA pista libre en esa fecha
            List<Disponibilidad> disponiblesEnFecha = disponibilidadRepository.findByFecha(fecha);
            disponible = disponiblesEnFecha.stream().anyMatch(Disponibilidad::isDisponible);
            mensaje = disponible ? "Hay disponibilidad" : "Completo / No disponible";
            return new DisponibilidadResponse(fecha, null, disponible, mensaje);
        }
    }

    /**
     * Obtiene la disponibilidad de una pista en una fecha específica
     */
    public DisponibilidadResponse obtenerDisponibilidadPistaConcreta(LocalDate fecha, Long courtId) {
        Optional<Disponibilidad> disp = disponibilidadRepository.findByFechaAndCourtId(fecha, courtId);
        
        if (disp.isPresent()) {
            return disp.get().toResponse();
        } else {
            // Si no existe, crear registro por defecto (disponible)
            Disponibilidad nuevaDisp = new Disponibilidad(fecha, courtId, true, "Libre");
            Disponibilidad guardada = disponibilidadRepository.save(nuevaDisp);
            return guardada.toResponse();
        }
    }

    /**
     * Actualiza la disponibilidad de una pista en una fecha
     */
    public DisponibilidadResponse actualizarDisponibilidad(LocalDate fecha, Long courtId, boolean disponible, String mensaje) {
        Optional<Disponibilidad> disp = disponibilidadRepository.findByFechaAndCourtId(fecha, courtId);
        
        Disponibilidad disponibilidadActualizada;
        if (disp.isPresent()) {
            Disponibilidad d = disp.get();
            d.setDisponible(disponible);
            d.setMensaje(mensaje);
            disponibilidadActualizada = disponibilidadRepository.save(d);
        } else {
            Disponibilidad nuevaDisp = new Disponibilidad(fecha, courtId, disponible, mensaje);
            disponibilidadActualizada = disponibilidadRepository.save(nuevaDisp);
        }
        
        return disponibilidadActualizada.toResponse();
    }

    /**
     * Obtiene todas las disponibilidades de una pista en un rango de fechas
     */
    public List<Disponibilidad> obtenerDisponibilidadesFecha(Long courtId, LocalDate fechaInicio, LocalDate fechaFin) {
        return disponibilidadRepository.findByCourtIdAndFechaBetween(courtId, fechaInicio, fechaFin);
    }
}
