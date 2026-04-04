package edu.comillas.icai.gitt.pat.spring.grupo5.entity;

import edu.comillas.icai.gitt.pat.spring.grupo5.model.NombreRol;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class Rol{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;

        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        NombreRol nombreRol;

        @Column(nullable = false)
        String descripcion;

}
