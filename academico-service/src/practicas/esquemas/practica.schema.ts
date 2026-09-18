import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument } from 'mongoose';

/** Tipos de campo que el Profesor puede poner en el cuestionario. */
export enum TipoCampo {
  TEXTO = 'texto',
  NUMERO = 'numero',
  OPCION_MULTIPLE = 'opcion_multiple',
}

/**
 * Si la cantidad declarada de un componente se consume por equipo o por alumno.
 *
 * Es el campo del que depende TODO el cálculo de demanda (docs/09-decisiones.md, D-06).
 * Un osciloscopio se consume por equipo; unos guantes, por alumno. Sin este dato la
 * multiplicación es ambigua y la funcionalidad de más valor del sistema no se puede hacer.
 */
export enum UnidadConsumo {
  POR_EQUIPO = 'POR_EQUIPO',
  POR_ALUMNO = 'POR_ALUMNO',
}

/** Un campo del cuestionario, tal como lo define el Profesor. */
@Schema({ _id: false })
export class CampoFormulario {
  @Prop({ required: true }) id: string;
  @Prop({ required: true, enum: TipoCampo }) tipo: TipoCampo;
  @Prop({ required: true }) etiqueta: string;
  @Prop({ default: false }) requerido: boolean;

  /** Solo para opcion_multiple. */
  @Prop({ type: [String], default: [] }) opciones: string[];
}

/** Un renglón del catálogo de componentes que lleva la práctica. */
@Schema({ _id: false })
export class ComponenteRequerido {
  /** Clave del artículo en logistica-service. Texto, no referencia: son bases distintas. */
  @Prop({ required: true }) claveArticulo: string;
  @Prop({ required: true, min: 1 }) cantidad: number;
  @Prop({ required: true, enum: UnidadConsumo }) unidadConsumo: UnidadConsumo;
}

/**
 * La PLANTILLA de una práctica: contenido teórico, cuestionario y componentes.
 * No tiene fecha ni grupo — eso es PracticaProgramada.
 *
 * Es documental porque el formulario lo diseña el Profesor y no tiene forma fija.
 * Guardarlo como JSON evita una tabla EAV en SQL.
 */
@Schema({ collection: 'practicas', timestamps: { createdAt: 'creadaEn', updatedAt: 'actualizadaEn' } })
export class Practica {
  @Prop({ required: true, unique: true }) clave: string;
  @Prop({ required: true }) titulo: string;
  @Prop({ default: '' }) contenidoTeorico: string;

  /** Clave de la materia en escolar-service. Texto, no referencia. */
  @Prop({ required: true, index: true }) materiaClave: string;

  /** Número de empleado del Profesor que la creó. */
  @Prop({ required: true, index: true }) autorEmpleado: string;

  @Prop({ type: [CampoFormulario], default: [] }) formulario: CampoFormulario[];
  @Prop({ type: [ComponenteRequerido], default: [] }) componentes: ComponenteRequerido[];
}

export type PracticaDocument = HydratedDocument<Practica>;
export const PracticaSchema = SchemaFactory.createForClass(Practica);
