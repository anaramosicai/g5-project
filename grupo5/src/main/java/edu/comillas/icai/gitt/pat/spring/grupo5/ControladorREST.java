package edu.comillas.icai.gitt.pat.spring.grupo5;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class ControladorREST {

    /* ============================
        SECCIÓN: AUTH
        ============================ */
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>(); // guardo los usuarios por email
    private final Map<Long, Usuario> usuariosporId = new ConcurrentHashMap<>(); // guardo los usuarios por email
    private final AtomicLong idUsuarioSeq = new AtomicLong(1);

    @PostMapping("/pistaPadel/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario registrarUsuario(@Valid @RequestBody Usuario usuarioNuevo, BindingResult bindingResult) {
        logger.info("Intento de registro para email={}", usuarioNuevo.email());
        logger.debug("Usuario recibido: nombre={}, apellidos={}, telefono={}",
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


    @GetMapping("/pistaPadel/users")
    @PreAuthorize("hasRole('ADMIN')") // Para solo acceda el rol ADMIN
    public Collection<Usuario> listarUsuarios() {
        logger.info("Devolucion lista de usuarios registrados");
        // Devolveremos la lista de usuarios ordenada alfabéticamente por apellidos:
        return usuariosporId.values()
                .stream()
                .sorted(Comparator.comparing(Usuario::apellidos))
                .toList();
    }


    @GetMapping("/pistaPadel/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario obtenerUsuario(@PathVariable Long userId){
        if (usuariosporId.get(userId) == null){
            //Si no existe:
            logger.info("Usuario no encontrado al hacer GET del userId");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return usuariosporId.get(userId);
    }


    @PatchMapping("/pistaPadel/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario actualizarUsuario(@PathVariable Long userId,
                                     @RequestBody Map<String, Object> cambios){

        logger.info("PATCH /users/{} llamado con cambios: {}", userId, cambios);

        Usuario user = usuariosporId.get(userId);
        if(user == null){
            logger.info("Usuario no encontrado al hacer PATCH del userId");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        String emailViejo = user.email();

        // Valido email unico
        if (cambios.containsKey("email")){
            String nuevoEmail = (String) cambios.get("email");
            boolean emailExiste = usuariosporId.values().stream()
                    .anyMatch(u -> u.email().equalsIgnoreCase(nuevoEmail)
                    && !u.idUsuario().equals(userId));
            if (emailExiste){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado en app");
            }
        }

        // Bloqueo aquellos campos que no quiera cambiar:
        cambios.remove("idUsuario");
        cambios.remove("rol");
        cambios.remove("fechaRegistro");

        // Creo el nuevo objeto con los cambios realizados:
        Usuario actualizado;
        try{
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

        } catch (IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Campo inválido");
        }
        // Actualizo Map con userId
        usuariosporId.put(userId, actualizado);
        logger.info("Usuario actualizado correctamente: id={}, email={}", actualizado.idUsuario(), actualizado.email());

        // Actualizo tambien Map con email por sincronizacion:
        if (!emailViejo.equalsIgnoreCase(actualizado.email())) { // Si son distintos el email viejo y nuevo...
            usuarios.remove(emailViejo);
            usuarios.put(actualizado.email(), actualizado);
        } else {
            usuarios.put(actualizado.email(), actualizado);
        }

        return actualizado;
    }

    // Principalmente pensado para que otro sistema verifique si mi servicio está vivo:
    @GetMapping("/pistaPadel/health")
    public Map<String, String> health(){
        return Map.of("status", "ok");
    }


}
