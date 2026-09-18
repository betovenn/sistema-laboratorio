/**
 * Modelo de dominio de equipos-service (MS-05).
 *
 * Un EQUIPO es un conjunto temporal de 1 a N alumnos que resuelven juntos una práctica
 * programada. Dura una sesión. No confundir con GRUPO, que es la sección escolar de ~40
 * alumnos que dura un semestre y vive en escolar-service.
 */

export enum EstadoEquipo {
  /** Admite integrantes. El código de unión funciona. */
  ABIERTO = 'ABIERTO',
  /** Ya no admite a nadie. Se puede solicitar el kit. */
  CERRADO = 'CERRADO',
}

export interface Equipo {
  id: number;

  /** Práctica programada de academico-service. Valor, no referencia: son bases distintas. */
  practicaProgramadaId: number;

  /** Grupo de escolar-service, para validar la inscripción de quien se une. */
  grupoId: number;

  /**
   * Clave de 6 caracteres con la que un compañero se une AL EQUIPO.
   * Vive en Redis con TTL de 2 horas y expira solo.
   *
   * No confundir con el código de inscripción, que es de un GRUPO, lo comparte el
   * Profesor y sirve para entrar a la materia.
   */
  codigoUnion: string;

  estado: EstadoEquipo;

  /** Número de cuenta del integrante que recibirá físicamente el kit. */
  cuentaResponsable: string;

  creadoEn: Date;
}

export interface Integrante {
  id: number;
  equipoId: number;

  /**
   * Denormalizado a propósito: permite imponer en la base que un alumno no esté en dos
   * equipos de la misma práctica programada, en vez de confiar en el código.
   */
  practicaProgramadaId: number;

  /** Número de cuenta. Identificador de iam-service, no llave foránea. */
  cuenta: string;

  esResponsable: boolean;
}

/** Un equipo lleno no admite a nadie más. */
export function puedeAdmitir(
  equipo: Equipo,
  integrantesActuales: number,
  maxIntegrantes: number,
): boolean {
  return equipo.estado === EstadoEquipo.ABIERTO && integrantesActuales < maxIntegrantes;
}

/**
 * Una práctica individual se maneja como un equipo de un solo integrante (D-08), de modo
 * que la entrega del kit, el reporte y la calificación siguen un único flujo y no hay
 * casos especiales en logistica-service ni en evaluacion-service.
 */
export function esIndividual(maxIntegrantes: number): boolean {
  return maxIntegrantes === 1;
}

/**
 * Solo se puede pedir el kit con el equipo cerrado: si se pidiera abierto, el laboratorio
 * armaría material para un equipo que todavía puede crecer.
 */
export function puedeSolicitarKit(equipo: Equipo): boolean {
  return equipo.estado === EstadoEquipo.CERRADO;
}
