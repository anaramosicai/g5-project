package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.jspecify.annotations.NonNull;

public record Rol(

        @NonNull
        int idRol,

        @NonNull
        NombreRol nombreRol,

        @NonNull
        String descripcion)
{
}
