# 07 · Convenciones

## Idioma

**Todo en español**: clases, tablas, columnas, variables, rutas, comentarios, mensajes de
error, documentación y mensajes de commit.

**Sin acentos ni `ñ` en identificadores** de código o base de datos: `calificacion`,
`danado`, `practica_programada`, `codigo_union`. Los acentos sí van en el texto para humanos.

Usa el vocabulario de [`08-glosario.md`](08-glosario.md) con precisión.

## Nombres

| Elemento | Estilo | Ejemplo |
|---|---|---|
| Tablas y columnas | `snake_case`, tabla en singular | `practica_programada`, `cuenta_responsable` |
| Rutas HTTP | `kebab-case`, recurso en plural | `/v1/practicas-programadas` |
| JSON | `snake_case` | `{"practica_programada_id": 882}` |
| Clases Java/TS/Python | `PascalCase` | `SolicitudKit` |
| Structs y campos Go | `PascalCase` exportado | `SolicitudKit.CuentaResponsable` |
| Variables y funciones | según el lenguaje | `camelCase` en Java/TS, `snake_case` en Python |
| Directorio de servicio | `<nombre>-service` | `escolar-service` |
| Paquete Java | `com.uaemex.laboratorio.<servicio>` | `com.uaemex.laboratorio.iam` |
| Contenedor | `lab_<nombre>` | `lab_escolar` |

**El identificador de persona se llama siempre igual** según de quién se trate: `cuenta`
para alumnos, `empleado` para profesores y laboratorio. Nunca `usuario_id`, `matricula`,
`no_cuenta` ni `id_alumno`.

## Versionado de la API

Versión en la ruta desde el primer día: `/v1/`. Cuesta nada ahora y es imposible después.

Un cambio **compatible** (agregar un campo opcional, un endpoint nuevo) no sube la versión.
Un cambio **incompatible** (quitar o renombrar un campo, cambiar un tipo, volver requerido
algo opcional) exige `/v2/` conviviendo con `/v1/` hasta que todos los consumidores migren.

Antes de un cambio incompatible: mira en [`01-arquitectura.md`](01-arquitectura.md) quién
consume ese endpoint. Nunca es solo tu servicio.

## Errores

```json
{ "error": "alumno_no_inscrito",
  "mensaje": "El alumno 318045772 no está inscrito en el grupo 31.",
  "detalles": { "cuenta": "318045772", "grupo_id": 31 } }
```

`error` es un código estable en `snake_case` que el frontend puede comparar. `mensaje` es
para humanos y puede cambiar. `detalles` es opcional.

| Código | Cuándo |
|---|---|
| `400` | Datos mal formados: falta un campo, tipo equivocado. |
| `401` | Sin token, inválido o expirado. |
| `403` | Rol insuficiente, o el recurso es de otro. |
| `404` | No existe. |
| `409` | Conflicto de estado: equipo cerrado, práctica ya enviada. |
| `422` | Regla de negocio violada: alumno no inscrito, equipo lleno. |

La diferencia entre `400` y `422`: `400` es "no entiendo lo que me mandaste", `422` es "te
entendí perfecto y no se puede".

## Bitácoras

JSON a la salida estándar, un objeto por línea:

```json
{"ts":"2026-09-13T09:14:22Z","nivel":"info","servicio":"equipos-service",
 "request_id":"7c2a...","mensaje":"equipo creado","equipo_id":412}
```

**Nunca registres** contraseñas, hashes, tokens completos ni el cuerpo de una petición de
login. Un número de cuenta sí se puede registrar; es el identificador de trabajo.

## Trazabilidad

El gateway genera `X-Request-Id`. Cada servicio lo lee, lo incluye en toda línea de bitácora
y **lo propaga en toda llamada saliente** —HTTP y eventos. Sin eso, seguir una petición que
tocó cuatro servicios es leer siete bitácoras a ojo.

## Llamadas entre servicios

- Por **nombre de servicio** en la red interna: `http://escolar-service:8000/v1/...`.
  Nunca por IP ni por `localhost`.
- **Tiempo de espera obligatorio.** 3 segundos para consultas, 10 para escrituras. Una
  llamada sin tiempo de espera cuelga a quien la hace cuando el otro se degrada.
- **Falla claro.** Si MS-02 no responde, devuelve `503` con `error: "escolar_no_disponible"`.
  No inventes un valor por omisión ni sigas como si nada.
- La URL de cada servicio llega por variable de entorno (`ESCOLAR_URL`, `ACADEMICO_URL`…),
  nunca escrita en el código.

## Commits

En español, imperativo, con el servicio al frente:

```
escolar: valida que el profesor imparta el grupo al programar
logistica: separa activos de consumibles en el calculo de demanda
docs: agrega el tabulador de penalizaciones pendiente
```

## Antes de dar algo por terminado

- [ ] ¿Rompe alguna regla de `AGENTS.md` §3?
- [ ] ¿Tocaste una frontera? Actualiza `contracts/` y `docs/`.
- [ ] ¿Campo nuevo en una base? Actualiza `02-modelo-de-datos.md`.
- [ ] ¿Endpoint nuevo? Actualiza `03-contratos-http.md`.
- [ ] ¿Arranca? `docker compose up -d --build <servicio>`
- [ ] ¿Nombres en español y según el glosario?
