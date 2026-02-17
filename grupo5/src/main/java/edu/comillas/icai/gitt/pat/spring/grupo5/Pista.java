package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.jspecify.annotations.NonNull;

public record Pista(

    @NonNull
    long idPista,

    @NonNull
    String nombre,

    @NonNull
    String ubicacion,

    @NonNull
    long precioHora,

    @NonNull
    boolean activa,

    @NonNull
    String fechaAlta)

{}
