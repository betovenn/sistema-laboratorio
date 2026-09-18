package com.uaemex.laboratorio.iam.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Persona que usa el sistema.
 *
 * <p>Esta es la única tabla de todo el sistema que conoce nombres y contraseñas. Los demás
 * servicios guardan solo el {@code identificador} —número de cuenta o de empleado— como
 * texto, y nunca como llave foránea: es el identificador compartido entre servicios, y
 * ninguna base puede validarlo contra otra.
 *
 * <p>Las cuentas se crean por autorregistro (CU-14). Hueco conocido: el rol queda
 * autodeclarado, así que hoy cualquiera que use un número de empleado entra como PROFESOR.
 * Ver docs/05-seguridad.md.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	/** Número de cuenta o de empleado. Único en todo el sistema. */
	@Column(nullable = false, unique = true, length = 15)
	private String identificador;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private TipoIdentificador tipo;

	@Column(nullable = false, length = 120)
	private String nombre;

	@Column(unique = true, length = 120)
	private String correo;

	/** Hash bcrypt o argon2. Nunca la contraseña en claro. */
	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 15)
	private Rol rol;

	@Column(nullable = false)
	private boolean activo = true;

	@Column(name = "creado_en", nullable = false)
	private OffsetDateTime creadoEn;

	protected Usuario() {
		// Requerido por JPA.
	}

	public Usuario(String identificador, TipoIdentificador tipo, String nombre,
			String correo, String passwordHash, Rol rol) {
		this.identificador = identificador;
		this.tipo = tipo;
		this.nombre = nombre;
		this.correo = correo;
		this.passwordHash = passwordHash;
		this.rol = rol;
		this.activo = true;
	}

	@PrePersist
	void alCrear() {
		if (creadoEn == null) {
			creadoEn = OffsetDateTime.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public String getIdentificador() {
		return identificador;
	}

	public TipoIdentificador getTipo() {
		return tipo;
	}

	public String getNombre() {
		return nombre;
	}

	public String getCorreo() {
		return correo;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Rol getRol() {
		return rol;
	}

	public boolean isActivo() {
		return activo;
	}

	public OffsetDateTime getCreadoEn() {
		return creadoEn;
	}

	public void cambiarNombre(String nombre) {
		this.nombre = nombre;
	}

	public void cambiarPassword(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void desactivar() {
		this.activo = false;
	}
}
