package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Disponibilidad;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoDisponibilidad;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoReserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepoDisponibilidad repoDisponibilidad;
    @Autowired
    private RepoReserva repoReserva;

    // ===============================
    // MÉTODOS DE LÓGICA DE NEGOCIO
    // ===============================

    /**
     * Obtiene disponibilidad general para una fecha y pista (opcional)
     */
    public List<Disponibilidad> disponibilidadGeneral(LocalDate fecha, Long courtId) {
        logger.info("DisponibilidadService: Consultando disponibilidad general para fecha {} y pista {}", 
                    fecha, courtId);

        if (fecha == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha requerida");
        }

        List<Disponibilidad> disponibilidades = new ArrayList<>();

        if (courtId != null) {
            var disp = repoDisponibilidad.findByPista_IdAndFecha(courtId, fecha);
            if (disp.isPresent()) {
                disponibilidades.add(disp.get());
            }
        } else {
            repoDisponibilidad.findByFecha(fecha).forEach(disponibilidades::add);
        }

        return disponibilidades;
    }

    /**
     * Obtiene disponibilidad de una pista específica en una fecha
     */
    public Disponibilidad disponibilidadPista(LocalDate fecha, Long courtId) {
        if (fecha == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha requerida");
        }
        if (courtId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pista requerida");
        }

        return repoDisponibilidad.findByPista_IdAndFecha(courtId, fecha)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay disponibilidad para esta pista en esta fecha"));
    }


    /**
     * Obtiene reservas de un usuario en un rango de fechas
     */
    public List<Disponibilidad> reservasPorUsuario(Long userId, LocalDate from, LocalDate to) {
        logger.info("DisponibilidadService: Obteniendo disponibilidades del usuario {} desde {} hasta {}", 
                    userId, from, to);

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no identificado");
        }

        // Obtener todas las disponibilidades y filtrar las del usuario
        List<Disponibilidad> todasDisponibilidades = new ArrayList<>();
        repoDisponibilidad.findAll().forEach(todasDisponibilidades::add);

        return todasDisponibilidades.stream()
                .filter(d -> from == null || !d.getFecha().isBefore(from))
                .filter(d -> to == null || !d.getFecha().isAfter(to))
                .toList();
    }

    public Disponibilidad crearDisponibilidad(Disponibilidad disponibilidad) {
        return repoDisponibilidad.save(disponibilidad);
    }
}
