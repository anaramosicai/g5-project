package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Reserva;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoReserva;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private RepoReserva repoReserva;


    public Reserva crearReserva(Reserva reservaNueva) {
        // Validar que inicio no sea posterior a fin
        if (reservaNueva.inicio.isAfter(reservaNueva.fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inicio posterior a fin");
        }

        // Detectar solapes en la pista
        boolean solapa = tieneConflicto(reservaNueva.courtId, reservaNueva.inicio, reservaNueva.fin, null);

        if (solapa) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }
        return repoReserva.save(reservaNueva);
    }


    public Reserva obtenerReserva(Long reservationId) {
        Optional<Reserva> reserva = repoReserva.findById(reservationId);
        if (reserva.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no hay ninguna reserva con este id");
        }
        return reserva.get();
    }


    public List<Reserva> obtenerTodasReservas() {
        return repoReserva.findAll();
    }


    public List<Reserva> obtenerMisReservas(String userId, LocalDate fromDate, LocalDate toDate) {
        List<Reserva> reservasUsuario = repoReserva.findByUserId(userId);

        // Filtrar por rango de fechas si se proporciona
        return reservasUsuario.stream()
                .filter(r -> fromDate == null || !r.inicio.toLocalDate().isBefore(fromDate))
                .filter(r -> toDate == null || !r.inicio.toLocalDate().isAfter(toDate))
                .toList();
    }

    public Reserva reprogramarReserva(Long reservationId, Reserva reservaNueva) {
        Reserva actual = obtenerReserva(reservationId); // Esto lanza NOT_FOUND si no existe

        boolean solapa = tieneConflicto(reservaNueva.courtId, reservaNueva.inicio, reservaNueva.fin, reservationId);

        if (solapa) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        Reserva actualizada = new Reserva(
                reservationId,
                actual.courtId,
                actual.userId,
                reservaNueva.inicio,
                reservaNueva.fin
        );

        return repoReserva.save(actualizada);
    }


    public void cancelarReserva(Long reservationId) {
        Reserva reserva = obtenerReserva(reservationId); // Esto lanza NOT_FOUND si no existe
        repoReserva.deleteById(reservationId);
    }

    private boolean tieneConflicto(Long courtId, LocalDateTime inicio, LocalDateTime fin, Long idExcluido) {
        List<Reserva> conflictivas = repoReserva.findByCourtIdAndInicioBeforeAndFinAfter(
                courtId, fin, inicio
        );

        // Si exluimos una reserva específica (durante actualización), la filtramos
        if (idExcluido != null) {
            conflictivas = conflictivas.stream()
                    .filter(r -> !r.reservationId.equals(idExcluido))
                    .toList();
        }

        return !conflictivas.isEmpty();
    }
}
