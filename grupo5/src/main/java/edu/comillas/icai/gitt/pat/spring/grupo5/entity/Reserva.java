package edu.comillas.icai.gitt.pat.spring.grupo5.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long reservationId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pista_id", nullable = false)
    public Pista pista;

    @Column(nullable = false)
    public LocalDateTime inicio;

    @Column(nullable = false)
    public LocalDateTime fin;

    @Column(nullable = false)
    public LocalDateTime fechaReservada;

    @Column(nullable = false)
    public Integer duracionMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public EstadoReserva estado;

    @Column(nullable = false)
    public LocalDateTime fechaCreacion;

    public Reserva() {
    }

    public Reserva(Long reservationId, Usuario usuario, Pista pista, LocalDateTime inicio, LocalDateTime fin, 
                   LocalDateTime fechaReservada, Integer duracionMinutos, EstadoReserva estado, LocalDateTime fechaCreacion) {
        this.reservationId = reservationId;
        this.usuario = usuario;
        this.pista = pista;
        this.inicio = inicio;
        this.fin = fin;
        this.fechaReservada = fechaReservada;
        this.duracionMinutos = duracionMinutos;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Pista getPista() {
        return pista;
    }

    public void setPista(Pista pista) {
        this.pista = pista;
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

    public LocalDateTime getFechaReservada() {
        return fechaReservada;
    }

    public void setFechaReservada(LocalDateTime fechaReservada) {
        this.fechaReservada = fechaReservada;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
