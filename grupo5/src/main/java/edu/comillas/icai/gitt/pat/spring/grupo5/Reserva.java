package edu.comillas.icai.gitt.pat.spring.grupo5;

import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;

public record Reserva(
        long reservationId,
        @NotNull
        long courtId,
        @NotNull
        String userId,
        @NotNull
        LocalDateTime inicio,
        @NotNull
        LocalDateTime fin
) {}
