package edu.comillas.icai.gitt.pat.spring.grupo5.repositorio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Disponibilidad;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Este repositorio NO persiste en base de datos.
 * Solo existe para cumplir la estructura solicitada, ya que
 * Disponibilidad se calcula dinámicamente y no se almacena.
 */
@Repository
public interface RepoDisponibilidad extends CrudRepository<Disponibilidad, Long> {

    // No tiene métodos: Disponibilidad NO debe guardarse.
    // La disponibilidad se calcula en DisponibilidadService.

}
