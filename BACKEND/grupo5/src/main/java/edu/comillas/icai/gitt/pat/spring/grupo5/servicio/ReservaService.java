package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Reserva;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.EstadoReserva;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoReserva;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private RepoReserva repoReserva;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PistaService pistaService;

    // ========================================
    // PARA TAREAS PROGRAMADAS
    // ========================================


    public void enviarRecordatorioDia(){
        LocalDate hoy_programadas = LocalDate.now();
        LocalDateTime inicioDia_programado = hoy_programadas.atStartOfDay();
        LocalDateTime finDia_programado = hoy_programadas.atTime(23,59,59);

        List<Reserva> reservasHoy = repoReserva.findByInicioBetween(inicioDia_programado, finDia_programado);

        for (Reserva reserva : reservasHoy){
            Usuario usuario = reserva.getUsuario();
            Pista pista = reserva.getPista();

            // Enviamos el correo:
            System.out.println("Enviando recordatorio a: "+ usuario.getEmail()
            + " para la pista: " + pista.nombre
            + " a las: " + reserva.getInicio());

            String email = usuario.getEmail();
            String asunto = "Recordatorio de tu reserva";
            String mensaje = "Hola " + usuario.getNombre() + ",\n\n" +
                    "Te recordamos que hoy tienes una reserva:\n" +
                    "- Pista: " + pista.nombre + "\n" +
                    "- Hora: " + reserva.getInicio() + "\n\n" +
                    "¡Gracias!";
            emailService.enviarEmail(email, asunto, mensaje);
        }
    }

    // ========================================
    // MÉTODOS PÚBLICOS CON AUTORIZACIÓN
    // ========================================

    /**
     * Crea una nueva reserva asociada al usuario autenticado
     */
    public Reserva crearReserva(Reserva reservaNueva, Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        if (reservaNueva.inicio.isAfter(reservaNueva.fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inicio posterior a fin");
        }

        // Validar que no hay conflictos de horarios
        Long pistaId = reservaNueva.pista != null ? reservaNueva.pista.id : null;
        if (pistaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pista es requerida");
        }

        // Validar que la pista existe
        pistaService.lee(pistaId);

        boolean solapa = tieneConflicto(pistaId, reservaNueva.inicio, reservaNueva.fin, null);
        if (solapa) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        // Asignar usuario autenticado y establecer valores por defecto
        reservaNueva.usuario = usuarioAutenticado;
        if (reservaNueva.fechaCreacion == null) {
            reservaNueva.fechaCreacion = LocalDateTime.now();
        }
        if (reservaNueva.fechaReservada == null) {
            reservaNueva.fechaReservada = LocalDateTime.now();
        }
        if (reservaNueva.duracionMinutos == null) {
            reservaNueva.duracionMinutos = (int) java.time.temporal.ChronoUnit.MINUTES.between(reservaNueva.inicio, reservaNueva.fin);
        }
        if (reservaNueva.estado == null) {
            reservaNueva.estado = EstadoReserva.ACTIVA;
        }

        return repoReserva.save(reservaNueva);
    }

    /**
     * Obtiene una reserva si el usuario es propietario o es ADMIN
     */
    public Reserva obtenerReserva(Long reservationId, Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        Optional<Reserva> reserva = repoReserva.findById(reservationId);
        if (reserva.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no hay ninguna reserva con este id");
        }

        Reserva reservaObtenida = reserva.get();
        verificarPermisoLectura(reservaObtenida, usuarioAutenticado);

        return reservaObtenida;
    }

    /**
     * Obtiene todas las reservas (solo ADMIN)
     */
    public List<Reserva> obtenerTodasReservas(Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        if (!usuarioService.isAdmin(usuarioAutenticado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN puede ver todas las reservas");
        }

        Iterable<Reserva> iterable = repoReserva.findAll();
        List<Reserva> reservas = new ArrayList<>();
        iterable.forEach(reservas::add);
        return reservas;
    }

    /**
     * Obtiene las reservas del usuario autenticado en un rango de fechas
     */
    public List<Reserva> obtenerMisReservas(Usuario usuarioAutenticado, LocalDate fromDate, LocalDate toDate) {
        if (usuarioAutenticado == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        List<Reserva> reservasUsuario = repoReserva.findByUsuario_Id(usuarioAutenticado.getId());

        return reservasUsuario.stream()
                .filter(r -> fromDate == null || !r.inicio.toLocalDate().isBefore(fromDate))
                .filter(r -> toDate == null || !r.inicio.toLocalDate().isAfter(toDate))
                .toList();
    }

    /**
     * Reprograma (modifica horario) de una reserva si el usuario es propietario o es ADMIN
     */
    public Reserva reprogramarReserva(Long reservationId, Reserva reservaNueva, Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        Optional<Reserva> optionalReserva = repoReserva.findById(reservationId);
        if (optionalReserva.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada");
        }

        Reserva actual = optionalReserva.get();
        verificarPermisoModificacion(actual, usuarioAutenticado);

        if (reservaNueva.inicio.isAfter(reservaNueva.fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inicio posterior a fin");
        }

        // Validar que la pista existe
        Long pistaId = reservaNueva.pista != null ? reservaNueva.pista.id : actual.pista.id;
        pistaService.lee(pistaId);

        // Validar que no hay conflictos con el nuevo horario
        pistaId = actual.pista.id;
        boolean solapa = tieneConflicto(pistaId, reservaNueva.inicio, reservaNueva.fin, reservationId);
        if (solapa) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        // Actualizar solo los campos modificables
        actual.inicio = reservaNueva.inicio;
        actual.fin = reservaNueva.fin;
        actual.duracionMinutos = (int) java.time.temporal.ChronoUnit.MINUTES.between(reservaNueva.inicio, reservaNueva.fin);
        actual.fechaReservada = LocalDateTime.now();

        return repoReserva.save(actual);
    }

    /**
     * Cancela una reserva si el usuario es propietario o es ADMIN
     */
    public void cancelarReserva(Long reservationId, Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        Optional<Reserva> optionalReserva = repoReserva.findById(reservationId);
        if (optionalReserva.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada");
        }

        Reserva reserva = optionalReserva.get();
        verificarPermisoModificacion(reserva, usuarioAutenticado);

        repoReserva.deleteById(reservationId);
    }

    // ========================================
    // MÉTODOS PRIVADOS DE AUTORIZACIÓN
    // ========================================

    /**
     * Verifica si el usuario tiene permiso para leer una reserva (es propietario o ADMIN)
     */
    private void verificarPermisoLectura(Reserva reserva, Usuario usuarioAutenticado) {
        boolean esAdmin = usuarioService.isAdmin(usuarioAutenticado);
        boolean esPropia = reserva.usuario.getId().equals(usuarioAutenticado.getId());

        if (!esAdmin && !esPropia) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "No tienes permiso para ver esta reserva");
        }
    }

    /**
     * Verifica si el usuario tiene permiso para modificar una reserva (es propietario o ADMIN)
     */
    private void verificarPermisoModificacion(Reserva reserva, Usuario usuarioAutenticado) {
        boolean esAdmin = usuarioService.isAdmin(usuarioAutenticado);
        boolean esPropia = reserva.usuario.getId().equals(usuarioAutenticado.getId());

        if (!esAdmin && !esPropia) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "No tienes permiso para modificar esta reserva");
        }
    }

    // ========================================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ========================================

    /**
     * Verifica si hay conflictos de horario para una pista en un rango de tiempo
     */
    private boolean tieneConflicto(Long pistaId, LocalDateTime inicio, LocalDateTime fin, Long idExcluido) {
        List<Reserva> conflictivas = repoReserva.findByPista_IdAndInicioBeforeAndFinAfter(
                pistaId, fin, inicio
        );

        if (idExcluido != null) {
            conflictivas = conflictivas.stream()
                    .filter(r -> !r.reservationId.equals(idExcluido))
                    .toList();
        }

        return !conflictivas.isEmpty();
    }
}
