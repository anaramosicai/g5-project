package edu.comillas.icai.gitt.pat.spring.grupo5.repositorio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RepoReserva extends CrudRepository<Reserva, Long> {
    List<Reserva> findByPista_Id(Long pistaId);
    List<Reserva> findByUsuario_Id(Long usuarioId);
    List<Reserva> findByPista_IdAndInicioBeforeAndFinAfter(Long pistaId, LocalDateTime fin, LocalDateTime inicio);
}
