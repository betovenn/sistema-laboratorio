package com.uaemex.laboratorio.iam;

import com.uaemex.laboratorio.iam.dominio.Rol;
import com.uaemex.laboratorio.iam.dominio.TipoIdentificador;
import com.uaemex.laboratorio.iam.dominio.Usuario;
import com.uaemex.laboratorio.iam.dominio.UsuarioRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IamApplication {

	public static void main(String[] args) {
		SpringApplication.run(IamApplication.class, args);
	}

	// Solo desarrollo: se activa con IAM_USUARIO_PRUEBA=true mientras no exista /v1/registro
	@Bean
	@ConditionalOnBooleanProperty("iam.usuario-prueba")
	public CommandLineRunner initData(UsuarioRepositorio repositorio) {
		return args -> {
			// Verificar que el usuario existe para no duplicarlo
			if (!repositorio.existsByIdentificador("11111")) {
				// Generar un hash para 12345
				String hashReal = new BCryptPasswordEncoder().encode("12345");
				// Crear y guardar el usuario en la base de datos
				Usuario usuarioPrueba = new Usuario("11111", TipoIdentificador.CUENTA,
						"Alumno de prueba", null, hashReal, Rol.ALUMNO);
				repositorio.save(usuarioPrueba);

				System.out.println("Usuario de ejemplo creado exitosamente");
			}
		};
	}

}
