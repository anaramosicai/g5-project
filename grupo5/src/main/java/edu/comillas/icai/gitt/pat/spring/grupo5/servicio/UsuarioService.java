package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.model.*;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoUsuario;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de usuarios, estilo simple:
 *  - Expone utilidades para que el controlador decida los códigos HTTP.
 *  - Devuelve null/valores simples (no lanza ResponseStatusException).
 *  - Gestiona tokens en memoria (token -> userId, expiry).
 */
@Service
public class UsuarioService {

    @Autowired
    RepoUsuario repoUsuario;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // =========================
    // Gestión de tokens
    // =========================
    private static class TokenInfo {
        final Long userId;
        final Instant expiry;
        TokenInfo(Long userId, Instant expiry) {
            this.userId = userId;
            this.expiry = expiry;
        }
    }

    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();
    private final Duration tokenTtl = Duration.ofHours(8);

    private String issueToken(Usuario u) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, new TokenInfo(u.getId(), Instant.now().plus(tokenTtl)));
        return token;
    }

    public void invalidateToken(String token) {
        if (token != null) tokenStore.remove(token);
    }

    public Long verifyAndGetUserId(String token) {
        if (token == null || token.isBlank()) return null;
        TokenInfo info = tokenStore.get(token);
        if (info == null) return null;
        if (Instant.now().isAfter(info.expiry)) {
            tokenStore.remove(token);
            return null;
        }
        return info.userId;
    }

    public static String extractToken(String authHeader) {
        if (authHeader == null) return null;
        String h = authHeader.trim();
        if (h.toLowerCase().startsWith("bearer ")) return h.substring(7).trim();
        return h;
    }

    // =========================
    // Funciones para el  controlador
    // =========================

    /** True si el email ya existe (para mapear 409 en el controlador). */
    public boolean emailExists(String email) {
        return email != null && repoUsuario.existsByEmail(email);
    }

    /** Recupera el usuario autenticado o null si token inválido/no presente. */
    @Transactional(readOnly = true)
    public Usuario getAuthenticatedUser(String rawAuthHeader) {
        String token = extractToken(rawAuthHeader);
        Long userId = verifyAndGetUserId(token);
        if (userId == null) return null;
        return repoUsuario.findById(userId).orElse(null);
    }

    /** Devuelve el usuario por id o null si no existe (para 404). */
    @Transactional(readOnly = true)
    public Usuario getUsuarioById(Long userId) {
        return repoUsuario.findById(userId).orElse(null);
    }

    /** True si es ADMIN. */
    public boolean isAdmin(Usuario u) {
        return u != null && u.getRol() == NombreRol.ADMIN;
    }

    /** True si es dueño del recurso (mismo id). */
    public boolean isOwner(Usuario u, Long targetUserId) {
        return u != null && u.getId() != null && u.getId().equals(targetUserId);
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private UsuarioResponse toResponse(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(u.getId());
        r.setNombre(u.getNombre());
        r.setApellidos(u.getApellidos());
        r.setEmail(u.getEmail());
        r.setTelefono(u.getTelefono());
        r.setRol(u.getRol());
        r.setFechaRegistro(u.getFechaRegistro());
        r.setActivo(u.isActivo());
        return r;
    }

    // =========================
    // Casos de uso
    // =========================

    /**
     * /auth/register
     * 201 creado, 400 datos inválidos, 409 email ya existe
     */
    @Transactional
    public UsuarioResponse register(@Valid RegisterRequest request) {

        if (request == null ||
                request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank() ||
                request.getNombre() == null || request.getNombre().isBlank() ||
                request.getTelefono() == null || request.getTelefono().isBlank()) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos inválidos");
        }

        if (repoUsuario.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya existe");
        }

        Usuario u = new Usuario(
                null,
                request.getNombre(),
                request.getApellidos(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getTelefono(),
                NombreRol.USER,
                LocalDateTime.now(),
                true
        );

        return toResponse(repoUsuario.save(u));
    }

    /**
     * /pistaPadel/auth/login
     * 200 ok, 400 request inválida, 401 credenciales incorrectas
     */
    @Transactional(readOnly = true)
    public LoginResponse login(@Valid LoginRequest request) {

        if (request == null ||
                request.email() == null || request.email().isBlank() ||
                request.password() == null || request.password().isBlank()) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request inválida");
        }

        Usuario user = repoUsuario.findByEmail(request.email());

        if (user == null || !Boolean.TRUE.equals(user.isActivo())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        String token = issueToken(user);

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        return resp;
    }

    /**
     * /pistaPadel/auth/logout
     * 204 ok, 401 no autenticado
     *
     * @return
     */
    @Transactional
    public boolean logout(String rawAuthHeader) {

        String token = extractToken(rawAuthHeader);
        Long userId = verifyAndGetUserId(token);

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        invalidateToken(token);
        return false;
    }

    /**
     * /pistaPadel/auth/me
     * 200 ok, 401 no autenticado
     */
    @Transactional(readOnly = true)
    public UsuarioResponse me(String rawAuthHeader) {

        Usuario u = getAuthenticatedUser(rawAuthHeader);

        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        return toResponse(u);
    }

    /**
     * /pistaPadel/users/{userId}
     * (ADMIN o dueño) 200, 401, 403, 404
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioPorIdAutorizado(Long userId, String rawAuthHeader) {

        // 1) Autenticación → 401
        Usuario auth = getAuthenticatedUser(rawAuthHeader);
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        // 2) Autorización → 403
        boolean esAdmin = isAdmin(auth);
        boolean esDueno = isOwner(auth, userId);
        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        // 3) Usuario destino existe → 404
        Usuario objetivo = repoUsuario.findById(userId).orElse(null);
        if (objetivo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe");
        }

        // 4) OK → 200
        return new UsuarioResponse(
                objetivo.getId(),
                objetivo.getNombre(),
                objetivo.getApellidos(),
                objetivo.getEmail(),
                objetivo.getTelefono(),
                objetivo.getRol(),
                objetivo.getFechaRegistro(),
                objetivo.isActivo()
        );
    }

    /* Parte de martina */

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Collection<Usuario> listarUsuarios() {
        logger.info("Devolucion lista de usuarios registrados");
        
        Iterable<Usuario> iterable = repoUsuario.findAll();
        List<Usuario> usuariosRegistrados = new ArrayList<>();
        iterable.forEach(usuariosRegistrados::add);
        
        if (usuariosRegistrados.isEmpty()) {
            logger.info("No hay usuarios registrados");
        }
        return usuariosRegistrados;
    }

    public Usuario obtenerUsuario(Long userId) {
        Optional<Usuario> usuarioBuscado = repoUsuario.findById(userId);

        if (usuarioBuscado.isEmpty()) {
            logger.info("Usuario no encontrado al hacer GET del userId");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return usuarioBuscado.get();
    }

    public Usuario actualizarUsuario(Long userId, Map<String, Object> cambios) {

        logger.info("PATCH /users/{} llamado con cambios: {}", userId, cambios);

        Usuario user = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String emailViejo = user.getEmail();

        // Valido email único:
        if (cambios.containsKey("email")) {
            String nuevoEmail = (String) cambios.get("email");
            boolean emailExiste = repoUsuario.existsByEmailAndIdNot(nuevoEmail, userId);

            if (emailExiste) { throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado en app");}
            user.setEmail(nuevoEmail);
        }

        // Campos permitidos
        if (cambios.containsKey("nombre"))
            user.setNombre((String) cambios.get("nombre"));
        if (cambios.containsKey("apellidos"))
            user.setApellidos((String) cambios.get("apellidos"));
        if (cambios.containsKey("password"))
            user.setPassword((String) cambios.get("password"));
        if (cambios.containsKey("telefono"))
            user.setTelefono((String) cambios.get("telefono"));
        if (cambios.containsKey("activo"))
            user.setActivo((Boolean) cambios.get("activo"));

        Usuario actualizado = repoUsuario.save(user);
        logger.info("Usuario actualizado correctamente: id={}, email={}",
                actualizado.getId(), actualizado.getEmail());

        return actualizado;
    }
    public List<Usuario> obtenerTodosUsuarios() {
        return (List<Usuario>) repoUsuario.findAll();
    }
}