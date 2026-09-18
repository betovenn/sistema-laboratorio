# AGENTS.md

Instrucciones para agentes de IA que trabajen en este repositorio.
Léelo completo antes de tu primer cambio. Si algo aquí contradice lo que crees saber
del proyecto, **gana este archivo**; si contradice a `docs/`, gana `docs/`.

---

## 1. Qué es esto

Sistema de gestión de prácticas de laboratorio de electrónica para una facultad.
Cubre el ciclo completo: el profesor diseña y programa una práctica, el laboratorio
calcula cuánto material necesita y entrega los kits, el alumno resuelve el cuestionario
en equipo, y el profesor califica. Lo distintivo es que el sistema administra **material
físico**, no solo información académica.

Arquitectura de **siete microservicios políglotas**, cada uno dueño de su propia base de
datos. Escala objetivo: **40 usuarios concurrentes, un laboratorio**. No es un sistema
grande; no lo diseñes como si lo fuera.

## 2. Mapa del repositorio

```
docs/                 Fuente de verdad del diseño. Léela antes de codificar.
contracts/            Contratos entre servicios (eventos, OpenAPI). Cambio aquí = cambio coordinado.
infra/                Inicialización de PostgreSQL y configuración de despliegue.
docker-compose.yml    Orquestación completa.
iam-service/          MS-01 · Java 21 · Spring Boot · PostgreSQL
escolar-service/      MS-02 · Python · Django REST · PostgreSQL
academico-service/    MS-03 · Node · NestJS · MongoDB
logistica-service/    MS-04 · Go · PostgreSQL
equipos-service/      MS-05 · Node · Fastify · PostgreSQL + Redis
ejecucion-service/    MS-06 · Node · NestJS · PostgreSQL
evaluacion-service/   MS-07 · Python · FastAPI · PostgreSQL
```

Estado: **`iam-service` es el único con proyecto real**. Los otros seis tienen sus clases
de dominio escritas y un README con lo que falta. Ver §8.

## 3. Reglas que no se rompen

Estas invariantes salieron de una revisión de requisitos documentada en `docs/09-decisiones.md`.
Romper una no es un detalle de estilo: rompe el diseño.

1. **Ningún servicio lee la base de otro.** No hay `JOIN` entre servicios. Si necesitas un
   dato ajeno, es una llamada HTTP a su API. PostgreSQL lo impone con roles: un `JOIN`
   entre servicios no falla la revisión de código, falla con error de permisos.

2. **El identificador de persona es el número de cuenta o de empleado**, como cadena de
   texto. Aparece en casi todas las bases y **nunca es llave foránea**: es el identificador
   compartido, y ninguna base puede validarlo contra otra. Es deliberado.

3. **La calificación es individual por alumno.** No existe —ni debe crearse— una entidad de
   "calificación de equipo". La llave única es `(practica_programada_id, cuenta)`. Poner la
   misma nota a todo el equipo es un atajo de captura en la interfaz que escribe N filas.

4. **Solo dos cosas son asíncronas:** `kit.solicitado` y `incidencia.registrada`. Todo lo
   demás es HTTP síncrono. No agregues eventos sin actualizar `docs/04-eventos.md` y
   `contracts/events/`.

5. **El token se valida localmente.** Cada servicio descarga la clave pública de
   `iam-service` al arrancar y la cachea. **Ninguna petición vuelve a llamar a `iam-service`
   para validar.** Es lo que permite que convivan cuatro lenguajes.

6. **La autorización de negocio no va en el token.** El JWT solo dice `sub`, `rol` y `exp`.
   "¿Este profesor imparte este grupo?" se le pregunta a `escolar-service`. Si esa relación
   viviera en el token, cada inscripción obligaría a reemitir credenciales.

7. **El reporte entregado es inmutable** y congela los nombres al momento del envío. Si un
   alumno cambia de nombre después, el reporte viejo no cambia.

8. **Una práctica individual es un equipo de un integrante.** No hay flujo aparte.

## 4. Idioma y nombres

- **Todo en español**: nombres de clases, tablas, columnas, variables, rutas, comentarios,
  mensajes de error, documentación y mensajes de commit.
- Sin acentos ni `ñ` en identificadores de código o base de datos: `calificacion`, `danado`,
  `practica_programada`. Los acentos sí van en texto para humanos.
