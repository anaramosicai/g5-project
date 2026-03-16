package edu.comillas.icai.gitt.pat.spring.grupo5.controlador;

import edu.comillas.icai.gitt.pat.spring.grupo5.model.*;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Reserva;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.PistaService;
import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
public class ControladorREST {

    @Autowired
    PistaService pistaService;

    // ============================
    // SECCIÓN: PISTAS
    // ============================

    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Pista crea(@RequestBody Pista pista) {
        return pistaService.crea(pista);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/pistaPadel/courts")
    public List<Pista> pistas() {
        return pistaService.leeTodas();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/pistaPadel/courts/{courtId}")
    public Pista obtenerDetalle(@PathVariable long courtId) {
        return pistaService.lee(courtId);
    }

    @PatchMapping("/pistaPadel/courts/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Pista modificarPista(@PathVariable long courtId, @RequestBody Pista pistaMod) {
        return pistaService.cambiar(pistaMod, courtId);
    }

    @DeleteMapping("/pistaPadel/courts/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void borrar(@PathVariable long courtId) {
        pistaService.borrar(courtId);
    }

    // ============================
    // SECCIÓN: AUTENTICACIÓN
    // ============================

    @Autowired
    UsuarioService usuarioService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();
    private final Map<Long, Usuario> usuariosporId = new ConcurrentHashMap<>();
    private final AtomicLong idUsuarioSeq = new AtomicLong(1);

    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();
    private final Map<Long, String> userIdToToken = new ConcurrentHashMap<>();

    /**
     * POST /pistaPadel/auth/register
     * 201 creado, 400 datos inválidos, 409 email ya existe
     */
    @PostMapping("/auth/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (request == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // 409 si email duplicado
        if (usuarioService.emailExists(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        UsuarioResponse body = usuarioService.register(request);
        if (body == null) {
            // datos inválidos -> 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(body); // 201
    }

    /**
     * POST /pistaPadel/auth/login
     * 200 ok, 400 request inválida, 401 credenciales incorrectas
     */
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        if (request == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        LoginResponse resp = usuarioService.login(request);
        if (resp == null) {
            // Diferencia 400 vs 401 de forma simple:
            if (request.email() == null || request.email().isBlank()
                    || request.password() == null || request.password().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(resp); // 200
    }

    /**
     * POST /pistaPadel/auth/logout
     * 204 ok, 401 no autenticado
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        boolean ok = usuarioService.logout(authHeader);
        if (!ok) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.noContent().build(); // 204
    }

    /**
     * GET /pistaPadel/auth/me
     * 200 ok, 401 no autenticado
     */
    @GetMapping("/auth/me")
    public ResponseEntity<UsuarioResponse> me(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        UsuarioResponse me = usuarioService.me(authHeader);
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(me); // 200
    }

    // ============================
    // SECCIÓN: USUARIOS
    // ============================

    /**
     * GET /pistaPadel/users/{userId}
     * (ADMIN o dueño) 200, 401, 403, 404
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UsuarioResponse> getUserById(
            @PathVariable Long userId,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        // 401 si no autenticado
        Usuario auth = usuarioService.getAuthenticatedUser(authHeader);
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 403 si no es admin ni dueño
        boolean esAdmin = usuarioService.isAdmin(auth);
        boolean esDueno = usuarioService.isOwner(auth, userId);
        if (!esAdmin && !esDueno) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        // 404 si no existe el usuario solicitado
        Usuario objetivo = usuarioService.getUsuarioById(userId);
        if (objetivo == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        // 200 OK con DTO
        UsuarioResponse body = new UsuarioResponse(
                objetivo.getId(),
                objetivo.getNombre(),
                objetivo.getApellidos(),
                objetivo.getEmail(),
                objetivo.getTelefono(),
                objetivo.getRol(),
                objetivo.getFechaRegistro(),
                objetivo.isActivo()
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping("/pistaPadel/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Collection<Usuario> listarUsuarios(){
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/pistaPadel/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario obtenerUsuario(@PathVariable Long userId) {
        return usuarioService.obtenerUsuario(userId);
    }

    @PatchMapping("/pistaPadel/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario actualizarUsuario(@PathVariable Long userId, @RequestBody Map<String, Object> cambios) {
        return usuarioService.actualizarUsuario(userId, cambios);
    }

    // ============================
    // SECCIÓN: RESERVAS
    // ============================

    @PostMapping("/pistaPadel/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public Reserva crearReserva(@RequestBody Reserva reservaNueva, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        //Verifica que la pista asignada existe
        if (!pistas.containsKey(reservaNueva.courtId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no existe"
            );
        }

        return reservaService.crearReserva(reservaNueva);
    }

    @PatchMapping("/pistaPadel/reservations/{reservationId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Reserva reprogramar(@PathVariable Long reservationId, @RequestBody Reserva reservaNueva, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        //Verifica que la pista asignada existe
        if (!pistas.containsKey(reservaNueva.courtId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no existe"
            );
        }

        return reservaService.reprogramarReserva(reservationId, reservaNueva);
    }

    @GetMapping("/pistaPadel/reservations/{reservationId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Reserva obtenerReserva(@PathVariable Long reservationId) {
        return reservaService.obtenerReserva(reservationId);
    }

    @GetMapping("/pistaPadel/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Reserva> obtenerTodasReservas() {
        return reservaService.obtenerTodasReservas();
    }

    @DeleteMapping("/pistaPadel/reservations/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public void cancelar(@PathVariable Long reservationId) {
        reservaService.cancelarReserva(reservationId);
    }

    // ============================
    // SECCIÓN: DISPONIBILIDAD
    // ============================

    @GetMapping("/pistaPadel/availability")
    public DisponibilidadResponse consultarDisponibilidad(
            @RequestParam(name = "date") String dateStr,
            @RequestParam(name = "courtId", required = false) Long courtId
    ) {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(dateStr);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de fecha inválido (use YYYY-MM-DD)");
        }

        boolean disponible = false;

        if (courtId != null) {
            // A) Buscamos disponibilidad de UNA pista concreta
            if (pistas.containsKey(courtId)) {
                disponible = isPistaLibreEnFecha(courtId, fecha);
            }
        } else {
            // B) Buscamos si hay ALGUNA pista libre
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

    @GetMapping("/pistaPadel/courts/{courtId}/availability")
    public DisponibilidadResponse disponibilidadPistaConcreta(
            @PathVariable Long courtId,
            @RequestParam(name = "date") String dateStr
    ) {
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

    @GetMapping("/pistaPadel/reservations")
    public List<Reserva> listarMisReservas(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestHeader(name = "X-User-Id", required = false) String userId
    ) {
        // 1. Validar Auth (401)
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no identificado");
        }

        // 2. Parsear filtros opcionales
        LocalDate fromDate = (from != null) ? LocalDate.parse(from) : null;
        LocalDate toDate = (to != null) ? LocalDate.parse(to) : null;

        // 3. Filtrar reservas
        return reservas.values().stream()
                .filter(r -> r.userId().equals(userId))
                .filter(r -> fromDate == null || !r.inicio().toLocalDate().isBefore(fromDate))
                .filter(r -> toDate == null || !r.inicio().toLocalDate().isAfter(toDate))
                .collect(Collectors.toList());
    }

    // ============================
    // SECCIÓN: HEALTH
    // ============================

    @GetMapping("/pistaPadel/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    // ============================
    // MÉTODOS HELPER
    // ============================

    private boolean isPistaLibreEnFecha(Long courtId, LocalDate fecha) {
        return reservas.values().stream()
                .filter(r -> r.courtId() == courtId)
                .noneMatch(r -> r.inicio().toLocalDate().equals(fecha));
    }

    //  DE UTILIDAD PARA TESTS:

    void reset() {
        usuarios.clear();
        usuariosporId.clear();
        idUsuarioSeq.set(1);
    }*/
}

