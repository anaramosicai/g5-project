package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Disponibilidad;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Reserva;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoReserva;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadService {

    private static final LocalTime APERTURA = LocalTime.of(8, 0);
    private static final LocalTime CIERRE = LocalTime.of(22, 0);
    private static final int DURACION_BLOQUE_MIN = 60;

    @Autowired RepoPista repoPista;
    @Autowired RepoReserva repoReserva;

    // ===============================
    // GET /availability
    // ===============================
    public List<Disponibilidad> disponibilidadGeneral(LocalDate fecha, Long courtId) {

        if (fecha == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha requerida");

        List<Pista> pistas = new ArrayList<>();

        if (courtId != null) {
            Pista pista = repoPista.findById(courtId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe"));
            pistas.add(pista);
        } else {
            repoPista.findAll().forEach(pistas::add);
        }

        return pistas.stream()
                .map(p -> calcularDisponibilidad(p, fecha))
                .toList();
    }

    // ===============================
    // GET /courts/{id}/availability
    // ===============================
    public Disponibilidad disponibilidadPista(Long courtId, LocalDate fecha) {

        if (fecha == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha requerida");

        Pista pista = repoPista.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe"));

        return calcularDisponibilidad(pista, fecha);
    }

    // ===============================
    // GET /reservations
    // ===============================
    public List<Reserva> reservasPorUsuario(Long userId, LocalDate from, LocalDate to) {

        if (userId == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no identificado");

        Usuario u = new Usuario();
        u.setId(userId);

        List<Reserva> lista = repoReserva.findByUsuario(u);

        return lista.stream()
                .filter(r -> from == null || !r.getInicio().toLocalDate().isBefore(from))
                .filter(r -> to == null || !r.getInicio().toLocalDate().isAfter(to))
                .toList();
    }

    // ===============================
    // LÓGICA CENTRAL DEL CÁLCULO
    // ===============================
    private Disponibilidad calcularDisponibilidad(Pista pista, LocalDate fecha) {

        List<Reserva> reservas = repoReserva.findByPista(pista);

        List<Disponibilidad.FranjaDisponible> libres = new ArrayList<>();

        for (LocalTime t = APERTURA; t.isBefore(CIERRE); t = t.plusMinutes(DURACION_BLOQUE_MIN)) {

            LocalTime fin = t.plusMinutes(DURACION_BLOQUE_MIN);
            LocalDateTime inicioDT = fecha.atTime(t);
            LocalDateTime finDT = fecha.atTime(fin);

            boolean ocupado = reservas.stream().anyMatch(r ->
                    r.getInicio().isBefore(finDT) &&
                            r.getFin().isAfter(inicioDT)
            );

            if (!ocupado) {
                libres.add(new Disponibilidad.FranjaDisponible(t, fin));
            }
        }

        return new Disponibilidad(pista, fecha, APERTURA, CIERRE, libres);
    }
}
