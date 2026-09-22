package com.uaemex.laboratorio.iam.controller;

import com.uaemex.laboratorio.iam.model.Usuario;
import com.uaemex.laboratorio.iam.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Key secretKey; //Llave en memoria para firmar los JWT

    // Inyeccion de dependencias por constructor
    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        // Genera una clave segura HMAC-SHA256 al levantar el servicio.
        // En producción, esta llave debería inyectarse desde el archivo application.yml
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    // Definir la estructura de los JSON
    public record LoginRequest (String cuenta, String password) {}
    public record LoginResponse (String token) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Ubicar al usuario en la BD
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCuenta(request.cuenta());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Verificar que el hash corresponda con el texto plano
            if (passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
                // Generar y devolver el JWT
                String token = generarToken(usuario);
                return ResponseEntity.ok(new LoginResponse(token));
            }
        }
        // Retornar http 401 si no existe el usuario o la contrasena es incorrecta
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales invalidas\n");
    }

    private String generarToken(Usuario usuario) {
        long horasEnMilisegundos = 2 * 60 * 60 * 1000; // 2 horas
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + horasEnMilisegundos);

        return Jwts.builder()
                .setSubject(usuario.getId().toString())
                .claim("cuenta", usuario.getCuenta())
                .claim("rol", usuario.getRol())
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(secretKey)
                .compact();
    }
}
