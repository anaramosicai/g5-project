package edu.comillas.icai.gitt.pat.spring.grupo5.repositorio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RepoUsuario extends CrudRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

}
