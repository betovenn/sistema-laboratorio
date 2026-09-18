/**
 * Modelo de dominio de ejecucion-service (MS-06).
 *
 * Conviven dos naturalezas muy distintas en la misma base: una Respuesta se sobrescribe
 * cientos de veces durante la sesión, y un Reporte se escribe una sola vez y nunca cambia.
 * La frontera entre ejecución y evaluación es exactamente el envío final que bloquea la
 * edición (docs/09-decisiones.md, D-01).
 */

export enum EstadoSesion {
  /** Se puede capturar y guardar progreso. */
  EN_PROGRESO = 'EN_PROGRESO',
  /** Enviada: bloqueada para todo el equipo, ya no admite cambios. */
  ENVIADA = 'ENVIADA',
}

export interface Sesion {
  id: number;
  practicaProgramadaId: number;
  /** Único: un equipo tiene una sola sesión por práctica programada. */
  equipoId: number;
  estado: EstadoSesion;
  creadaEn: Date;
}

/**
 * Una respuesta a un campo del formulario. Se sobrescribe con cada autoguardado.
 * `campoId` corresponde al `id` del campo definido por el Profesor en academico-service.
 */
export interface Respuesta {
  id: number;
  sesionId: number;
  campoId: string;
  /** El tipo depende del campo: texto, número u opción. Por eso es jsonb en la base. */
  valor: unknown;
  /** Número de cuenta del integrante que escribió el último valor. */
  actualizadoPor: string;
  actualizadoEn: Date;
}

/** Nombre y cuenta de un integrante, congelados en el reporte al momento del envío. */
export interface IntegranteReporte {
  cuenta: string;
  nombre: string;
}

/**
 * Documento INMUTABLE que se genera al enviar la práctica.
 *
 * Congela los nombres a propósito (D-09): los nombres viven en iam-service, y si se
 * consultaran cada vez que se abre el reporte, este servicio quedaría acoplado a Identidad
 * para siempre. Además es un documento histórico: si un alumno cambia de nombre en agosto,
 * el reporte de marzo no debe cambiar.
 *
 * Nada de este objeto se modifica después de creado.
 */
export interface Reporte {
  id: number;
  sesionId: number;
  /** Número de cuenta del integrante que presionó enviar. */
  enviadoPor: string;
  enviadoEn: Date;
  integrantes: IntegranteReporte[];
  respuestas: Record<string, unknown>;
}

/** Solo se captura mientras la sesión está en progreso. */
export function puedeCapturar(sesion: Sesion): boolean {
  return sesion.estado === EstadoSesion.EN_PROGRESO;
}

/**
 * El envío es irreversible y bloquea la edición para TODO el equipo, no solo para quien
 * envió. Es la transacción que separa ejecución de evaluación.
 */
export function puedeEnviar(sesion: Sesion): boolean {
  return sesion.estado === EstadoSesion.EN_PROGRESO;
}
