package edu.comillas.icai.gitt.pat.spring.grupo5.repositorio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Disponibilidad;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepoDisponibilidad extends CrudRepository<Disponibilidad, Long> {

    List<Disponibilidad> findByPista_Id(Long pistaId);

    Optional<Disponibilidad> findByPista_IdAndFecha(Long pistaId, LocalDate fecha);

    List<Disponibilidad> findByFecha(LocalDate fecha);

}
