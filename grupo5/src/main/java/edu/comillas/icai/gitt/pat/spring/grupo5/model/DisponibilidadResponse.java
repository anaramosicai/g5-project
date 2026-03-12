package edu.comillas.icai.gitt.pat.spring.grupo5.model;

import java.time.LocalDate;

public record DisponibilidadResponse(LocalDate fecha, Long courtId, boolean disponible, String mensaje) {}
