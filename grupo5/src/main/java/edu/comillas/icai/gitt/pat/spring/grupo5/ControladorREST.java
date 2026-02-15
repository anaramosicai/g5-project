package edu.comillas.icai.gitt.pat.spring.grupo5;



import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class ControladorREST {

    /* ============================
        SECCIÓN: PISTAS
        ============================ */

    Map<Long, Pista> pistas = new ConcurrentHashMap<Long, Pista>();
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
                    "Otra pista con la igual nombre"
            );
        }

        if (idPistaContador > N){
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
        for (Pista p : pistas.values()){
            pistaList.add(p);
        }
        return pistaList;
    }
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/pistaPadel/courts/{courtId}")
    public Pista obtenerDetalle(@PathVariable long courtId){
        Pista pista = pistas.get(courtId);
        if(pista == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "no pista con este id"
            );
        }

        return pista;
    }

    @PatchMapping("/pistaPadel/courts/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Pista modificarPista(@PathVariable long courtId, @RequestBody Pista pistaMod){
        Pista pista = pistas.get(courtId);
        if (pista == null){
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
    public void borrar(@PathVariable long courtId){
        Pista pista = pistas.get(courtId);
        if(pista == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "no pista con este id"
            );
        }
        pistas.remove(courtId);
    }


    /* ============================
        SECCIÓN: POST AUTH - REGISTRO
        ============================ */
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>(); // guardo los usurarios por email
    private final Map<Long, Usuario> usuariosporId = new ConcurrentHashMap<>(); // guardo los usurarios por email
    private final AtomicLong idUsuarioSeq = new AtomicLong(1);

    @PostMapping("/pistaPadel/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario registrarUsuario(@Valid @RequestBody Usuario usuarioNuevo, BindingResult bindingResult) {
        logger.info("Intento de registro para email={}", usuarioNuevo.email());
        logger.debug("Usurario recibido: nombre={}, apellidos={}, telefono={}",
                usuarioNuevo.nombre(), usuarioNuevo.apellidos(), usuarioNuevo.telefono());
        if (bindingResult.hasErrors()) {
            // Error 400 --> datos inválidos
            logger.error("Error inesperado");
            throw new ExcepcionUsuarioIncorrecto(bindingResult);
        }
        if (usuarios.get(usuarioNuevo.email())!= null) {
            // Error 409 --> email ya existe
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
                NombreRol.USER, // rol por defecto
                java.time.LocalDateTime.now(),
                true
        );

        usuariosporId.put(id, u);
        usuarios.put(u.email(), u);

        logger.info("Usuario registrado correctamente id={} email={}", id, usuarioNuevo.email());
        // Devuelve 201 con un DTO de salida SIN password
        return u;
    }

    /* ============================
        SECCIÓN: POST AUTH - LOGIN/TOKEN
        ============================ */

    // Almacén de sesiones (token -> idUsuario)
    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();

    // DTO de entrada
    public record LoginRequest(
            @Email(message = "Email inválido")
            @NotBlank(message = "Email requerido")
            String email,
            @NotBlank(message = "Password requerida")
            String password
    ) {}

    // DTO de salida
    public record LoginResponse(String token) {}

    @PostMapping("/pistaPadel/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        /*ERROR 401 - CREDENCIALES INCORRECTAS*/
        // 1) ¿Existe el usuario?
        Usuario u = usuarios.get(req.email());
        if (u == null) {
            // 401 (no 404) para no filtrar existencia de cuentas
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciales incorrectas");
        }

        // 2) Comprobación password
        boolean ok = req.password().equals(u.password());
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciales incorrectas");
        }

        // 3) Generar token (UUID) y guardarlo en memoria
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, u.idUsuario());

        return new LoginResponse(token);
    }

    /* ============================
        SECCIÓN: POST AUTH - LOGOUT
        ============================ */
    @PostMapping("/pistaPadel/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        String token = extractBearer(authHeader);
        if (token == null || !tokenToUserId.containsKey(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no autenticado");
        }
        tokenToUserId.remove(token);
    }

    /* ============================
        SECCIÓN: GET AUTH - ME
        ============================ */
    @GetMapping("/pistaPadel/auth/me")
    public Usuario me(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        String token = extractBearer(authHeader);
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no autenticado");
        }
        Long userId = tokenToUserId.get(token);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no autenticado");
        }
        Usuario u = usuariosporId.get(userId);
        if (u == null) {
            // Sesión “huérfana”: usuario borrado, etc.
            tokenToUserId.remove(token);
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


}
