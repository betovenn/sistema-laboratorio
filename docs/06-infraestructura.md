# 06 · Infraestructura

Todo vive en **un solo anfitrión** levantado con `docker compose`.

## Redes

| Red | Quién | Para qué |
|---|---|---|
| `borde` | Solo el gateway | Lo que el anfitrión alcanza. |
| `interna` | Servicios y almacenes | `internal: true`: **sin salida a Internet**. |

**Un solo puerto publicado**: el del gateway. Un servicio que no esté en `borde` es
inalcanzable desde fuera aunque alguien se equivoque en la configuración del gateway.

## Contenedores

| Contenedor | Imagen | Puerto interno | Almacén | RAM |
|---|---|---|---|---|
| `gateway` | `traefik:v3.3` | 80 (publicado en 8080) | — | 64 MB |
| `iam-service` | `eclipse-temurin:21-jre` | 8080 | pg `iam_db` | 512 MB |
| `escolar-service` | `python:3.12-slim` | 8000 | pg `escolar_db` | 256 MB |
| `academico-service` | `node:22-alpine` | 3000 | mongo `academico` | 256 MB |
| `logistica-service` | distroless (Go) | 8080 | pg `logistica_db` | 64 MB |
| `equipos-service` | `node:22-alpine` | 3000 | pg `equipos_db` + redis | 256 MB |
| `ejecucion-service` | `node:22-alpine` | 3000 | pg `ejecucion_db` | 384 MB |
| `evaluacion-service` | `python:3.12-slim` | 8000 | pg `evaluacion_db` | 256 MB |
| `postgres` | `postgres:17-alpine` | 5432 | volumen `pgdata` | 512 MB |
| `mongo` | `mongo:7` | 27017 | volumen `mongodata` | 512 MB |
| `redis` | `redis:7-alpine` | 6379 | efímero | 64 MB |
| `rabbitmq` | `rabbitmq:3-management-alpine` | 5672 | volumen `mqdata` | 256 MB |

Suman **≈3.4 GB**. Un anfitrión de **8 GB y 4 vCPU** deja margen cómodo. Con 4 GB arranca,
pero sin espacio para nada más. Disco: **20 GB** cubren años —un semestre de respuestas y
calificaciones son megabytes.

## Un PostgreSQL con seis bases, no seis PostgreSQL

A 40 concurrentes, seis instancias gastarían cerca de 3 GB de RAM en procesos ociosos para
servir un tráfico que una sola atiende sin sudar.

Lo que importa de "una base por servicio" es que **nadie pueda hacer un `JOIN` contra las
tablas de otro**, y eso se garantiza con seis bases y seis roles con permiso únicamente
sobre la suya (`infra/postgres/init.sh`). Un `JOIN` entre servicios no falla la revisión de
código: falla con error de permisos.

El aislamiento lógico queda intacto. Si mañana `logistica-service` necesita su propio
servidor, es un `pg_dump` y tres líneas de compose. Lo que no se podría deshacer nunca es
haber dejado que dos servicios compartan tablas, y aquí es imposible.

## Perfiles

Los servicios que todavía son esqueleto están detrás de un perfil, para que el `up` por
omisión siga funcionando:

```bash
docker compose up -d                     # infraestructura + iam-service
docker compose --profile escolar up -d   # + escolar-service
docker compose --profile todos up -d     # los siete
```

Quita el `profiles:` de un servicio cuando su directorio ya compile.

## Dimensionamiento

**Conexiones.** 40 usuarios simultáneos **no son 40 conexiones**. Pool de **5 por servicio**:
seis servicios contra PostgreSQL dan 30, muy por debajo del `max_connections=100` por
omisión. El error caro es el contrario: dejar el pool en el valor por omisión de algunos ORM
—10 o 20 por proceso— y agotar el servidor con seis servicios ociosos.

`ejecucion-service` es el único con escritura sostenida. Si se queda corto, súbele el pool a
10 antes de pensar en réplicas.

**Réplicas: una por servicio.** No hay nada que balancear a esta escala, y meter réplicas
obligaría a resolver sesiones pegajosas en el WebSocket de MS-06 sin ninguna ganancia.

## Lo que no hace falta

- **Sin caché de aplicación.** Redis está solo para códigos de unión con TTL. Meter caché
  ahora es agregar invalidación —y sus errores— sin un problema de desempeño que resolver.
- **Sin Kubernetes.** Un compose cubre todo el semestre.
- **Sin réplicas de lectura ni malla de servicios.** Resuelven problemas que aparecen dos
  órdenes de magnitud más arriba.

## Operación

**Salud.** `/health` (el proceso vive) y `/health/ready` (además alcanza su base y tiene la
clave pública). El compose usa el segundo.

**Migraciones** al arrancar, antes de aceptar tráfico. Cada servicio migra su propia base.

**Bitácoras** en JSON a la salida estándar. A esta escala `docker compose logs` alcanza; no
hace falta un recolector.

**Respaldo nocturno**: `pg_dump` de las seis bases y `mongodump`, a un directorio fuera de
los volúmenes. **Es el único punto donde una falla es irrecuperable**: las calificaciones
son expediente académico.

```bash
docker compose exec postgres pg_dumpall -U postgres > respaldo-$(date +%F).sql
docker compose exec mongo mongodump --archive > mongo-$(date +%F).archive
```
