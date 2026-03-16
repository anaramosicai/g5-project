package edu.comillas.icai.gitt.pat.spring.grupo5.repositorio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Rol;
import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import org.springframework.data.repository.CrudRepository;

public interface RepoRol extends CrudRepository<Rol, Long> {
}
