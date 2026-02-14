package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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
