"""
Modelo de dominio de escolar-service (MS-02).

Es dueño de la estructura escolar: periodos, materias, grupos, asignación de profesores e
inscripción de alumnos. La captura la hace el Profesor porque el sistema no tiene
integración con los sistemas escolares de la facultad (docs/09-decisiones.md, D-03).

Nota importante sobre `cuenta` y `empleado`: son cadenas de texto, NO llaves foráneas.
Son el identificador compartido entre servicios; iam-service es dueño de las personas y
esta base no puede validarlos contra él. Es deliberado: así queda la frontera.
"""

from django.db import models


class Periodo(models.Model):
    """Ciclo escolar. Todo lo demás cuelga de aquí."""

    clave = models.CharField(max_length=10, unique=True)  # "2026-1"
    fecha_inicio = models.DateField()
    fecha_fin = models.DateField()

    class Meta:
        db_table = "periodo"
        ordering = ["-clave"]

    def __str__(self):
        return self.clave


class Materia(models.Model):
    """
    Asignatura del plan de estudios.

    El catálogo es COMPARTIDO entre todos los profesores y la clave es única en todo el
    sistema (D-12): si cada quien diera de alta las suyas, "Electrónica Analógica",
    "Electronica Analogica" y "ELE-0421" acabarían siendo tres materias distintas y el
    filtrado por materia dejaría de servir. Solo el grupo tiene dueño; la materia es de todos.
    """

    clave = models.CharField(max_length=20, unique=True)  # "ELE-0421"
    nombre = models.CharField(max_length=160)
    plan_estudios = models.CharField(max_length=40, blank=True)

    class Meta:
        db_table = "materia"
        ordering = ["clave"]

    def __str__(self):
        return f"{self.clave} {self.nombre}"


class Grupo(models.Model):
    """Sección escolar de una materia en un periodo. Dura un semestre, ~40 alumnos."""

    materia = models.ForeignKey(Materia, on_delete=models.PROTECT, related_name="grupos")
    periodo = models.ForeignKey(Periodo, on_delete=models.PROTECT, related_name="grupos")
    clave = models.CharField(max_length=10)  # "04"
    horario = models.CharField(max_length=80, blank=True)
    laboratorio = models.CharField(max_length=60, blank=True)

    # Clave que el Profesor comparte para que un alumno se inscriba solo (CU-13).
    # NO confundir con el código de unión, que es por equipo y vive en equipos-service.
    codigo_inscripcion = models.CharField(max_length=8, unique=True)

    creado_en = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "grupo"
        constraints = [
            models.UniqueConstraint(
                fields=["materia", "periodo", "clave"],
                name="grupo_unico_por_materia_periodo",
            )
        ]

    def __str__(self):
        return f"{self.materia.clave}-{self.clave} ({self.periodo.clave})"

    def imparte(self, empleado: str) -> bool:
        """¿Este profesor está asignado al grupo? Lo consulta academico-service en CU-02."""
        return self.docentes.filter(empleado=empleado).exists()

    def tiene_inscrito(self, cuenta: str) -> bool:
        """¿Este alumno pertenece al grupo? Lo consulta equipos-service al armar equipo."""
        return self.inscripciones.filter(
            cuenta=cuenta, estado=Inscripcion.Estado.ACTIVA
        ).exists()

    @property
    def total_inscritos(self) -> int:
        """Base del cálculo de demanda de material que hace logistica-service."""
        return self.inscripciones.filter(estado=Inscripcion.Estado.ACTIVA).count()


class AsignacionDocente(models.Model):
    """
    Profesor asignado a un grupo. Quien crea el grupo queda como TITULAR y puede sumar
    un ADJUNTO. Solo los asignados pueden modificar el grupo o su lista de inscritos.
    """

    class RolDocente(models.TextChoices):
        TITULAR = "TITULAR", "Titular"
        ADJUNTO = "ADJUNTO", "Adjunto"

    grupo = models.ForeignKey(Grupo, on_delete=models.CASCADE, related_name="docentes")
    empleado = models.CharField(max_length=15)  # identificador, no FK
    rol = models.CharField(max_length=10, choices=RolDocente.choices)
    asignado_en = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "asignacion_docente"
        constraints = [
            models.UniqueConstraint(
                fields=["grupo", "empleado"], name="docente_unico_por_grupo"
            )
        ]

    def __str__(self):
        return f"{self.empleado} {self.rol} en {self.grupo}"


class Inscripcion(models.Model):
    """
    Alumno inscrito en un grupo. Es el roster que alimenta el cálculo de demanda de
    materiales y el registro de calificaciones.
    """

    class Estado(models.TextChoices):
        ACTIVA = "ACTIVA", "Activa"
        BAJA = "BAJA", "Baja"

    grupo = models.ForeignKey(Grupo, on_delete=models.CASCADE, related_name="inscripciones")
    cuenta = models.CharField(max_length=15)  # identificador, no FK
    estado = models.CharField(max_length=10, choices=Estado.choices, default=Estado.ACTIVA)
    inscrito_en = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "inscripcion"
        constraints = [
            models.UniqueConstraint(
                fields=["grupo", "cuenta"], name="inscripcion_unica_por_grupo"
            )
        ]
        indexes = [models.Index(fields=["cuenta"])]

    def __str__(self):
        return f"{self.cuenta} en {self.grupo} ({self.estado})"

    def dar_de_baja(self):
        self.estado = self.Estado.BAJA
        self.save(update_fields=["estado"])
