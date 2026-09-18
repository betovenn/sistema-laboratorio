# escolar-service · MS-02

Dueño de la **estructura escolar**: periodos, materias, grupos, asignación de profesores e
inscripción de alumnos. Es el servicio del que más gente depende: lo consultan
`academico-service`, `logistica-service`, `equipos-service` y `evaluacion-service`.

**Python · Django REST Framework · PostgreSQL `escolar_db` · puerto 8000**

## Estado

Las **clases de dominio están escritas** en `escolar/models.py`. Falta:

- [ ] `django-admin startproject config .` para generar `config/` y `manage.py`
- [ ] Registrar `escolar` en `INSTALLED_APPS` y configurar `DATABASES` desde `DATABASE_URL`
- [ ] Serializadores y vistas (ver `docs/03-contratos-http.md`)
- [ ] Middleware de validación de JWT contra `JWKS_URL`
- [ ] `/health` y `/health/ready`
- [ ] Migraciones: `python manage.py makemigrations escolar`

## Lo que no se debe romper

- **La clave de materia es única globalmente.** El catálogo es compartido (D-12).
- `cuenta` y `empleado` son **texto, no llaves foráneas**. `iam-service` es dueño de las
  personas; esta base no puede validarlos contra él.
- Solo los profesores **asignados** a un grupo pueden modificarlo o tocar su lista.
- Un alumno se une a un grupo de dos formas: el profesor lo inscribe, o usa el
  **código de inscripción**. No confundirlo con el **código de unión**, que es de equipos.

Ver `../docs/02-modelo-de-datos.md` y `../AGENTS.md`.
