/**
 * Eventos que publica equipos-service.
 * El esquema normativo está en contracts/events/kit.solicitado.json.
 */

export const TIPO_KIT_SOLICITADO = 'kit.solicitado';

/**
 * El equipo avisa al laboratorio que quiere su material (CU-06).
 *
 * Es asíncrono a propósito: el equipo no debe quedarse esperando a que el laboratorio
 * confirme, y si logistica-service está caído la solicitud se encola y se atiende al
 * volver. Quien es dueño de la solicitud y su estado es logistica-service (D-02).
 */
export interface KitSolicitado {
  /** UUID. El consumidor descarta repetidos: RabbitMQ entrega al menos una vez. */
  evento_id: string;
  tipo: typeof TIPO_KIT_SOLICITADO;
  version: 1;
  ocurrido_en: string;
  equipo_id: number;
  practica_programada_id: number;
  grupo_id: number;
  cuenta_responsable: string;
  integrantes: number;
}
