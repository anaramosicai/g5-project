package edu.comillas.icai.gitt.pat.spring.grupo5.entity;

import jakarta.validation.constraints.NotNull;

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
