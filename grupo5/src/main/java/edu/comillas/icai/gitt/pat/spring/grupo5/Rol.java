package edu.comillas.icai.gitt.pat.spring.grupo5;

import jakarta.validation.constraints.NotNull;

public record Rol(

        @NotNull
        int idRol,

        @NotNull
        NombreRol nombreRol,

        @NotNull
        String descripcion)
{
}
