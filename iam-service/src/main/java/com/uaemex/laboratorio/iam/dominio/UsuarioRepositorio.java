package com.uaemex.laboratorio.iam.dominio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositorio extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByIdentificador(String identificador);

	boolean existsByIdentificador(String identificador);

	boolean existsByCorreo(String correo);

	/**
	 * Resolución en lote de nombres. La usa ejecucion-service para congelar los nombres
	 * de los integrantes dentro del reporte al momento del envío (docs/09-decisiones.md, D-09).
	 */
	List<Usuario> findByIdentificadorIn(Collection<String> identificadores);
}
