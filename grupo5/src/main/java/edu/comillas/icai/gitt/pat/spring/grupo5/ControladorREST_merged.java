package edu.comillas.icai.gitt.pat.spring.grupo5;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    // ============================
    // SECCIÓN: PISTAS
    // ============================

    private Map<Long, Pista> pistas = new ConcurrentHashMap<>();
    private long idPistaContador = 0;
    private static final int N = 1000;


    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Pista crea(@RequestBody Pista pista) {

        boolean nombreDuplicado = pistas.values().stream()
                .anyMatch(p -> p.nombre().equalsIgnoreCase(pista.nombre()));

        if (nombreDuplicado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Otra pista con igual nombre"
            );
        }

        if (idPistaContador > N) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "idPista es mayor de N"
            );
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
    @GetMapping("/pistaPadel/courts")
    public List<Pista> pistas() {
        ArrayList<Pista> pistaList = new ArrayList<>();
        for (Pista p : pistas.values()) {
            pistaList.add(p);
        }
        return pistaList;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/pistaPadel/courts/{courtId}")
    public Pista obtenerDetalle(@PathVariable long courtId) {
        Pista pista = pistas.get(courtId);
        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No hay una pista con este id"
            );
        }

        return pista;
    }

    @PatchMapping("/pistaPadel/courts/{courtId}")
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

    @DeleteMapping("/pistaPadel/courts/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void borrar(@PathVariable long courtId) {
        Pista pista = pistas.get(courtId);
        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No hay una pista con este id"
            );
        }
        pistas.remove(courtId);
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

    // DTO para Login
    public record LoginRequest(
            @Email(message = "Email inválido")
            @NotBlank(message = "Email requerido")
            String email,
            @NotBlank(message = "Password requerida")
            String password
    ) {}

    // DTO para respuesta de Login
    public record LoginResponse(String token) {}

    @PostMapping("/pistaPadel/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario registrarUsuario(@Valid @RequestBody Usuario usuarioNuevo, BindingResult bindingResult) {
        logger.info("Intento de registro para email={}", usuarioNuevo.email());
        logger.debug("Usuario recibido: nombre={}, apellidos={}, telefono={}",
                usuarioNuevo.nombre(), usuarioNuevo.apellidos(), usuarioNuevo.telefono());
        if (bindingResult.hasErrors()) {
            logger.error("Datos inválidos");
            throw new ExcepcionUsuarioIncorrecto(bindingResult);
        }
        if (usuarios.get(usuarioNuevo.email()) != null) {
            logger.error("este email ya existe");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email ya existe");
        }

        // Generar id en servidor
        long id = idUsuarioSeq.getAndIncrement();

        Usuario u = new Usuario(
                id,
                usuarioNuevo.nombre(),
                usuarioNuevo.apellidos(),
                usuarioNuevo.email(),
                usuarioNuevo.password(),
                usuarioNuevo.telefono(),
                NombreRol.USER,
                java.time.LocalDateTime.now(),
                true
        );

        usuariosporId.put(id, u);
        usuarios.put(u.email(), u);

        logger.info("Usuario registrado correctamente id={} email={}", id, usuarioNuevo.email());
        return u;
    }

    @PostMapping("/pistaPadel/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        // 1) ¿Existe el usuario?
        Usuario u = usuarios.get(req.email());
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciales incorrectas");
        }

        // 2) Comprobación password
        boolean ok = req.password().equals(u.password());
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciales incorrectas");
        }

        // 3) Generar token (UUID) y guardarlo en memoria
        String tokenNuevo = UUID.randomUUID().toString();
        String TokenViejo = userIdToToken.put(u.idUsuario(), tokenNuevo);
        if (TokenViejo != null) tokenToUserId.remove(TokenViejo);
        tokenToUserId.put(tokenNuevo, u.idUsuario());

        return new LoginResponse(tokenNuevo);
    }

    @PostMapping("/pistaPadel/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        String token = extractBearer(authHeader);
        if (token == null || !tokenToUserId.containsKey(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no autenticado");
        }

        Long userId = tokenToUserId.remove(token);

        if (userId != null) {
            userIdToToken.computeIfPresent(userId, (k, v) -> v.equals(token) ? null : v);
        }
    }

    @GetMapping("/pistaPadel/auth/me")
    public Usuario me(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        logger.debug("Authorization header recibido: {}", authHeader);
        String token = extractBearer(authHeader);
        logger.debug("Token extraído: {}", token);
        if (token == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no autenticado");

        Long userId = tokenToUserId.get(token);
        logger.debug("userId buscado por token: {}", userId);
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no autenticado");

        Usuario u = usuariosporId.get(userId);
        if (u == null) {
            tokenToUserId.remove(token);
            userIdToToken.computeIfPresent(userId, (k, v) -> v.equals(token) ? null : v);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no autenticado");
        }
        return u;
    }

    // Función para extraer "Bearer <token>"
    private String extractBearer(String authHeader) {
        if (authHeader == null) return null;
        String prefix = "Bearer ";
        return authHeader.startsWith(prefix) ? authHeader.substring(prefix.length()).trim() : null;
    }

    // ============================
    // SECCIÓN: USUARIOS
    // ============================

    @GetMapping("/pistaPadel/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Collection<Usuario> listarUsuarios() {
        logger.info("Devolucion lista de usuarios registrados");
        return usuariosporId.values()
                .stream()
                .sorted(Comparator.comparing(Usuario::apellidos))
                .toList();
    }

    @GetMapping("/pistaPadel/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario obtenerUsuario(@PathVariable Long userId) {
        if (usuariosporId.get(userId) == null) {
            logger.info("Usuario no encontrado al hacer GET del userId");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return usuariosporId.get(userId);
    }

    @PatchMapping("/pistaPadel/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario actualizarUsuario(@PathVariable Long userId,
                                     @RequestBody Map<String, Object> cambios) {

        logger.info("PATCH /users/{} llamado con cambios: {}", userId, cambios);

        Usuario user = usuariosporId.get(userId);
        if (user == null) {
            logger.info("Usuario no encontrado al hacer PATCH del userId");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        String emailViejo = user.email();

        // Valido email único
        if (cambios.containsKey("email")) {
            String nuevoEmail = (String) cambios.get("email");
            boolean emailExiste = usuariosporId.values().stream()
                    .anyMatch(u -> u.email().equalsIgnoreCase(nuevoEmail)
                            && !u.idUsuario().equals(userId));
            if (emailExiste) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado en app");
            }
        }

        // Bloqueo aquellos campos que no quiera cambiar:
        cambios.remove("idUsuario");
        cambios.remove("rol");
        cambios.remove("fechaRegistro");

        // Creo el nuevo objeto con los cambios realizados:
        Usuario actualizado;
        try {
            actualizado = new Usuario(
                    user.idUsuario(),
                    cambios.containsKey("nombre") ? (String) cambios.get("nombre") : user.nombre(),
                    cambios.containsKey("apellidos") ? (String) cambios.get("apellidos") : user.apellidos(),
                    cambios.containsKey("email") ? (String) cambios.get("email") : user.email(),
                    cambios.containsKey("password") ? (String) cambios.get("password") : user.password(),
                    cambios.containsKey("telefono") ? (String) cambios.get("telefono") : user.telefono(),
                    user.rol(),
                    user.fechaRegistro(),
                    cambios.containsKey("activo") ? (Boolean) cambios.get("activo") : user.activo()
            );

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Campo inválido");
        }
        // Actualizo Map con userId
        usuariosporId.put(userId, actualizado);
        logger.info("Usuario actualizado correctamente: id={}, email={}", actualizado.idUsuario(), actualizado.email());

        // Actualizo también Map con email por sincronización:
        if (!emailViejo.equalsIgnoreCase(actualizado.email())) {
            usuarios.remove(emailViejo);
            usuarios.put(actualizado.email(), actualizado);
        } else {
            usuarios.put(actualizado.email(), actualizado);
        }

        return actualizado;
    }

    // ============================
    // SECCIÓN: RESERVAS
    // ============================

    private final Map<Long, Reserva> reservas = new ConcurrentHashMap<>();
    private long idReservaContador = 0;

    @PostMapping("/pistaPadel/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public Reserva crearReserva(@RequestBody Reserva reservaNueva, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (reservaNueva.inicio().isAfter(reservaNueva.fin())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inicio posterior a fin");
        }

        //Verifica que la pista asignada existe
        if (!pistas.containsKey(reservaNueva.courtId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no existe"
            );
        }

        // Detección solapes (409)
        boolean solapa = reservas.values().stream()
                .filter(r -> r.courtId() == reservaNueva.courtId())
                .anyMatch(r ->
                        reservaNueva.inicio().isBefore(r.fin()) &&
                                reservaNueva.fin().isAfter(r.inicio())
                );

        if (solapa) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Slot ocupado"
            );
        }

        long ReservationId = idReservaContador++;

        Reserva reservaCreada = new Reserva(
                ReservationId,
                reservaNueva.courtId(),
                reservaNueva.userId(),
                reservaNueva.inicio(),
                reservaNueva.fin()
        );

        reservas.put(ReservationId, reservaCreada);
        return reservaCreada;
    }

    @PatchMapping("/pistaPadel/reservations/{reservationId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Reserva reprogramar(@PathVariable long reservationId, @RequestBody Reserva reservaNueva, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Reserva actual = reservas.get(reservationId);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        //Verifica que la pista asignada existe
        if (!pistas.containsKey(reservaNueva.courtId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no existe"
            );
        }

        // Detección solapes (409)
        boolean solapa = reservas.values().stream()
                .filter(r -> r.courtId() == actual.courtId())
                .filter(r -> r.reservationId() != reservationId)
                .anyMatch(r ->
                        reservaNueva.inicio().isBefore(r.fin()) &&
                                reservaNueva.fin().isAfter(r.inicio())
                );

        if (solapa) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        Reserva actualizada = new Reserva(
                reservationId,
                actual.courtId(),
                actual.userId(),
                reservaNueva.inicio(),
                reservaNueva.fin()
        );

        reservas.put(reservationId, actualizada);
        return actualizada;
    }

    @GetMapping("/pistaPadel/reservations/{reservationId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Reserva obtenerReserva(@PathVariable long reservationId) {
        Reserva reserva = reservas.get(reservationId);
        if (reserva == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no hay ninguna reserva con este id");
        }
        return reserva;
    }

    @GetMapping("/pistaPadel/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Reserva> obtenerTodasReservas() {
        ArrayList<Reserva> ReservaList = new ArrayList<>();
        for (Reserva r : reservas.values()) {
            ReservaList.add(r);
        }
        return ReservaList;
    }

    @DeleteMapping("/pistaPadel/reservations/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public void cancelar(@PathVariable long reservationId) {
        Reserva reserva = reservas.get(reservationId);
        if (reserva == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no hay ninguna reserva con este id");
        }
        reservas.remove(reservationId);
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
}

