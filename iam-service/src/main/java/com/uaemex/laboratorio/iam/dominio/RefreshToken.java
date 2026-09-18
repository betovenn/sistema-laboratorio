package com.uaemex.laboratorio.iam.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Token de refresco. Dura 8 horas, lo suficiente para cubrir una jornada de laboratorio
 * sin obligar al alumno a reautenticarse a media práctica.
 *
 * <p>Se guarda el hash, no el token: si alguien lee esta tabla no puede suplantar a nadie.
 */
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "token_hash", nullable = false, length = 255)
	private String tokenHash;

	@Column(name = "expira_en", nullable = false)
	private OffsetDateTime expiraEn;

	@Column(nullable = false)
	private boolean revocado = false;

	protected RefreshToken() {
		// Requerido por JPA.
	}

	public RefreshToken(Usuario usuario, String tokenHash, OffsetDateTime expiraEn) {
		this.usuario = usuario;
		this.tokenHash = tokenHash;
		this.expiraEn = expiraEn;
		this.revocado = false;
	}

	/** Un token sirve solo si no fue revocado y todavía no expira. */
	public boolean esUtilizable() {
		return !revocado && expiraEn.isAfter(OffsetDateTime.now());
	}

	public void revocar() {
		this.revocado = true;
	}

	public UUID getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public OffsetDateTime getExpiraEn() {
		return expiraEn;
	}

	public boolean isRevocado() {
		return revocado;
	}
}
