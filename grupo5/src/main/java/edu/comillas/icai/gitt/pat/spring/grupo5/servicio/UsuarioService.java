package edu.comillas.icai.gitt.pat.spring.grupo5.servicio;

import edu.comillas.icai.gitt.pat.spring.grupo5.entity.Usuario;
import edu.comillas.icai.gitt.pat.spring.grupo5.repositorio.RepoUsuario;
import org.springframework.beans.factory.annotation.Autowired;

@Service
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

    public Collection<Usuario> listarUsuarios() {
        logger.info("Devolucion lista de usuarios registrados");
        List<Usuario> usuariosRegistrados = repoUsuario.findAll();
        if (usuariosRegistrados.isEmpty()) {
            logger.info("No hay usuarios registrados");
        }
        return usuariosRegistrados;
    }

    public Usuario obtenerUsuario(Long userId) {
        Optional<Usuario> usuarioBuscado = repoUsuario.findById(userId);

        if (usuarioBuscado.isEmpty()) {
            logger.info("Usuario no encontrado al hacer GET del userId");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return usuarioBuscado.get();
    }

    public Usuario actualizarUsuario(Long userId, Map<String, Object> cambios) {

        logger.info("PATCH /users/{} llamado con cambios: {}", userId, cambios);

        Usuario user = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String emailViejo = user.getEmail();

        // Valido email único:
        if (cambios.containsKey("email")) {
            String nuevoEmail = (String) cambios.get("email");
            boolean emailExiste = repoUsuario.existsByEmailAndIdNot(nuevoEmail, userId);

            if (emailExiste) { throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado en app");}
            user.setEmail(nuevoEmail);
        }

        // Campos permitidos
        if (cambios.containsKey("nombre"))
            user.setNombre((String) cambios.get("nombre"));
        if (cambios.containsKey("apellidos"))
            user.setApellidos((String) cambios.get("apellidos"));
        if (cambios.containsKey("password"))
            user.setPassword((String) cambios.get("password"));
        if (cambios.containsKey("telefono"))
            user.setTelefono((String) cambios.get("telefono"));
        if (cambios.containsKey("activo"))
            user.setActivo((Boolean) cambios.get("activo"));

        Usuario actualizado = repoUsuario.save(user);
        logger.info("Usuario actualizado correctamente: id={}, email={}",
                actualizado.getId(), actualizado.getEmail());

        return actualizado;
    }
}
