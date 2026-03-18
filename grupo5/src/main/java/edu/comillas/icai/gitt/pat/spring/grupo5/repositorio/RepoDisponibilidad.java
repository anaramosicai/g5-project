package edu.comillas.icai.gitt.pat.spring.grupo5.repositorio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Disponibilidad;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepoDisponibilidad extends CrudRepository<Disponibilidad, Long> {

    /**
     * Busca disponibilidades por ID de pista y fecha
     */
    List<Disponibilidad> findByPistaIdAndFecha(Long pistaId, LocalDate fecha);

    /**
     * Busca todas las disponibilidades de una pista
     */
    List<Disponibilidad> findByPistaId(Long pistaId);

    /**
     * Busca disponibilidades por fecha
     */
    List<Disponibilidad> findByFecha(LocalDate fecha);

}
