package edu.comillas.icai.gitt.pat.spring.grupo5.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Disponibilidad {

    private Pista pista;
    private LocalDate fecha;
    private LocalTime apertura;
    private LocalTime cierre;
    private List<FranjaDisponible> franjasLibres;

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

    public Pista getPista() { return pista; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getApertura() { return apertura; }
    public LocalTime getCierre() { return cierre; }
    public List<FranjaDisponible> getFranjasLibres() { return franjasLibres; }

    public static class FranjaDisponible {
        private LocalTime inicio;
        private LocalTime fin;

        public FranjaDisponible(LocalTime inicio, LocalTime fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        public LocalTime getInicio() { return inicio; }
        public LocalTime getFin() { return fin; }
    }
}
