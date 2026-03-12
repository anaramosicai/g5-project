package edu.comillas.icai.gitt.pat.spring.grupo5.entity;

import edu.comillas.icai.gitt.pat.spring.grupo5.model.NombreRol;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

@Entity
public record Rol(

        @NotNull
        int idRol,

        @NotNull
        NombreRol nombreRol,

        @NotNull
        String descripcion)
{
}
