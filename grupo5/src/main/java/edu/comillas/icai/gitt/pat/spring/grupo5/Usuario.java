package edu.comillas.icai.gitt.pat.spring.grupo5;

import java.time.LocalDateTime;

public record Usuario(
        Long idUsuario,
        String nombre,
        String apellidos,
        @Email String email, // Validacion de que tiene formato correcto
        String password,
        String telefono,
        Rol rol,
        LocalDateTime fechaRegistro,
        Boolean activo
) {
}