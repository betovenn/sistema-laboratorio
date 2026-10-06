package com.uaemex.laboratorio.iam.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * Sección {@code jwt} de application.yaml.
 *
 * @param emisor        valor del claim {@code iss}.
 * @param accesoMinutos vigencia del token de acceso.
 * @param llavePrivada  archivo PEM (PKCS#8) con la llave RSA que firma los tokens.
 */
@ConfigurationProperties("jwt")
public record PropiedadesJwt(String emisor, int accesoMinutos, Path llavePrivada) {
}
