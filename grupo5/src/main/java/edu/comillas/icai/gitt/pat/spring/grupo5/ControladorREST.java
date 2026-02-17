package edu.comillas.icai.gitt.pat.spring.grupo5;


import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ControladorREST {

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
                    "idPista es major de N"
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
}
