package edu.comillas.icai.gitt.pat.spring.grupo5.entity;

import edu.comillas.icai.gitt.pat.spring.grupo5.model.NombreRol;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
public class Usuario{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;

        @Column(nullable = false)
        public String nombre;

        @Column
        public String apellidos;

        @Email(message = "El formato del email es incorrecto")
        @Column(nullable = false, unique = true)
        public String email;

        @Column(nullable = false)
        public String password;

        @Column(nullable = false)
        public String telefono;

        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        public NombreRol rol;

        @Column(nullable = false)
        public LocalDateTime fechaRegistro;

        @Column(nullable = false)
        public Boolean activo;

        public Usuario(Long id, String nombre, String apellidos, String email, String password, String telefono, NombreRol rol, LocalDateTime fechaRegistro, Boolean activo){
                this.id = id;
                this.nombre = nombre;
                this.apellidos = apellidos;
                this.email = email;
                this.password = password;
                this.telefono = telefono;
                this.rol = rol;
                this.fechaRegistro = fechaRegistro;
                this.activo = activo;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public Long getId() {
                return id;
        }

        public void setNombre(String nombre) {
                this.nombre = nombre;
        }

        public String getNombre() {
                return nombre;
        }

        public void setApellidos(String apellidos) {
                this.apellidos = apellidos;
        }

        public String getApellidos() {
                return apellidos;
        }

        public void setEmail(String email) {
                this.email = email;
        }

        public String getEmail() {
                return email;
        }

        public void setPassword(String password) {
                this.password = password;
        }

        public String getPassword() {
                return password;
        }

        public void setRol(NombreRol rol) {
                this.rol = rol;
        }

        public NombreRol getRol() {
                return rol;
        }

        public void setTelefono(String telefono) {
                this.telefono = telefono;
        }

        public String getTelefono() {
                return telefono;
        }

        public void setActivo(Boolean activo) {
                this.activo = activo;
        }

        public Boolean isActivo() {
                return activo;
        }

        public void setFechaRegistro(LocalDateTime fechaRegistro) {
                this.fechaRegistro = fechaRegistro;
        }

        public LocalDateTime getFechaRegistro() {
                return fechaRegistro;
        }
}
