package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoPista;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
@Service
public class PistaService {
    @Autowired
    private RepoPista repoPista;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Transactional
    public Pista crea(Pista pistaNuevo){
        logger.info("ServicioPista: Trying to create a pista: " + pistaNuevo.id);

        if(repoPista.existsByNombreIgnoreCase(pistaNuevo.nombre)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another Pista with the same name");
        }

        Pista pista = new Pista(
                pistaNuevo.id,
                pistaNuevo.nombre,
                pistaNuevo.ubicacion,
                pistaNuevo.precioHora,
                pistaNuevo.activa,
                pistaNuevo.fechaAlta
        );

        return repoPista.save(pista);
    }

    public List<Pista> leeTodas(){
        List<Pista> pistas = new ArrayList<>();
        repoPista.findAll().forEach(pistas::add);
        return pistas;
    }

    public Pista lee(long id){
        logger.info("ServicioPista: Read pista with id " + id);

        return repoPista.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "couldn't find pista with this id"));
    }

    @Transactional
    public Pista cambiar(Pista pistaNuevo, long id){
        Pista pista = repoPista.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Pista pistaActualizada = new Pista(
                id,
                pistaNuevo.nombre,
                pistaNuevo.ubicacion,
                pistaNuevo.precioHora,
                pistaNuevo.activa,
                pistaNuevo.fechaAlta
        );
        return repoPista.save(pistaActualizada);
    }

    @Transactional
    public void borrar(long id){
        Pista pista = repoPista.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Couldn't find pista with this id"));

        repoPista.delete(pista);
    }


}
