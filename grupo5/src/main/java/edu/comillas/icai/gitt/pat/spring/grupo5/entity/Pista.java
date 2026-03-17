package edu.comillas.icai.gitt.pat.spring.grupo5.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
public class
Pista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String nombre;

    @Column(nullable = false)
    public String ubicacion;

    @Column(nullable = false)
    public long precioHora;

    @Column(nullable = false)
    public boolean activa;

    @Column(nullable = false)
    public String fechaAlta;

    public Pista(Long id, String nombre, String ubicacion, long precioHora, boolean activa, String fechaAlta){
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.precioHora = precioHora;
        this.activa = activa;
        this.fechaAlta = fechaAlta;
    }
    @OneToMany(mappedBy = "pista")
    private List<Reserva> reservas;

}
