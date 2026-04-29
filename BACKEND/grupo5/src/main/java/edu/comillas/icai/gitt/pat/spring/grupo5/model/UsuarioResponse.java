package edu.comillas.icai.gitt.pat.spring.grupo5.model;

import java.time.LocalDateTime;

/**
 * Respuesta pública de usuario (para /auth/me y /users/{userId}).
 * No expone password.
 */
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private NombreRol rol;
    private LocalDateTime fechaRegistro;
    private Boolean activo;

    public UsuarioResponse() { }

    public UsuarioResponse(Long id, String nombre, String apellidos, String email, String telefono,
                           NombreRol rol, LocalDateTime fechaRegistro, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.rol = rol;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public NombreRol getRol() { return rol; }
    public void setRol(NombreRol rol) { this.rol = rol; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
