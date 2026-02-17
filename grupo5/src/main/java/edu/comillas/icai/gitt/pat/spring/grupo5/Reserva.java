package edu.comillas.icai.gitt.pat.spring.grupo5;

import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;

public record Reserva(
        long reservationId,
        @NonNull
        long courtId,
        @NonNull
        String userId,
        @NonNull
        LocalDateTime inicio,
        @NonNull
        LocalDateTime fin
) {}
