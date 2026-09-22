package com.uaemex.laboratorio.iam.repository;

import com.uaemex.laboratorio.iam.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    // Aquí se construyen los SELECT
    Optional<Usuario> findByCuenta(String Cuenta);
}
