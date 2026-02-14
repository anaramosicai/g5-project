package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ControladorREST {
    private final Map<Long, Usuario> usuarios = new ConcurrentHashMap<>(); // Para poder arreglar la concurrencia en delete
    //private Logger logger = LoggerFactory.getLogger(getClass());

    // El endpoint siempre se acompaña de la URL entre paréntesis
    @GetMapping("/pistaPadel/users")
    @PreAuthorize("hasRole('ADMIN')") // Para solo acceda el rol ADMIN
    public Collection<Usuario> listarUsuarios() {
        /*
        logger.error("Cuando se produce un error inesperado en la lógica que hay que revisar");
        logger.warn("Cuando la lógica detecta algo que hay que mantener");
        logger.info("Información importante para hacer seguimiento de la ejecución");
        logger.debug("Información que ayuda a identificar problemas");
        logger.trace("Trazas de ayuda durante el {}", "desarrollo"); // No se da por estar en un nivel inferior
*/
        // Devolveremos la lista de usuarios ordenada alfabéticamente por apellidos:
        return usuarios.values()
                .stream()
                .sorted(Comparator.comparing(Usuario::apellidos))
                .toList();

    }

    @GetMapping("/pistaPadel/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario obtenerUsuario(@PathVariable Long idUsuario){
        if (usuarios.get(idUsuario) == null){
            //Si no existe:
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return usuarios.get(idUsuario);
    }
/*
    @PatchMapping("/pistaPadel/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario actualizarUsuario(@PathVariable Long idUsuario){

        // PREGUNTAR A ATILANO SOBRE QUÉ DATOS DEBO ACTUALIZAR (si solo el id o los que me pasen en el body)

    }*/


}
