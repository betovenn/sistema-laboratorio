package com.uaemex.laboratorio.iam.dominio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositorio extends JpaRepository<RefreshToken, UUID> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	@Modifying
	@Transactional
	@Query("update RefreshToken t set t.revocado = true where t.usuario.id = :usuarioId")
	int revocarTodosDe(UUID usuarioId);

	@Modifying
	@Transactional
	@Query("delete from RefreshToken t where t.expiraEn < :momento")
	int borrarExpirados(OffsetDateTime momento);
}
