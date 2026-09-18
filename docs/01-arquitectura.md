# 01 · Arquitectura

Siete microservicios, cada uno dueño de su base. Cuatro lenguajes. Dos eventos asíncronos.

## Los servicios y sus fronteras

### MS-01 · `iam-service` — Identidad y Accesos
**Java 21 · Spring Boot · PostgreSQL `iam_db`**

Registro de usuarios, validación de credenciales, emisión de tokens firmados y catálogo de
usuarios con su rol. Es el único servicio que conoce nombres y contraseñas.

*No incluye* la estructura escolar: en qué grupo está inscrito alguien lo sabe MS-02.

### MS-02 · `escolar-service` — Gestión Escolar
**Python · Django REST · PostgreSQL `escolar_db`**

Periodos, materias, grupos, asignación de profesores e inscripción de alumnos, capturados
por el Profesor. Responde las preguntas que ningún otro servicio puede contestar solo:
si un grupo existe, si un profesor lo imparte, cuántos alumnos hay inscritos y si un alumno
pertenece a él.

Lo consumen MS-03, MS-04, MS-05 y MS-07. Es la pieza de la que más gente depende.

### MS-03 · `academico-service` — Gestión Académica
**Node · NestJS · MongoDB `academico`**

Plantillas de prácticas: contenido teórico, el cuestionario configurable, la lista de
componentes con su unidad de consumo, y la programación contra un grupo y una fecha.

Documental porque **el formulario lo diseña el profesor y no tiene forma fija**. Guardarlo
como JSON evita una tabla EAV en SQL, que es donde estos sistemas se vuelven inmanejables.

### MS-04 · `logistica-service` — Logística e Inventario
**Go · PostgreSQL `logistica_db`**

Existencias distinguiendo activos retornables de consumibles, cálculo de demanda de una
sesión, armado de kits, solicitudes, entregas, devoluciones e incidencias.

Es el servicio con **consistencia fuerte obligatoria**: dos empleados en el mostrador no
pueden entregar la misma pieza. El bloqueo lo hace PostgreSQL, no el lenguaje; Go está aquí
porque es el servicio más aislado y el mejor lugar para un cuarto lenguaje.

### MS-05 · `equipos-service` — Equipos
**Node · Fastify · PostgreSQL `equipos_db` + Redis**

Formación de equipos temporales: creación, código de unión, alta de integrantes, designación
del responsable y cierre. Los códigos expiran solos con un TTL en Redis.

### MS-06 · `ejecucion-service` — Ejecución
**Node · NestJS · PostgreSQL `ejecucion_db`**

Respuestas durante la sesión: guardado de progreso, envío final con bloqueo de edición para
todo el equipo, y generación del reporte inmutable.

### MS-07 · `evaluacion-service` — Evaluación
**Python · FastAPI · PostgreSQL `evaluacion_db`**

Calificación individual de cada integrante, aplicación de penalizaciones por incidencia y
atención de solicitudes de revisión.

## Quién depende de quién

```
escolar-service   ←── academico-service   (¿existe el grupo? ¿lo imparte este profesor?)
                  ←── logistica-service   (¿cuántos alumnos hay inscritos?)
                  ←── equipos-service     (¿este alumno pertenece al grupo?)
                  ←── evaluacion-service  (lista de alumnos del grupo)

academico-service ←── logistica-service   (catálogo de componentes, programación)
                  ←── equipos-service     (modalidad, máximo de integrantes)
                  ←── ejecucion-service   (definición del formulario)

equipos-service   ←── logistica-service   (¿a qué equipo pertenece el kit? ¿quién lo recibe?)
                  ←── ejecucion-service   (integrantes del equipo)
                  ←── evaluacion-service  (integrantes del equipo)

iam-service       ←── ejecucion-service   (nombres que se congelan en el reporte)
                  ←── todos               (clave pública, una vez al arrancar)

ejecucion-service ←── evaluacion-service  (el reporte entregado)
```

Asíncrono, por RabbitMQ:
```
equipos-service   ──kit.solicitado──────────→  logistica-service
logistica-service ──incidencia.registrada───→  evaluacion-service
```

## Por qué estas fronteras

**Por qué existe MS-02.** En el diseño original ningún servicio era dueño de las palabras
"materia" y "grupo", y esas dos aparecían en cuatro requerimientos y tres casos de uso. Tres
servicios tenían que hacer preguntas que nadie podía contestar.

**Por qué MS-05, MS-06 y MS-07 están separados.** Antes eran un solo servicio. Ahí convivían
tres cosas con ciclos de vida distintos: los equipos viven una sesión, las respuestas se
vuelven inmutables al enviarse, y las calificaciones son expediente permanente que alguien
retoca tres semanas después. La frontera natural es **el envío final que bloquea la edición**:
es exactamente la transacción que separa ejecución de evaluación.

**Por qué solicitar un kit es de MS-04 y no de MS-05.** El equipo origina el aviso, pero el
kit, su fila de atención y su estado son datos de inventario. MS-05 publica el evento; MS-04
es dueño de la solicitud.

**Por qué MS-02 no vive dentro de MS-01.** Identidad responde "¿quién eres?": es estable y su
caída tumba el sistema. Gestión Escolar responde "¿en qué estás inscrito?": cambia cada
periodo y se consulta en caliente toda la sesión. Fusionarlos convertiría al servicio más
crítico en el que más se toca.

## Lo que se descartó a propósito

A 40 concurrentes, esto sobra y solo agrega superficie de falla:

- Caché de aplicación. Redis está solo para códigos con TTL, no como caché.
- Réplicas de lectura, balanceo, malla de servicios.
- Kubernetes. Un `docker compose` cubre todo el semestre.
- Un bus para todo. Solo dos hechos cruzan frontera de forma asíncrona.
