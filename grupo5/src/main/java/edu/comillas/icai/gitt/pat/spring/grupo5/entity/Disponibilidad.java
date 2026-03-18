package edu.comillas.icai.gitt.pat.spring.grupo5.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pista_id", nullable = false)
    private Pista pista;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime apertura;

    @Column(nullable = false)
    private LocalTime cierre;

    @OneToMany(mappedBy = "disponibilidad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FranjaDisponible> franjasLibres;

    public Disponibilidad() {}

    public Disponibilidad(Pista pista,
                          LocalDate fecha,
                          LocalTime apertura,
                          LocalTime cierre,
                          List<FranjaDisponible> franjasLibres) {

        this.pista = pista;
        this.fecha = fecha;
        this.apertura = apertura;
        this.cierre = cierre;
        this.franjasLibres = franjasLibres;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pista getPista() { return pista; }
    public void setPista(Pista pista) { this.pista = pista; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getApertura() { return apertura; }
    public void setApertura(LocalTime apertura) { this.apertura = apertura; }

    public LocalTime getCierre() { return cierre; }
    public void setCierre(LocalTime cierre) { this.cierre = cierre; }

    public List<FranjaDisponible> getFranjasLibres() { return franjasLibres; }
    public void setFranjasLibres(List<FranjaDisponible> franjasLibres) { this.franjasLibres = franjasLibres; }

    @Entity
    public static class FranjaDisponible {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "disponibilidad_id", nullable = false)
        private Disponibilidad disponibilidad;

        @Column(nullable = false)
        private LocalTime inicio;

        @Column(nullable = false)
        private LocalTime fin;

        public FranjaDisponible() {}

        public FranjaDisponible(LocalTime inicio, LocalTime fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Disponibilidad getDisponibilidad() { return disponibilidad; }
        public void setDisponibilidad(Disponibilidad disponibilidad) { this.disponibilidad = disponibilidad; }

        public LocalTime getInicio() { return inicio; }
        public void setInicio(LocalTime inicio) { this.inicio = inicio; }

        public LocalTime getFin() { return fin; }
        public void setFin(LocalTime fin) { this.fin = fin; }
    }
}
