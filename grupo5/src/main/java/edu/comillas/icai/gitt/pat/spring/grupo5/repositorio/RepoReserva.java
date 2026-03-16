package edu.comillas.icai.gitt.pat.spring.grupo5.repositorio;

public interface RepoReserva extends CrudRepository<Reserva, Long> {
    List<Reserva> findByFecha(LocalDate fecha);
    List<Reserva> findByInicioBetween(LocalDateTime inicio, LocalDateTime fin);
}
