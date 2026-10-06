package com.uaemex.laboratorio.iam.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.uaemex.laboratorio.iam.dominio.Usuario;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Firma los tokens de acceso con RS256 y publica la clave pública como JWKS.
 *
 * <p>Los demás servicios descargan el JWKS una vez al arrancar y validan cada token
 * localmente: ninguna petición vuelve a llamar a iam-service (docs/05-seguridad.md).
 */
@Component
public class EmisorTokens {

	private static final String COMO_GENERARLA = "Genérala con: openssl genpkey -algorithm RSA"
			+ " -pkeyopt rsa_keygen_bits:2048 -out infra/llaves/privada.pem"
			+ " && chmod 644 infra/llaves/privada.pem";

	private final PropiedadesJwt propiedades;
	private final RSAKey llave;
	private final JWSSigner firmador;

	public EmisorTokens(PropiedadesJwt propiedades) {
		this.propiedades = propiedades;
		this.llave = cargarLlave(propiedades.llavePrivada());
		try {
			this.firmador = new RSASSASigner(llave);
		} catch (JOSEException e) {
			throw new IllegalStateException("La llave privada no sirve para firmar con RS256.", e);
		}
	}

	/**
	 * Token de acceso. Solo lleva quién es ({@code sub}), su rol y la vigencia: la
	 * autorización de negocio se le pregunta a escolar-service, no va en el token.
	 */
	public String emitirAcceso(Usuario usuario) {
		Instant ahora = Instant.now();
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.issuer(propiedades.emisor())
				.subject(usuario.getIdentificador())
				.claim("rol", usuario.getRol().name())
				.issueTime(Date.from(ahora))
				.expirationTime(Date.from(ahora.plus(propiedades.accesoMinutos(), ChronoUnit.MINUTES)))
				.jwtID(UUID.randomUUID().toString())
				.build();
		JWSHeader encabezado = new JWSHeader.Builder(JWSAlgorithm.RS256)
				.type(JOSEObjectType.JWT)
				.keyID(llave.getKeyID())
				.build();

		SignedJWT token = new SignedJWT(encabezado, claims);
		try {
			token.sign(firmador);
		} catch (JOSEException e) {
			throw new IllegalStateException("No se pudo firmar el token de acceso.", e);
		}
		return token.serialize();
	}

	/** El JWKS con la parte pública de la llave. Nunca incluye la privada. */
	public Map<String, Object> jwks() {
		return new JWKSet(llave.toPublicJWK()).toJSONObject();
	}

	private static RSAKey cargarLlave(Path archivo) {
		String pem;
		try {
			pem = Files.readString(archivo);
		} catch (IOException e) {
			throw new IllegalStateException("No se pudo leer la llave privada en " + archivo + ". "
					+ COMO_GENERARLA, e);
		}
		if (pem.contains("BEGIN RSA PRIVATE KEY")) {
			throw new IllegalStateException("La llave en " + archivo + " está en formato PKCS#1 y"
					+ " se espera PKCS#8. " + COMO_GENERARLA);
		}

		String base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");
		try {
			KeyFactory fabrica = KeyFactory.getInstance("RSA");
			RSAPrivateCrtKey privada = (RSAPrivateCrtKey) fabrica.generatePrivate(
					new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64)));
			// La pública se deriva de la privada: no hace falta montar un segundo archivo.
			RSAPublicKey publica = (RSAPublicKey) fabrica.generatePublic(
					new RSAPublicKeySpec(privada.getModulus(), privada.getPublicExponent()));
			return new RSAKey.Builder(publica)
					.privateKey(privada)
					.keyUse(KeyUse.SIGNATURE)
					.algorithm(JWSAlgorithm.RS256)
					.keyIDFromThumbprint()
					.build();
		} catch (GeneralSecurityException | JOSEException | IllegalArgumentException | ClassCastException e) {
			throw new IllegalStateException("La llave en " + archivo + " no es una llave RSA válida. "
					+ COMO_GENERARLA, e);
		}
	}
}
