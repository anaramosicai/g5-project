package edu.comillas.icai.gitt.pat.spring.grupo5;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "disponibilidad")
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "fecha")
    private LocalDate fecha;

    @NotNull
    @Column(name = "court_id")
    private Long courtId;

    @NotNull
    @Column(name = "disponible")
    private boolean disponible;

    @Column(name = "mensaje")
    private String mensaje;

    // Constructores
    public Disponibilidad() {}

    public Disponibilidad(LocalDate fecha, Long courtId, boolean disponible, String mensaje) {
        this.fecha = fecha;
        this.courtId = courtId;
        this.disponible = disponible;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getCourtId() {
        return courtId;
    }

    public void setCourtId(Long courtId) {
        this.courtId = courtId;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    // Método para convertir a DisponibilidadResponse
    public DisponibilidadResponse toResponse() {
        return new DisponibilidadResponse(this.fecha, this.courtId, this.disponible, this.mensaje);
    }
}
