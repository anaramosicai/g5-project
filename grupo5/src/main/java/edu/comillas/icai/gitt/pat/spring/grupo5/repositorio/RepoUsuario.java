package edu.comillas.icai.gitt.pat.spring.grupo5.repositorio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import org.springframework.data.repository.CrudRepository;

public interface RepoUsuario extends CrudRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    Usuario findByNombre(String nombre);
    // Crud tiene implícito: Iterable<Usuario> findAll();
    boolean existsByEmailAndIdNot(String email, Long id);
}
