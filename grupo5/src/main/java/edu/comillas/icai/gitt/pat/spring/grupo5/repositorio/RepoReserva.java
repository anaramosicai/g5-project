package edu.comillas.icai.gitt.pat.spring.grupo5.repositorio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RepoReserva extends JpaRepository<Reserva, Long> {
    List<Reserva> findByCourtId(Long courtId);
    List<Reserva> findByUserId(String userId);
    List<Reserva> findByCourtIdAndInicioBeforeAndFinAfter(Long courtId, LocalDateTime fin, LocalDateTime inicio);
}
