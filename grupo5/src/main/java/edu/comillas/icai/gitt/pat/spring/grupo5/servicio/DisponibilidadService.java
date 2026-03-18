package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Disponibilidad;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Reserva;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoDisponibilidad;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoReserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DisponibilidadService {

    private static final LocalTime APERTURA = LocalTime.of(8, 0);
    private static final LocalTime CIERRE = LocalTime.of(22, 0);
    private static final int DURACION_BLOQUE_MIN = 60;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired RepoPista repoPista;
    @Autowired RepoReserva repoReserva;
    @Autowired RepoDisponibilidad repoDisponibilidad;

    // ===============================
    // GET /availability - Disponibilidad General
    // ===============================
    public List<Disponibilidad> disponibilidadGeneral(LocalDate fecha, Long courtId) {

        if (fecha == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha requerida");

        logger.info("Calculando disponibilidad general para fecha: {}, courtId: {}", fecha, courtId);

        List<Pista> pistas = new ArrayList<>();

        if (courtId != null) {
            Pista pista = repoPista.findById(courtId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe"));
            pistas.add(pista);
        } else {
            repoPista.findAll().forEach(pistas::add);
        }

        List<Disponibilidad> disponibilidades = pistas.stream()
                .map(p -> calcularYGuardarDisponibilidad(p, fecha))
                .toList();

        logger.info("Se calcularon {} disponibilidades", disponibilidades.size());
        return disponibilidades;
    }

    // ===============================
    // GET /courts/{id}/availability - Disponibilidad por Pista
    // ===============================
    public Disponibilidad disponibilidadPista(Long courtId, LocalDate fecha) {

        if (fecha == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha requerida");

        logger.info("Calculando disponibilidad de pista {} para fecha {}", courtId, fecha);

        Pista pista = repoPista.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe"));

        return calcularYGuardarDisponibilidad(pista, fecha);
    }

    // ===============================
    // GET /reservations - Reservas por Usuario
    // ===============================
    @Transactional(readOnly = true)
    public List<Reserva> reservasPorUsuario(Long userId, LocalDate from, LocalDate to) {

        if (userId == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no identificado");

        logger.info("Obteniendo reservas del usuario {} desde {} hasta {}", userId, from, to);

        Usuario u = new Usuario();
        u.setId(userId);

        List<Reserva> lista = repoReserva.findByUsuario(u);

        return lista.stream()
                .filter(r -> from == null || !r.getInicio().toLocalDate().isBefore(from))
                .filter(r -> to == null || !r.getInicio().toLocalDate().isAfter(to))
                .toList();
    }

    // ===============================
    // LÓGICA CENTRAL DEL CÁLCULO Y PERSISTENCIA
    // ===============================
    /**
     * Calcula la disponibilidad de una pista para una fecha específica
     * y la persiste en la base de datos.
     * 
     * @param pista La pista a calcular
     * @param fecha La fecha para la cual calcular
     * @return La disponibilidad calculada y guardada
     */
    private Disponibilidad calcularYGuardarDisponibilidad(Pista pista, LocalDate fecha) {

        logger.debug("Calculando disponibilidad para pista {} en fecha {}", pista.id, fecha);

        // Buscar si ya existe disponibilidad para esta pista y fecha
        List<Disponibilidad> existentes = repoDisponibilidad.findByPistaIdAndFecha(pista.id, fecha);
        
        if (!existentes.isEmpty()) {
            logger.debug("Disponibilidad ya existe en BD, retornando la existente");
            return existentes.get(0);
        }

        // Obtener todas las reservas de la pista
        List<Reserva> reservas = repoReserva.findByPista(pista);

        List<Disponibilidad.FranjaDisponible> libres = new ArrayList<>();

        // Recorrer cada bloque de tiempo del día
        for (LocalTime t = APERTURA; t.isBefore(CIERRE); t = t.plusMinutes(DURACION_BLOQUE_MIN)) {

            LocalTime fin = t.plusMinutes(DURACION_BLOQUE_MIN);
            LocalDateTime inicioDT = fecha.atTime(t);
            LocalDateTime finDT = fecha.atTime(fin);

            // Verificar si existe conflicto con alguna reserva
            boolean ocupado = reservas.stream().anyMatch(r ->
                    r.getInicio().isBefore(finDT) &&
                            r.getFin().isAfter(inicioDT)
            );

            // Si no hay conflicto, agregar la franja como disponible
            if (!ocupado) {
                Disponibilidad.FranjaDisponible franja = new Disponibilidad.FranjaDisponible(t, fin);
                libres.add(franja);
                logger.debug("Franja disponible: {} - {}", t, fin);
            } else {
                logger.debug("Franja ocupada: {} - {}", t, fin);
            }
        }

        // Crear la disponibilidad
        Disponibilidad disponibilidad = new Disponibilidad(pista, fecha, APERTURA, CIERRE, libres);

        // Asignar la referencia de disponibilidad a cada franja
        libres.forEach(f -> f.setDisponibilidad(disponibilidad));

        // Persistir en la base de datos
        Disponibilidad guardada = repoDisponibilidad.save(disponibilidad);

        logger.info("Disponibilidad guardada con ID {} para pista {} en fecha {}. Total franjasLibres: {}",
                guardada.getId(), pista.id, fecha, libres.size());

        return guardada;
    }
}
