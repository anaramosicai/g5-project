package edu.comillas.icai.gitt.pat.spring.grupo5.entity;

/**
 * Estados posibles de una reserva.
 */
public enum EstadoReserva {
    ACTIVA("Activa"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoReserva(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
