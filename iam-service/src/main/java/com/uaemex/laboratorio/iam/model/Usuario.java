package com.uaemex.laboratorio.iam.model;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String cuenta;

    @Column(name = "passwordHash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String rol;

    // Constructors
    public Usuario() {}

    public Usuario(String cuenta, String passwordHash, String rol) {
        this.cuenta = cuenta;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    // Getters and setters ---------------------------------------------------------

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getCuenta() {
        return cuenta;
    }
    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRol() {
        return rol;
    }
    public void setRol(String rol) {
        this.rol = rol;
    }
}
