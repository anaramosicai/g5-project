package edu.comillas.icai.gitt.pat.spring.grupo5.controlador;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Reserva;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.DisponibilidadResponse;
import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.ReservaService;
import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controlador de Pistas, Reservas, Disponibilidad y Health.
 * /pistaPadel/health incorporado a petición (provenía del controlador de Martina).
 */
@RestController
@RequestMapping("/pistaPadel")
public class PistaReservaController {

    // =========================
    // INYECCIONES DE SERVICIOS
    // =========================
    @Autowired
    private ReservaService reservaService;

    @Autowired
    private UsuarioService usuarioService;

    // =========================
    // PISTAS (en memoria)
    // =========================
    private final Map<Long, Pista> pistas = new ConcurrentHashMap<>();
    private long idPistaContador = 0;
    private static final int N = 1000;

    @PostMapping("/courts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Pista crea(@RequestBody Pista pista) {
        boolean nombreDuplicado = pistas.values().stream()
                .anyMatch(p -> p.nombre().equalsIgnoreCase(pista.nombre()));
        if (nombreDuplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Otra pista con igual nombre");
        }
        if (idPistaContador > N) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "idPista es mayor de N");
        }
        long idPista = idPistaContador++;
        Pista pistaNuevo = new Pista(
                idPista,
                pista.nombre(),
                pista.ubicacion(),
                pista.precioHora(),
                pista.activa(),
                pista.fechaAlta()
        );
        pistas.put(idPista, pistaNuevo);
        return pistaNuevo;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/courts")
    public List<Pista> listarPistas() {
        return new ArrayList<>(pistas.values());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/courts/{courtId}")
    public Pista obtenerDetalle(@PathVariable long courtId) {
        Pista pista = pistas.get(courtId);
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay una pista con este id");
        }
        return pista;
    }

    @PatchMapping("/courts/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Pista modificarPista(@PathVariable long courtId, @RequestBody Pista pistaMod) {
        Pista pista = pistas.get(courtId);
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Pista pistaActualizada = new Pista(
                courtId,
                pistaMod.nombre(),
                pistaMod.ubicacion(),
                pistaMod.precioHora(),
                pistaMod.activa(),
                pistaMod.fechaAlta());
        pistas.put(courtId, pistaActualizada);
        return pistaActualizada;
    }

    @DeleteMapping("/courts/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrar(@PathVariable long courtId) {
        Pista pista = pistas.get(courtId);
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay una pista con este id");
        }
        pistas.remove(courtId);
    }

    // =========================
    // RESERVAS (delegadas a ReservaService)
    // =========================
    private final Map<Long, Pista> pistas = new ConcurrentHashMap<>();
    private long idPistaContador = 0;

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public Reserva crearReserva(@RequestBody @Valid Reserva reservaNueva,
                                BindingResult bindingResult,
                                @RequestHeader(name = "Authorization", required = false) String authHeader) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Validar que la pista existe
        if (!pistas.containsKey(reservaNueva.courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        // Obtener usuario autenticado
        Usuario usuarioAutenticado = usuarioService.getAuthenticatedUser(authHeader);

        // Delegar a ReservaService (que valida autenticación y autorización)
        return reservaService.crearReserva(reservaNueva, usuarioAutenticado);
    }

    @PatchMapping("/reservations/{reservationId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Reserva reprogramar(@PathVariable long reservationId,
                               @RequestBody @Valid Reserva reservaNueva,
                               BindingResult bindingResult,
                               @RequestHeader(name = "Authorization", required = false) String authHeader) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Validar que la pista existe
        if (!pistas.containsKey(reservaNueva.courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        // Obtener usuario autenticado
        Usuario usuarioAutenticado = usuarioService.getAuthenticatedUser(authHeader);

        // Delegar a ReservaService (que valida autenticación y autorización)
        return reservaService.reprogramarReserva(reservationId, reservaNueva, usuarioAutenticado);
    }

    @GetMapping("/reservations/{reservationId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Reserva obtenerReserva(@PathVariable long reservationId,
                                  @RequestHeader(name = "Authorization", required = false) String authHeader) {

        // Obtener usuario autenticado
        Usuario usuarioAutenticado = usuarioService.getAuthenticatedUser(authHeader);

        // Delegar a ReservaService (que valida autenticación y autorización)
        return reservaService.obtenerReserva(reservationId, usuarioAutenticado);
    }

    @GetMapping("/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Reserva> obtenerTodasReservas(@RequestHeader(name = "Authorization", required = false) String authHeader) {

        // Obtener usuario autenticado
        Usuario usuarioAutenticado = usuarioService.getAuthenticatedUser(authHeader);

        // Delegar a ReservaService (que valida que es ADMIN)
        return reservaService.obtenerTodasReservas(usuarioAutenticado);
    }

    @DeleteMapping("/reservations/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public void cancelar(@PathVariable long reservationId,
                         @RequestHeader(name = "Authorization", required = false) String authHeader) {

        // Obtener usuario autenticado
        Usuario usuarioAutenticado = usuarioService.getAuthenticatedUser(authHeader);

        // Delegar a ReservaService (que valida autenticación y autorización)
        reservaService.cancelarReserva(reservationId, usuarioAutenticado);
    }

    // =========================
    // DISPONIBILIDAD
    // =========================
    @GetMapping("/availability")
    public DisponibilidadResponse consultarDisponibilidad(
            @RequestParam(name = "date") String dateStr,
            @RequestParam(name = "courtId", required = false) Long courtId) {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(dateStr);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de fecha inválido (use YYYY-MM-DD)");
        }

        boolean disponible = false;
        if (courtId != null) {
            if (pistas.containsKey(courtId)) {
                disponible = isPistaLibreEnFecha(courtId, fecha);
            }
        } else {
            for (Long id : pistas.keySet()) {
                if (isPistaLibreEnFecha(id, fecha)) {
                    disponible = true;
                    break;
                }
            }
        }
        String msg = disponible ? "Hay disponibilidad" : "Completo / No disponible";
        return new DisponibilidadResponse(fecha, courtId, disponible, msg);
    }

    @GetMapping("/courts/{courtId}/availability")
    public DisponibilidadResponse disponibilidadPistaConcreta(
            @PathVariable Long courtId,
            @RequestParam(name = "date") String dateStr) {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(dateStr);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de fecha inválido");
        }
        if (!pistas.containsKey(courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista " + courtId + " no existe");
        }
        boolean disponible = isPistaLibreEnFecha(courtId, fecha);
        return new DisponibilidadResponse(fecha, courtId, disponible, disponible ? "Libre" : "Ocupada");
    }

    // =========================
    // HEALTH
    // =========================
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    // =========================
    // Helpers
    // =========================
    private boolean isPistaLibreEnFecha(Long courtId, LocalDate fecha) {
        return reservas.values().stream()
                .filter(r -> r.courtId() == courtId)
                .noneMatch(r -> r.inicio().toLocalDate().equals(fecha));
    }
}
