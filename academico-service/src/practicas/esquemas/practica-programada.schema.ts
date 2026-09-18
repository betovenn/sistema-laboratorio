import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument, Types } from 'mongoose';

export enum Modalidad {
  INDIVIDUAL = 'INDIVIDUAL',
  EQUIPO = 'EQUIPO',
}

export enum EstadoProgramacion {
  PROGRAMADA = 'PROGRAMADA',
  EN_CURSO = 'EN_CURSO',
  CERRADA = 'CERRADA',
}

/**
 * Una práctica asignada a un grupo, una fecha y una modalidad.
 *
 * Es la unidad contra la que se calcula la demanda, se entregan los kits y se califica.
 * La consultan logistica-service, equipos-service y ejecucion-service.
 *
 * Al crearla hay que validar contra escolar-service que el profesor que programa esté
 * asignado al grupo (CU-02).
 */
@Schema({ collection: 'practicas_programadas', timestamps: true })
export class PracticaProgramada {
  @Prop({ type: Types.ObjectId, ref: 'Practica', required: true })
  practicaId: Types.ObjectId;

  /** Id del grupo en escolar-service. Número, no referencia: son bases distintas. */
  @Prop({ required: true, index: true }) grupoId: number;

  @Prop({ required: true, index: true }) fecha: Date;
  @Prop({ required: true }) horaInicio: string;
  @Prop({ required: true }) horaFin: string;

  @Prop({ required: true, enum: Modalidad }) modalidad: Modalidad;

  /**
   * Máximo de integrantes por equipo. En modalidad INDIVIDUAL vale 1:
   * una práctica individual se maneja como un equipo de un solo integrante (D-08),
   * para no duplicar los flujos de entrega, reporte y calificación.
   */
  @Prop({ required: true, min: 1, default: 1 }) maxIntegrantes: number;

  @Prop({ required: true, enum: EstadoProgramacion, default: EstadoProgramacion.PROGRAMADA })
  estado: EstadoProgramacion;
}

export type PracticaProgramadaDocument = HydratedDocument<PracticaProgramada>;
export const PracticaProgramadaSchema = SchemaFactory.createForClass(PracticaProgramada);
