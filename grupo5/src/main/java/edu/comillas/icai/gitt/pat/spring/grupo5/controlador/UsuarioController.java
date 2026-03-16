package edu.comillas.icai.gitt.pat.spring.grupo5.controlador;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.LoginRequest;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.LoginResponse;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.RegisterRequest;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.UsuarioResponse;
import edu.comillas.icai.gitt.pat.spring.grupo5.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pistaPadel")
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    // =========================
    // AUTH
    // =========================

    /**
     * POST /pistaPadel/auth/register
     * 201 creado, 400 datos inválidos, 409 email ya existe
     */
    @PostMapping("/auth/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registrando usuario");

        if (request == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // 409 si email duplicado
        if (usuarioService.emailExists(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este email ya se encuentra registrado");
        }

        UsuarioResponse body = usuarioService.register(request);
        if (body == null) {
            // datos inválidos -> 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos introducidos no son validos");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(body); // 201
    }

    /**
     * POST /pistaPadel/auth/login
     * 200 ok, 400 request inválida, 401 credenciales incorrectas
     */
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Iniciando sesión");

        if (request == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        LoginResponse resp = usuarioService.login(request);
        if (resp == null) {
            // Diferencia 400 vs 401:
            if (request.email() == null || request.email().isBlank()
                    || request.password() == null || request.password().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request invalida");
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autorizado");
        }
        return ResponseEntity.ok(resp); // 200
    }

    /**
     * POST /pistaPadel/auth/logout
     * 204 ok, 401 no autenticado
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        logger.info("Cerrando sesión");
        boolean ok = usuarioService.logout(authHeader);
        if (!ok) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autorizado");
        return ResponseEntity.noContent().build(); // 204
    }

    /**
     * GET /pistaPadel/auth/me
     * 200 ok, 401 no autenticado
     */
    @GetMapping("/auth/me")
    public ResponseEntity<UsuarioResponse> me(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        logger.info("Auntenticacion de usuario");
        UsuarioResponse me = usuarioService.me(authHeader);
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autorizado");
        return ResponseEntity.ok(me); // 200
    }

    // =========================
    // USERS
    // =========================

    /**
     * GET /pistaPadel/users/{userId}
     * (ADMIN o dueño) 200, 401, 403, 404
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UsuarioResponse> getUserById(
            @PathVariable Long userId,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        logger.info("Obtencion de usuario");

        // 401 si no autenticado
        Usuario auth = usuarioService.getAuthenticatedUser(authHeader);
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autorizado");

        // 403 si no es admin ni dueño
        boolean esAdmin = usuarioService.isAdmin(auth);
        boolean esDueno = usuarioService.isOwner(auth, userId);
        if (!esAdmin && !esDueno) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes el rol requerido para esta accion");

        // 404 si no existe el usuario solicitado
        Usuario objetivo = usuarioService.getUsuarioById(userId);
        if (objetivo == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");

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

    /*MARTINA*/

    /** (ADMIN) Listado de usuarios: 200, 401, 403 */
    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> listarUsuarios(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        Usuario auth = usuarioService.getAuthenticatedUser(authHeader);
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (!usuarioService.isAdmin(auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        List<Usuario> lista = usuarioService.listarUsuarios(); // alias para listarTodos()
        return ResponseEntity.ok(lista);
    }

    /** (ADMIN) Actualización parcial: 200, 400, 401, 403, 404, 409 (email duplicado) */
    @PatchMapping("/users/{userId}")
    public ResponseEntity<Usuario> actualizarUsuario(
            @PathVariable Long userId,
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> cambios) {

        Usuario auth = usuarioService.getAuthenticatedUser(authHeader);
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (!usuarioService.isAdmin(auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        Usuario existente = usuarioService.getUsuarioById(userId);
        if (existente == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        if (cambios != null && cambios.containsKey("email")) {
            Object nuevoEmailObj = cambios.get("email");
            if (nuevoEmailObj instanceof String nuevoEmail) {
                boolean duplicado = usuarioService.emailExistsParaOtroUsuario(nuevoEmail, userId);
                if (duplicado) throw new ResponseStatusException(HttpStatus.CONFLICT);
            } else if (nuevoEmailObj != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        Usuario actualizado = usuarioService.actualizarParcial(userId, cambios);
        if (actualizado == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return ResponseEntity.ok(actualizado);
    }

}
