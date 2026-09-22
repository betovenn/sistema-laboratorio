package com.uaemex.laboratorio.iam;

import com.uaemex.laboratorio.iam.model.Usuario;
import com.uaemex.laboratorio.iam.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class IamApplication {

	public static void main(String[] args) {
		SpringApplication.run(IamApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(UsuarioRepository repository) {
		return args -> {
			// Verificar que el usuario existe para no duplicarlo
			if (repository.findByCuenta("11111").isEmpty()) {
				// Generar un hash para 12345
				String hashReal = new BCryptPasswordEncoder().encode("12345");
				// Crear y guardar el usuario en la base de datos
				Usuario usuarioPrueba = new Usuario("11111", hashReal, "ALUMNO");
				repository.save(usuarioPrueba);

				System.out.println("Usuario de ejemplo creado exitosamente");
			}
		};
	}

}
