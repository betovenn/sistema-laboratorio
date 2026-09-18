# logistica-service · MS-04

Inventario, cálculo de demanda, kits, entregas, devoluciones e incidencias.

**Go · PostgreSQL `logistica_db` · puerto 8080**

## Por qué Go

Es el servicio con **consistencia fuerte obligatoria**: dos empleados en el mostrador no
pueden entregar la misma pieza. El bloqueo de renglón lo hace PostgreSQL
(`SELECT ... FOR UPDATE`), no el lenguaje. Go está aquí porque Logística es el servicio más
aislado —inventario puro, sin formularios ni tiempo real—, es el mejor lugar para meter un
cuarto lenguaje sin arriesgar el resto, y produce un binario que arranca en milisegundos en
la máquina del mostrador.

## Estado

Las **estructuras de dominio están escritas** en `internal/dominio/` y los eventos en
`internal/eventos/`. Falta:

- [ ] `cmd/servidor/main.go`
- [ ] Repositorios con `pgx` y transacciones con bloqueo de renglón
- [ ] Manejadores HTTP (ver `docs/03-contratos-http.md`)
- [ ] Middleware de JWT validando contra `JWKS_URL`
- [ ] Consumidor de `kit.solicitado` y publicador de `incidencia.registrada`
- [ ] `/health` y `/health/ready`
- [ ] Migraciones

## Lo que no se debe romper

- **Activos y consumibles son dos comportamientos distintos** (D-07). Un activo no baja
  existencias al prestarse: cambia de estado y se rastrea por número de serie.
- El **cálculo de demanda** depende de `unidadConsumo`, que declara `academico-service`
  por componente. Ver `CalcularRequerido` en `internal/dominio/articulo.go`.
- **Entregar y devolver deben ser transaccionales con bloqueo.** Es el único punto del
  sistema donde dos operaciones concurrentes pueden corromper el inventario.
- El consumidor de eventos debe ser **idempotente por `evento_id`**: RabbitMQ entrega al
  menos una vez, no exactamente una.

Ver `../docs/02-modelo-de-datos.md`, `../docs/04-eventos.md` y `../AGENTS.md`.
