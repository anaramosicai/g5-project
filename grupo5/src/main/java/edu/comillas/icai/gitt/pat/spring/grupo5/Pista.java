package edu.comillas.icai.gitt.pat.spring.grupo5;

import jakarta.validation.constraints.NotNull;

public record Pista(

    @NotNull
    long idPista,

    @NotNull
    String nombre,

    @NotNull
    String ubicacion,

    @NotNull
    long precioHora,

    @NotNull
    boolean activa,

    @NotNull
    String fechaAlta)

{}
