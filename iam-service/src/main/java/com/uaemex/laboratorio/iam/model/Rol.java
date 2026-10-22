package com.uaemex.laboratorio.iam.model;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class Rol {
    @Id 
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String nombre;


    // Constructor vácio
    public Rol () {}

    public Rol (String nombre) {
        this.nombre = nombre;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return this.nombre;
    }

}