# equipos-service · MS-05

Formación de equipos temporales para una práctica programada: creación, código de unión,
alta de integrantes, designación del responsable y cierre.

**Node · Fastify · PostgreSQL `equipos_db` + Redis · puerto 3000**

## Por qué Redis

Los códigos de unión son estado corto y volátil: necesitan expirar solos. Redis lo hace con
un TTL de 2 horas sin que nadie tenga que barrer una tabla. **No es una caché**: es el único
uso de Redis en el sistema.

## Estado

Las **interfaces de dominio están escritas** en `src/dominio/`. Falta:

- [ ] `src/main.ts` y el servidor Fastify
- [ ] Repositorios con `pg`
- [ ] Rutas (ver `docs/03-contratos-http.md`)
- [ ] Validación de JWT contra `JWKS_URL` con `jose`
- [ ] Clientes hacia `escolar-service` (validar inscripción) y `academico-service`
      (modalidad y máximo de integrantes)
- [ ] Publicador de `kit.solicitado` en RabbitMQ
- [ ] `/health` y `/health/ready`
- [ ] Migraciones y `tsconfig.json`

## Lo que no se debe romper

- **Solo se une quien está inscrito en el grupo** de esa práctica. Hay que preguntárselo a
  `escolar-service`: esa relación no viene en el token (D-11).
- **Un alumno no puede estar en dos equipos de la misma práctica programada.** La llave
  única `(practica_programada_id, cuenta)` en `integrante` lo impone en la base; por eso la
  práctica está denormalizada ahí.
- **Equipo** ≠ **grupo**, y **código de unión** ≠ **código de inscripción**. Ver el glosario.
- Solo se solicita el kit con el equipo **cerrado**.
- Una práctica individual es un equipo de un integrante (D-08): no hay flujo aparte.

Ver `../docs/02-modelo-de-datos.md`, `../docs/08-glosario.md` y `../AGENTS.md`.