- Usa el vocabulario de `docs/08-glosario.md` con precisión. **`grupo` y `equipo` no son
  sinónimos** y confundirlos rompe el modelo:
  - **grupo** = sección escolar de ~40 alumnos, dura un semestre.
  - **equipo** = 1 a N alumnos juntos en una práctica, dura una sesión.
  - **código de inscripción** = para entrar a un *grupo*.
  - **código de unión** = para entrar a un *equipo*.

## 5. Convenciones técnicas

**Rutas.** Internamente cada servicio sirve en `/v1/...`. El gateway antepone
`/api/<servicio>` y lo recorta al reenviar. Tu servicio nunca ve el prefijo.

**Errores.** Mismo cuerpo en los siete servicios:
```json
{ "error": "codigo_en_snake_case", "mensaje": "Explicación para humanos.", "detalles": {} }
```
Códigos HTTP: `400` datos inválidos, `401` sin token o inválido, `403` rol o propiedad
insuficiente, `404` no existe, `409` conflicto de estado, `422` regla de negocio violada.

**Bitácora.** JSON a la salida estándar, un objeto por línea, con `request_id`, `servicio`,
`nivel` y `mensaje`.

**Trazabilidad.** El gateway genera `X-Request-Id`. **Propágalo en toda llamada saliente.**
Sin eso, seguir una petición que tocó cuatro servicios es leer siete bitácoras a ojo.

**Salud.** Dos endpoints: `/health` (el proceso vive) y `/health/ready` (además alcanza su
base y tiene la clave pública en caché). El compose usa el segundo.

**Migraciones.** Cada servicio migra **su propia base y ninguna otra**, al arrancar.

## 6. Cómo levantar el sistema

```bash
cp .env.example .env          # y cambia las contraseñas
docker compose up -d          # infraestructura + iam-service
docker compose --profile todos up -d   # los siete (falla en los que son esqueleto)
docker compose logs -f iam-service
docker compose exec postgres psql -U escolar -d escolar_db
```

La API entra por `http://localhost:8080/api/<servicio>/v1/...`.
El panel de Traefik está en `http://localhost:8090` (solo desarrollo).

## 7. Antes de dar un cambio por terminado

- [ ] ¿Rompe alguna regla de §3? Si sí, no lo hagas: propón el cambio de diseño primero.
- [ ] ¿Tocaste una frontera entre servicios? Actualiza `contracts/` **y** `docs/`.
- [ ] ¿Agregaste un campo a una base? Actualiza `docs/02-modelo-de-datos.md`.
- [ ] ¿Agregaste un endpoint? Actualiza `docs/03-contratos-http.md`.
- [ ] ¿El servicio sigue arrancando? `docker compose up -d --build <servicio>`.
- [ ] ¿Los nombres están en español y respetan el glosario?

## 8. Estado real y qué NO asumir

- **Solo `iam-service` tiene proyecto compilable** (Gradle, Spring Boot 4.1.1, Java 21).
  Los demás tienen clases de dominio pero les falta empaquetado, dependencias y arranque.
  Cada uno lo dice en su `README.md`.
- **No hay frontend.** Ningún documento lo describe todavía. El compose reserva el lugar
  pero no existe. Si vas a construirlo, decídelo con el equipo primero.
- **No hay pruebas** más allá del test que generó Spring Initializr.
- **No hay CI.**
- **El rol se autodeclara al registrarse**: hoy cualquiera que use un número de empleado
  entra como Profesor. Es un hueco conocido, documentado en `docs/05-seguridad.md`.
- **Falta el tabulador de penalizaciones.** `evaluacion-service` sabe que debe descontar
  puntos por incidencia, pero no cuántos. Es una decisión de la academia, no técnica.

## 9. Cosas que un agente suele equivocar aquí

- **Crear una tabla de calificación de equipo** porque "es más natural". No. Ver regla 3.
- **Meter la lista de grupos del profesor dentro del JWT** para ahorrarse una llamada. No.
  Ver regla 6.
- **Compartir una base entre dos servicios** porque "total, es el mismo Postgres". Son
  seis bases con seis roles: no te va a dejar, y está bien que no te deje.
- **Traducir nombres al inglés** por costumbre. Todo va en español.
- **Diseñar para escala.** Son 40 usuarios concurrentes. No agregues caché, réplicas de
  lectura, Kubernetes ni colas para todo. `docs/06-infraestructura.md` explica qué se
  descartó a propósito.
- **Inventar un evento nuevo** para resolver un acoplamiento. Casi siempre la respuesta
  correcta es una llamada HTTP síncrona.
