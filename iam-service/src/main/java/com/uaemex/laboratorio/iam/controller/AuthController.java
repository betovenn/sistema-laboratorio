package com.uaemex.laboratorio.iam.controller;

import com.uaemex.laboratorio.iam.dominio.Usuario;
import com.uaemex.laboratorio.iam.dominio.UsuarioRepositorio;
import com.uaemex.laboratorio.iam.token.EmisorTokens;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class AuthController {
    private final UsuarioRepositorio usuarioRepositorio;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmisorTokens emisorTokens; // Firma los JWT con la llave privada RS256

    // Inyeccion de dependencias por constructor
    public AuthController(UsuarioRepositorio usuarioRepositorio, EmisorTokens emisorTokens) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.emisorTokens = emisorTokens;
    }

    // Definir la estructura de los JSON (en snake_case: token_acceso)
    public record LoginRequest (String identificador, String password) {}
    public record LoginResponse (String tokenAcceso) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (estaVacio(request.identificador()) || estaVacio(request.password())) {
            return ResponseEntity.badRequest().body(new RespuestaError(
                    "datos_invalidos", "Faltan el identificador o la contraseña.", Map.of()));
        }

        // Ubicar al usuario en la BD
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByIdentificador(request.identificador());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Verificar que la cuenta siga activa y que el hash corresponda con el texto plano
            if (usuario.isActivo() && passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
                // Generar y devolver el JWT
                return ResponseEntity.ok(new LoginResponse(emisorTokens.emitirAcceso(usuario)));
            }
        }
        // Retornar http 401 si no existe el usuario, esta inactivo o la contrasena es incorrecta
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RespuestaError(
                "credenciales_invalidas", "Identificador o contraseña incorrectos.", Map.of()));
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
