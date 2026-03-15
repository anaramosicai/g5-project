package edu.comillas.icai.gitt.pat.spring.grupo5.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Petición de login.
 * Usa @Valid en el controlador para activar estas validaciones.
 */
public record LoginRequest(
        @Email(message = "Email inválido")
        @NotBlank(message = "Email requerido")
        String email,

        @NotBlank(message = "Password requerida")
        String password
) {}
