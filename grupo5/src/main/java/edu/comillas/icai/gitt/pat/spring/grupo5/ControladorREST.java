package edu.comillas.icai.gitt.pat.spring.grupo5;



import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        SECCIÓN: AUTH
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

}
