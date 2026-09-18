# 04 · Eventos

**Solo dos cosas cruzan frontera sin que nadie espere respuesta.** Todo lo demás es una
llamada HTTP síncrona. Un bus para todo sería tan malo como no tenerlo: convierte cada
consulta en un problema de depuración distribuida.

Transporte: **RabbitMQ**, exchange `laboratorio` de tipo `topic`, colas durables.

| Evento | Publica | Consume | Cola | Por qué asíncrono |
|---|---|---|---|---|
| `kit.solicitado` | `equipos-service` | `logistica-service` | `logistica.kits` | El equipo no debe esperar a que el laboratorio confirme. Si MS-04 está caído, la solicitud se encola. |
| `incidencia.registrada` | `logistica-service` | `evaluacion-service` | `evaluacion.incidencias` | El mostrador no puede bloquearse porque el servicio de calificaciones tarde. |

Los esquemas formales están en `contracts/events/`.

## `kit.solicitado`

```json
{
  "evento_id": "9f2c1e4a-77b1-4a0e-9f3d-2b8c5a1d0e77",
  "tipo": "kit.solicitado",
  "version": 1,
  "ocurrido_en": "2026-09-13T09:14:22Z",
  "equipo_id": 412,
  "practica_programada_id": 882,
  "grupo_id": 31,
  "cuenta_responsable": "318045772",
  "integrantes": 4
}
```

MS-04 al recibirlo crea una `solicitud_kit` en estado `SOLICITADO` y la pone en la fila de
atención del laboratorio.

## `incidencia.registrada`

```json
{
  "evento_id": "1a77b3c9-0d21-4f88-b6aa-77e1c4d90b33",
  "tipo": "incidencia.registrada",
  "version": 1,
  "ocurrido_en": "2026-09-13T12:41:05Z",
  "equipo_id": 412,
  "practica_programada_id": 882,
  "incidencias": [
    { "incidencia_id": 77, "clave_articulo": "RES-220", "tipo": "QUEMADO", "cantidad": 3 }
  ]
}
```

MS-07 al recibirlo aplica la penalización a **la calificación individual de cada integrante**
del equipo, conforme al tabulador. Si todavía no hay calificación, guarda la penalización
pendiente y la aplica cuando el profesor califique.

## Tres reglas que evitan los problemas clásicos

**Confirmación manual.** La cola no borra el mensaje hasta que el consumidor terminó de
procesarlo. Con confirmación automática, un servicio que muere a medio procesar pierde el
evento en silencio.

**Cola de mensajes muertos.** Lo que falle tres veces se va a `<cola>.muertos` en vez de
reintentarse para siempre. Un mensaje envenenado puede bloquear la cola entera.

**Idempotencia por `evento_id`.** RabbitMQ garantiza entrega **al menos una vez**, no
exactamente una. Cada consumidor guarda los `evento_id` ya procesados y descarta repetidos.
Sin esto, una reentrega duplica una penalización sobre la calificación de un alumno.

## Antes de agregar un evento

Casi siempre la respuesta correcta es una llamada HTTP síncrona. Un evento se justifica solo
si se cumplen las tres:

1. Quien publica **no necesita** el resultado para continuar.
2. Que el consumidor esté caído **no debe** impedir la operación.
3. El hecho ya ocurrió y es irreversible (no es una pregunta, es un aviso).

Si agregas uno: actualiza este documento, agrega su esquema en `contracts/events/`, y
declara la cola y su cola de muertos.
