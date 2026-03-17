package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

<<<<<<< martina_branch
@Repository
public interface RepoDisponibilidad extends <JpaRepository<edu.comillas.icai.gitt.pat.spring.grupo5.>Disponibilidad, Long> {

    /**
     * Busca disponibilidades por fecha y courtId
     */
    Optional<Disponibilidad> findByFechaAndCourtId(LocalDate fecha, Long courtId);

    /**
     * Busca todas las disponibilidades de una pista en un rango de fechas
     */
    List<Disponibilidad> findByCourtIdAndFechaBetween(Long courtId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca todas las disponibilidades en una fecha determinada
     */
    List<Disponibilidad> findByFecha(LocalDate fecha);

    /**
     * Verifica si hay disponibilidad para una pista en una fecha específica
     */
    List<Disponibilidad> findByFechaAndCourtIdAndDisponible(LocalDate fecha, Long courtId, boolean disponible);
}
=======

@Repository
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    */
/**
     * Busca disponibilidades por fecha y courtId
     *//*

    Optional<Disponibilidad> findByFechaAndCourtId(LocalDate fecha, Long courtId);

    */
/**
     * Busca todas las disponibilidades de una pista en un rango de fechas
     *//*

    List<Disponibilidad> findByCourtIdAndFechaBetween(Long courtId, LocalDate fechaInicio, LocalDate fechaFin);

    */
/**
     * Busca todas las disponibilidades en una fecha determinada
     *//*

    List<Disponibilidad> findByFecha(LocalDate fecha);

    */
/**
     * Verifica si hay disponibilidad para una pista en una fecha específica
     *//*

    List<Disponibilidad> findByFechaAndCourtIdAndDisponible(LocalDate fecha, Long courtId, boolean disponible);
}

>>>>>>> main
