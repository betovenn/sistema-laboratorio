# evaluacion-service · MS-07

Calificación individual de cada integrante, aplicación de penalizaciones por incidencia y
atención de solicitudes de revisión.

**Python · FastAPI · SQLAlchemy · PostgreSQL `evaluacion_db` · puerto 8000**

## La regla central

**La calificación es individual.** La llave única es `(practica_programada_id, cuenta)` y
**no existe ninguna tabla de nota de equipo** (D-04). `equipo_id` se guarda como contexto
—para saber con quién trabajó y para aplicar la penalización a todos—, pero no agrupa la
calificación.

`PUT /v1/calificaciones` **nunca recibe una sola nota para el equipo**. El cuerpo es:

```json
{ "programacion_id": 882, "equipo_id": 412,
  "notas": [ {"cuenta": "318045772", "nota": 9.5}, {"cuenta": "318045773", "nota": 8.0} ] }
```

Poner la misma nota a todos es un atajo de la interfaz que escribe N filas.

## Estado

Los **modelos están escritos** en `app/modelos.py`. Falta:

- [ ] `app/main.py` y los enrutadores (ver `docs/03-contratos-http.md`)
- [ ] Validación de JWT contra `JWKS_URL`
- [ ] Consumidor de `incidencia.registrada` desde RabbitMQ, **idempotente por `evento_id`**
- [ ] Clientes hacia `ejecucion-service` (el reporte), `equipos-service` (integrantes) y
      `escolar-service` (roster del grupo)
- [ ] `/health` y `/health/ready`
- [ ] Migraciones con Alembic

## Pendiente de decisión

**El tabulador de penalizaciones**: cuántos puntos descuenta cada tipo de incidencia. Es
una decisión de la academia. Modelarlo como **datos en una tabla configurable**, no como
constantes: si se programa a mano, cada cambio de criterio es un despliegue. Hay una forma
sugerida al final de `app/modelos.py`.

## Lo que no se debe romper

- **Nunca crear una entidad de calificación de equipo.** Ver D-04.
- El alumno solo ve y solo pide revisión de **su propia** calificación.
- El consumidor de eventos debe ser **idempotente**: sin eso, una reentrega duplica una
  penalización sobre la nota de un alumno. La restricción
  `penalizacion_unica_por_incidencia` lo respalda en la base.
- Una revisión afecta **solo** la calificación de quien la solicita.

Ver `../docs/02-modelo-de-datos.md`, `../docs/04-eventos.md` y `../AGENTS.md`.
