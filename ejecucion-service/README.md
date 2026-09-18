# ejecucion-service · MS-06

Respuestas del cuestionario durante la sesión: guardado de progreso, envío final con
bloqueo de edición para todo el equipo, y generación del reporte inmutable.

**Node · NestJS · PostgreSQL `ejecucion_db` · puerto 3000 · WebSocket en `/ws/ejecucion`**

Es el servicio con más escritura concurrente del sistema. A 40 concurrentes son 2 a 5
escrituras por segundo: nada extraordinario, pero es el único con carga sostenida. Si el
pool se queda corto, súbelo de 5 a 10 antes de pensar en réplicas.

## Estado

Las **interfaces de dominio están escritas** en `src/dominio/`. Falta:

- [ ] `main.ts`, `app.module.ts`, entidades TypeORM a partir de las interfaces
- [ ] Controladores y gateway de WebSocket (ver `docs/03-contratos-http.md`)
- [ ] Guardia de JWT contra `JWKS_URL`
- [ ] Clientes hacia `academico-service` (definición del formulario), `equipos-service`
      (integrantes) e `iam-service` (nombres para congelar en el reporte)
- [ ] `/health` y `/health/ready`
- [ ] Migraciones y `tsconfig.json`

## Lo que no se debe romper

- **El envío bloquea la edición para TODO el equipo**, no solo para quien envió.
- **El reporte es inmutable** y congela nombre y cuenta al momento del envío (D-09).
  No lo regeneres ni lo recalcules después: si un alumno cambia de nombre, el reporte viejo
  no cambia.
- Solo los **integrantes del equipo** pueden capturar en esa sesión.
- Las respuestas se validan contra la **definición del formulario** que da
  `academico-service`, no contra un esquema propio.

Ver `../docs/02-modelo-de-datos.md` y `../AGENTS.md`.
