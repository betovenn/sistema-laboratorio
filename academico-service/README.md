# academico-service · MS-03

Plantillas de prácticas y su programación: contenido teórico, cuestionario configurable,
catálogo de componentes, y la asignación a un grupo y una fecha.

**Node · NestJS · MongoDB `academico` · puerto 3000**

## Por qué MongoDB

El formulario lo diseña el Profesor con campos heterogéneos —texto abierto, numérico,
opción múltiple— y no tiene forma fija. Guardar ese esquema como documento JSON evita una
tabla EAV en SQL, que es donde este tipo de sistemas se vuelve inmanejable.

## Estado

Los **esquemas de dominio están escritos** en `src/practicas/esquemas/`. Falta:

- [ ] `main.ts`, `app.module.ts` y el módulo `practicas`
- [ ] Controladores y servicios (ver `docs/03-contratos-http.md`)
- [ ] Guardia de JWT validando contra `JWKS_URL` con `jose`
- [ ] Cliente HTTP hacia `escolar-service` para validar grupo y profesor titular en CU-02
- [ ] `/health` y `/health/ready`
- [ ] `tsconfig.json` y `nest-cli.json`

## Lo que no se debe romper

- **`unidadConsumo` es obligatorio en cada componente.** Sin él, `logistica-service` no
  puede calcular la demanda: es la funcionalidad de más valor del sistema (D-06).
- Al programar hay que **validar contra `escolar-service`** que el profesor esté asignado
  al grupo. No confiar en el token: esa relación no va ahí (D-11).
- `materiaClave` y `grupoId` son **datos de otro servicio**, guardados como valor. No hay
  referencia ni integridad entre bases, y es deliberado.
- En modalidad `INDIVIDUAL`, `maxIntegrantes` vale 1: una práctica individual es un equipo
  de un integrante (D-08).

Ver `../docs/02-modelo-de-datos.md` y `../AGENTS.md`.
