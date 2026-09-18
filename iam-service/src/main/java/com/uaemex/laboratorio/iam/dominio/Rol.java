package com.uaemex.laboratorio.iam.dominio;

/**
 * Los tres roles del sistema. No existe un rol administrador: la captura de la
 * estructura escolar es responsabilidad del PROFESOR (ver docs/09-decisiones.md, D-03).
 */
public enum Rol {
	ALUMNO,
	PROFESOR,
	LABORATORIO
}
