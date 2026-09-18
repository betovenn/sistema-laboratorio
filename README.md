# Sistema de Prácticas de Laboratorio

Gestión del ciclo completo de una práctica de electrónica en un laboratorio de facultad:
del diseño de la práctica por el profesor, al cálculo y entrega del material por el
laboratorio, a la resolución en equipo por los alumnos, a la calificación.

Lo que distingue a este sistema de un gestor escolar común es que administra **material
físico**: sabe que una práctica lleva 1 osciloscopio y 5 resistencias por equipo, cuántos
alumnos hay inscritos, y por lo tanto cuánto material tiene que preparar el laboratorio el
martes a las 7:00. Y como la entrega y la devolución quedan atadas a un equipo, una
resistencia quemada se convierte en una penalización sobre la calificación.

## Los siete servicios

| Servicio | Responsabilidad | Stack | Base |
|---|---|---|---|
| `iam-service` | Registro, credenciales, emisión de tokens | Java 21 · Spring Boot | `iam_db` |
| `escolar-service` | Periodos, materias, grupos, inscripciones | Python · Django REST | `escolar_db` |
| `academico-service` | Prácticas, cuestionarios, programación | Node · NestJS | Mongo `academico` |
| `logistica-service` | Inventario, demanda, kits, incidencias | Go | `logistica_db` |
| `equipos-service` | Equipos temporales, códigos de unión | Node · Fastify | `equipos_db` + Redis |
| `ejecucion-service` | Respuestas, envío, reporte | Node · NestJS | `ejecucion_db` |
| `evaluacion-service` | Calificaciones, penalizaciones, revisión | Python · FastAPI | `evaluacion_db` |

Cuatro lenguajes, siete servicios, una base por servicio. El único contrato compartido es
HTTP + JSON y un token firmado, así que cada servicio elige su stack sin comprometer al resto.

## Arrancar

```bash
cp .env.example .env     # cambia todas las contraseñas
docker compose up -d     # infraestructura + iam-service
```

La API queda en `http://localhost:8080/api/<servicio>/v1/...` y el panel de Traefik en
`http://localhost:8090`.

Los servicios que todavía son esqueleto están detrás de perfiles, para que el `up` por
omisión siga funcionando:

```bash
docker compose --profile escolar up -d   # infraestructura + iam + escolar
docker compose --profile todos up -d     # los siete
```

## Documentación

Empieza por donde te toque:

| Documento | Para qué |
|---|---|
| [`docs/00-vision.md`](docs/00-vision.md) | Qué hace el sistema y quién lo usa |
| [`docs/01-arquitectura.md`](docs/01-arquitectura.md) | Por qué siete servicios y dónde van las fronteras |
| [`docs/02-modelo-de-datos.md`](docs/02-modelo-de-datos.md) | El esquema de las siete bases |
| [`docs/03-contratos-http.md`](docs/03-contratos-http.md) | Qué expone cada servicio |
| [`docs/04-eventos.md`](docs/04-eventos.md) | Los dos saltos asíncronos |
| [`docs/05-seguridad.md`](docs/05-seguridad.md) | Tokens, roles y autorización |
| [`docs/06-infraestructura.md`](docs/06-infraestructura.md) | Redes, contenedores, dimensionamiento |
| [`docs/07-convenciones.md`](docs/07-convenciones.md) | Nombres, errores, bitácoras, versionado |
| [`docs/08-glosario.md`](docs/08-glosario.md) | Vocabulario con significado fijo |
| [`docs/09-decisiones.md`](docs/09-decisiones.md) | Las decisiones de diseño y su porqué |

**[`AGENTS.md`](AGENTS.md)** contiene el protocolo para agentes de IA que trabajen aquí:
reglas que no se rompen, convenciones y errores frecuentes.

## Estado

Solo `iam-service` tiene proyecto compilable. Los otros seis tienen sus **clases de dominio**
escritas y un `README.md` que dice exactamente qué falta para levantarlos.

Orden de construcción sugerido:

1. `iam-service` + `escolar-service` + `academico-service` — sin materias ni grupos no hay
   nada que programar.
2. `logistica-service` — el cálculo de demanda es la funcionalidad de más valor.
3. `equipos-service` + `ejecucion-service` — la sesión del alumno de punta a punta.
4. `evaluacion-service` — calificación, penalizaciones y revisión.

Levanta el compose completo desde la primera etapa aunque solo tenga tres servicios:
integrar al final es donde estos proyectos se caen.

## Pendientes conocidos

- **No hay frontend.** Ningún documento lo describe; el compose reserva el lugar.
- **El rol se autodeclara al registrarse.** Ver `docs/05-seguridad.md`.
- **Falta el tabulador de penalizaciones.** Es una decisión de la academia.
- **Falta definir** el periodo de retención del expediente y la ventana para pedir revisión.
