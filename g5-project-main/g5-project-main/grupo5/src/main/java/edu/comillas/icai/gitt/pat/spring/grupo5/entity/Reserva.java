package edu.comillas.icai.gitt.pat.spring.grupo5.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long reservationId;

    @Column(nullable = false)
    public Long courtId;

    @Column(nullable = false)
    public String userId;

    @Column(nullable = false)
    public LocalDateTime inicio;

    @Column(nullable = false)
    public LocalDateTime fin;

    public Reserva() {
    }

    public Reserva(Long reservationId, Long courtId, String userId, LocalDateTime inicio, LocalDateTime fin) {
        this.reservationId = reservationId;
        this.courtId = courtId;
        this.userId = userId;
        this.inicio = inicio;
        this.fin = fin;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getCourtId() {
        return courtId;
    }

    public void setCourtId(Long courtId) {
        this.courtId = courtId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }
}
