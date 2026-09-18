"""
Modelo de dominio de evaluacion-service (MS-07).

Aquí queda materializada la decisión más importante del sistema: LA CALIFICACIÓN ES
INDIVIDUAL. La llave única es (practica_programada_id, cuenta) y NO EXISTE ninguna tabla
de calificación de equipo, de modo que el modelo no puede desviarse hacia allá por
accidente (docs/09-decisiones.md, D-04).

Poner la misma nota a todo el equipo es un atajo de captura en la interfaz que escribe N
filas individuales, no una nota compartida.
"""

import enum
from datetime import datetime

from sqlalchemy import (
    CheckConstraint,
    Column,
    DateTime,
    Enum,
    ForeignKey,
    Integer,
    Numeric,
    String,
    Text,
    UniqueConstraint,
    func,
)
from sqlalchemy.orm import DeclarativeBase, relationship


class Base(DeclarativeBase):
    pass


class EstadoRevision(str, enum.Enum):
    ABIERTA = "ABIERTA"
    RESUELTA = "RESUELTA"


class Calificacion(Base):
    """
    Nota de UN alumno en UNA práctica programada.

    `equipo_id` se guarda como contexto —sirve para saber con quién trabajó y para aplicar
    la penalización de una incidencia a todo el equipo—, pero NO agrupa la calificación:
    cada integrante tiene su propia fila y su propia nota.

    `cuenta` es el número de cuenta del alumno: identificador de iam-service, no llave
    foránea. Ninguna base puede validarlo contra otra, y es deliberado.
    """

    __tablename__ = "calificacion"

    id = Column(Integer, primary_key=True)
    practica_programada_id = Column(Integer, nullable=False, index=True)
    equipo_id = Column(Integer, nullable=False, index=True)
    cuenta = Column(String(15), nullable=False, index=True)

    nota = Column(Numeric(5, 2), nullable=False)
    penalizacion = Column(Numeric(5, 2), nullable=False, server_default="0")

    calificado_por = Column(String(15), nullable=False)  # número de empleado
    calificado_en = Column(DateTime(timezone=True), nullable=False, server_default=func.now())

    penalizaciones = relationship(
        "PenalizacionAplicada", back_populates="calificacion", cascade="all, delete-orphan"
    )
    revisiones = relationship(
        "Revision", back_populates="calificacion", cascade="all, delete-orphan"
    )

    __table_args__ = (
        # Una sola calificación por alumno y práctica. Esta es la restricción que impide
        # que alguien reintroduzca una nota de equipo.
        UniqueConstraint("practica_programada_id", "cuenta", name="calificacion_unica_por_alumno"),
        CheckConstraint("nota >= 0 AND nota <= 10", name="nota_en_rango"),
        CheckConstraint("penalizacion >= 0", name="penalizacion_no_negativa"),
    )

    @property
    def nota_final(self):
        """Lo que realmente cuenta. Nunca baja de cero por una penalización grande."""
        return max(float(self.nota) - float(self.penalizacion), 0.0)

    def aplicar_penalizacion(self, puntos: float) -> None:
        self.penalizacion = float(self.penalizacion) + puntos


class PenalizacionAplicada(Base):
    """
    Descuento concreto sobre una calificación, originado por una incidencia del kit.

    Llega por el evento `incidencia.registrada` que publica logistica-service, y se aplica
    a la calificación individual de CADA integrante del equipo.

    `incidencia_id` es el identificador de la incidencia en logistica-service. Se guarda
    para que el consumidor sea idempotente: RabbitMQ entrega al menos una vez, y sin este
    control una reentrega duplicaría el descuento sobre un alumno.
    """

    __tablename__ = "penalizacion_aplicada"

    id = Column(Integer, primary_key=True)
    calificacion_id = Column(Integer, ForeignKey("calificacion.id"), nullable=False)
    incidencia_id = Column(Integer, nullable=False)
    tipo = Column(String(20), nullable=False)  # QUEMADO | PERDIDO | DANADO
    puntos = Column(Numeric(5, 2), nullable=False)
    aplicada_en = Column(DateTime(timezone=True), nullable=False, server_default=func.now())

    calificacion = relationship("Calificacion", back_populates="penalizaciones")

    __table_args__ = (
        UniqueConstraint(
            "calificacion_id", "incidencia_id", name="penalizacion_unica_por_incidencia"
        ),
    )


class Revision(Base):
    """
    Solicitud de un alumno para que se revise SU PROPIA calificación.

    No afecta la del resto del equipo: como cada quien tiene su nota, la revisión es
    naturalmente individual.
    """

    __tablename__ = "revision"

    id = Column(Integer, primary_key=True)
    calificacion_id = Column(Integer, ForeignKey("calificacion.id"), nullable=False)

    solicitada_por = Column(String(15), nullable=False)  # debe ser la cuenta del dueño
    motivo = Column(Text, nullable=False)
    solicitada_en = Column(DateTime(timezone=True), nullable=False, server_default=func.now())

    estado = Column(
        Enum(EstadoRevision, name="estado_revision"),
        nullable=False,
        default=EstadoRevision.ABIERTA,
    )
    resolucion = Column(Text)
    nota_corregida = Column(Numeric(5, 2))
    resuelta_por = Column(String(15))  # número de empleado
    resuelta_en = Column(DateTime(timezone=True))

    calificacion = relationship("Calificacion", back_populates="revisiones")

    def resolver(self, empleado: str, resolucion: str, nota_corregida=None) -> None:
        self.estado = EstadoRevision.RESUELTA
        self.resolucion = resolucion
        self.nota_corregida = nota_corregida
        self.resuelta_por = empleado
        self.resuelta_en = datetime.now()
        if nota_corregida is not None:
            self.calificacion.nota = nota_corregida


# ─────────────────────────────────────────────────────────────────────────────────────
# PENDIENTE: el tabulador de penalizaciones.
#
# Cuántos puntos descuenta cada tipo de incidencia es una decisión de la academia, no
# técnica, y todavía no está definida. Cuando se defina, modelarla como DATOS en una tabla
# configurable, no como constantes en el código: si se programa a mano, cada cambio de
# criterio de la academia se convierte en un despliegue.
#
# Forma sugerida:
#   tabulador(id, tipo_incidencia, clave_articulo NULL, puntos, vigente_desde)
# con clave_articulo NULL como regla general y filas específicas para material caro.
# ─────────────────────────────────────────────────────────────────────────────────────
