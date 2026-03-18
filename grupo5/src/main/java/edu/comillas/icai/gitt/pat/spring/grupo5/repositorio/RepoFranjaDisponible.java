package edu.comillas.icai.gitt.pat.spring.grupo5.repositorio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Disponibilidad;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepoFranjaDisponible extends CrudRepository<Disponibilidad.FranjaDisponible, Long> {

    /**
     * Busca todas las franjasDisponibles de una disponibilidad
     */
    List<Disponibilidad.FranjaDisponible> findByDisponibilidad(Disponibilidad disponibilidad);

    /**
     * Busca todas las franjasDisponibles de una disponibilidad por ID
     */
    List<Disponibilidad.FranjaDisponible> findByDisponibilidadId(Long disponibilidadId);
}
