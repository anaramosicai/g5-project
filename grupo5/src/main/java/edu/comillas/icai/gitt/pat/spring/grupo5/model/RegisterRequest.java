package edu.comillas.icai.gitt.pat.spring.grupo5.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición de registro de usuario.
 * Se valida en el controlador con @Valid (y/o dentro del servicio si lo deseas).
 */
public class RegisterRequest {

    @NotBlank(message = "Nombre requerido")
    private String nombre;

    private String apellidos;

    @Email(message = "El formato del email es incorrecto")
    @NotBlank(message = "Email requerido")
    private String email;

    @NotBlank(message = "Password requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "Teléfono requerido")
    private String telefono;

    public RegisterRequest() { }

    public RegisterRequest(String nombre, String apellidos, String email, String password, String telefono) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}

