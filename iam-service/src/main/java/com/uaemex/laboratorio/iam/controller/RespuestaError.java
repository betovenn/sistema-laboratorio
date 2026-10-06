package com.uaemex.laboratorio.iam.controller;

import java.util.Map;

/** Cuerpo de error común a los siete servicios (docs/07-convenciones.md). */
public record RespuestaError(String error, String mensaje, Map<String, Object> detalles) {
}
