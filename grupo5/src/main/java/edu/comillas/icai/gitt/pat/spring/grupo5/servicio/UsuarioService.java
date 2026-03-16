package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoUsuario;
import org.springframework.beans.factory.annotation.Autowired;

public class UsuarioService {
    @Autowired
    RepoUsuario repoUsuario;

    public String login(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) return null;
        if (!usuario.password.equals(password)) return null;

        String token = tokenRepository.findByUsuario(usuario);
        if (token != null) return token;

        token = new Token();
        token.usuario = usuario;
        return tokenRepository.save(token);
    }
}
