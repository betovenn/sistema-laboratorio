package com.uaemex.laboratorio.iam.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uaemex.laboratorio.iam.dto.CrearUsuarioRequest;
import com.uaemex.laboratorio.iam.dto.UsuarioResponse;
import com.uaemex.laboratorio.iam.model.Rol;
import com.uaemex.laboratorio.iam.model.Usuario;
import com.uaemex.laboratorio.iam.repository.RolRepository;
import com.uaemex.laboratorio.iam.repository.UsuarioRepository;

@RestController 
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping 
    public ResponseEntity<?> crear(@RequestBody CrearUsuarioRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCuenta(request.cuenta());

        if ( usuarioOpt.isPresent() ) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La cuenta ya existe");
        }

        Optional<Rol> rolOpt = rolRepository.findByNombre(request.rol());
        if ( rolOpt.isEmpty() ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rol no válido");
        } 

        Rol rol = rolOpt.get();
        String passwordHash = passwordEncoder.encode(request.password());
        Usuario usuarioNuevo = new Usuario(request.cuenta(),passwordHash, rol);
        Usuario usuarioGuardado = usuarioRepository.save(usuarioNuevo);

        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(usuarioGuardado));
    }
}
