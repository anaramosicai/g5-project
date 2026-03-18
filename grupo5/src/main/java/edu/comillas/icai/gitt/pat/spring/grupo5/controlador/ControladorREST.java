package edu.comillas.icai.gitt.pat.spring.grupo5.controlador;

import edu.comillas.icai.gitt.pat.spring.grupo5.model.*;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.*;
import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.*;
import jakarta.validation.Valid;
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

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ReservaService reservaService;

    @Autowired
    DisponibilidadService disponibilidadService;


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

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();
    private final Map<Long, Usuario> usuariosporId = new ConcurrentHashMap<>();
    private final AtomicLong idUsuarioSeq = new AtomicLong(1);

    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();
    private final Map<Long, String> userIdToToken = new ConcurrentHashMap<>();

    @PostMapping("/auth/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.register(request));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        usuarioService.logout(authHeader);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auth/me")
    public ResponseEntity<UsuarioResponse> me(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(usuarioService.me(authHeader));
    }

    // ============================
    // SECCIÓN: USUARIOS
    // ============================

    // Sin @PreAuthorize("hasRole('ADMIN')") pues dice admin O DUEÑO, que entra con su userId
    @GetMapping("/pistaPadel/users/{userId}")
    public ResponseEntity<UsuarioResponse> getUserById(
            @PathVariable Long userId,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        UsuarioResponse body = usuarioService.obtenerUsuarioPorIdAutorizado(userId, authHeader);
        return ResponseEntity.ok(body); // 200
    }

    @GetMapping("/pistaPadel/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Collection<Usuario> listarUsuarios(){
        return usuarioService.listarUsuarios();
    }

  //  @GetMapping("/pistaPadel/users/{userId}")
   // @PreAuthorize("hasRole('ADMIN')")
    //public Usuario obtenerUsuario(@PathVariable Long userId) {
      //  return usuarioService.obtenerUsuario(userId);
    //}

    @PatchMapping("/pistaPadel/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario actualizarUsuario(@PathVariable Long userId, @RequestBody Map<String, Object> cambios) {
        return usuarioService.actualizarUsuario(userId, cambios);
    }

    // ============================
    // SECCIÓN: RESERVAS
    // ============================
    
    private final Map<Long, Pista> pistas = new ConcurrentHashMap<>();
    private long idPistaContador = 0;

    @PostMapping("/pistaPadel/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public Reserva crearReserva(@RequestBody @Valid Reserva reservaNueva,
                                BindingResult bindingResult,
                                @RequestHeader(name = "Authorization", required = false) String authHeader) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Obtener usuario autenticado
        Usuario usuarioAutenticado = usuarioService.getAuthenticatedUser(authHeader);

        // Delegar a ReservaService (que valida autenticación, autorización y que la pista existe)
        return reservaService.crearReserva(reservaNueva, usuarioAutenticado);
    }

    @PatchMapping("/pistaPadel/reservations/{reservationId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Reserva reprogramar(@PathVariable long reservationId,
                               @RequestBody @Valid Reserva reservaNueva,
                               BindingResult bindingResult,
                               @RequestHeader(name = "Authorization", required = false) String authHeader) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Obtener usuario autenticado
        Usuario usuarioAutenticado = usuarioService.getAuthenticatedUser(authHeader);

        // Delegar a ReservaService (que valida autenticación, autorización y que la pista existe)
        return reservaService.reprogramarReserva(reservationId, reservaNueva, usuarioAutenticado);
    }

    @GetMapping("/pistaPadel/reservations/{reservationId}")
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

    @DeleteMapping("/pistaPadel/reservations/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public void cancelar(@PathVariable long reservationId,
                         @RequestHeader(name = "Authorization", required = false) String authHeader) {

        // Obtener usuario autenticado
        Usuario usuarioAutenticado = usuarioService.getAuthenticatedUser(authHeader);

        // Delegar a ReservaService (que valida autenticación y autorización)
        reservaService.cancelarReserva(reservationId, usuarioAutenticado);
    }
    
    // ============================
    // SECCIÓN: DISPONIBILIDAD
    // ============================

@GetMapping("/pistaPadel/availability")
        public ResponseEntity<List<Disponibilidad>> disponibilidadGeneral(
                @RequestParam(name = "date") String dateStr,
                @RequestParam(name = "courtId", required = false) Long courtId
        ) {
            LocalDate fecha = LocalDate.parse(dateStr);
            return ResponseEntity.ok(disponibilidadService.disponibilidadGeneral(fecha, courtId));
        }

        @GetMapping("/pistaPadel/courts/{courtId}/availability")
        public ResponseEntity<Disponibilidad> disponibilidadPista(
                @PathVariable Long courtId,
                @RequestParam(name = "date") String dateStr
        ) {
            LocalDate fecha = LocalDate.parse(dateStr);
            return ResponseEntity.ok(disponibilidadService.disponibilidadPista(fecha, courtId));
        }

        @GetMapping("/pistaPadel/reservations")
        public ResponseEntity<List<Disponibilidad>> misReservas(
                @RequestHeader(name = "X-User-Id", required = false) Long userId,
                @RequestParam(required = false) String from,
                @RequestParam(required = false) String to
        ) {
            LocalDate f = from != null ? LocalDate.parse(from) : null;
            LocalDate t = to != null ? LocalDate.parse(to) : null;

            return ResponseEntity.ok(disponibilidadService.reservasPorUsuario(userId, f, t));
        }

    // ============================
    // SECCIÓN: HEALTH
    // ============================

    @GetMapping("/pistaPadel/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    //  DE UTILIDAD PARA TESTS:
    /*
    void reset() {
        usuarios.clear();
        usuariosporId.clear();
        idUsuarioSeq.set(1);
    }*/

}

