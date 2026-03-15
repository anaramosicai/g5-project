package edu.comillas.icai.gitt.pat.spring.grupo5.repositorio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Pista;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RepoPista extends CrudRepository<Pista, Long> {
        //Pista findById(Long id);
        boolean existsByNombreIgnoreCase(String nombre);

        @Query(value = "DELETE FROM Pista p WHERE p.idPista = ?", nativeQuery = true)
        int borraPorID(Long id);
}