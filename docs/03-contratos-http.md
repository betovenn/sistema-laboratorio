# 03 · Contratos HTTP

Internamente cada servicio sirve en `/v1/...`. El gateway antepone `/api/<servicio>` y lo
recorta al reenviar, así que **el servicio nunca ve el prefijo**.

| Prefijo público | Servicio | Puerto interno |
|---|---|---|
| `/api/auth/*` | `iam-service` | 8080 |
| `/api/escolar/*` | `escolar-service` | 8000 |
| `/api/academico/*` | `academico-service` | 3000 |
| `/api/logistica/*` | `logistica-service` | 8080 |
| `/api/equipos/*` | `equipos-service` | 3000 |
| `/api/ejecucion/*` · `/ws/ejecucion` | `ejecucion-service` | 3000 |
| `/api/evaluacion/*` | `evaluacion-service` | 8000 |

Todos exponen además `GET /health` y `GET /health/ready`.

---

## MS-01 · `iam-service`

| Método y ruta | Quién | Para qué |
|---|---|---|
| `POST /v1/registro` | Público | Crear cuenta declarando número de cuenta o empleado (CU-14). |
| `POST /v1/login` | Público | Credenciales → token de acceso y de refresco. |
| `POST /v1/refrescar` | Público | Renovar el token de acceso. |
| `GET /v1/yo` | Autenticado | Datos del usuario en sesión. |
| `GET /v1/usuarios?cuentas=a,b,c` | Servicios | Resolver nombres en lote. Lo usa MS-06 al congelar el reporte. |
| `GET /v1/.well-known/jwks.json` | Servicios | Clave pública. **Se consulta una vez al arrancar, no por petición.** |

## MS-02 · `escolar-service`

| Método y ruta | Quién | Para qué |
|---|---|---|
| `POST /v1/periodos` | Profesor | Registrar un periodo escolar. |
| `POST /v1/materias` | Profesor | Alta en el catálogo compartido (clave única global). |
| `GET /v1/materias` | Profesor | Buscar antes de crear, para no duplicar. |
| `POST /v1/grupos` | Profesor | Abrir grupo. El creador queda como TITULAR. |
| `POST /v1/grupos/{id}/docentes` | Titular | Agregar un adjunto. |
| `POST /v1/grupos/{id}/inscripciones` | Titular/Adjunto | Inscribir por cuenta o cargar lista. |
| `DELETE /v1/grupos/{id}/inscripciones/{cuenta}` | Titular/Adjunto | Dar de baja. |
| `POST /v1/grupos/{id}/codigo` | Titular | Regenerar el código de inscripción. |
| `POST /v1/inscripciones/por-codigo` | Alumno | Agregarse a un grupo con el código (CU-13). |
| `GET /v1/grupos/{id}` | MS-03, MS-04 | Materia, periodo, horario. |
| `GET /v1/grupos/{id}/alumnos` | MS-04, MS-07 | Roster: demanda y acta. |
| `GET /v1/grupos/{id}/alumnos/{cuenta}` | MS-05 | ¿Pertenece? `200` / `404`. |
| `GET /v1/grupos/{id}/docentes/{empleado}` | MS-03 | ¿Lo imparte? `200` / `404`. |
| `GET /v1/profesores/{empleado}/grupos` | MS-03 | Menú "mis grupos". |
| `GET /v1/alumnos/{cuenta}/grupos` | MS-03 | Filtrar las prácticas del alumno. |

## MS-03 · `academico-service`

| Método y ruta | Quién | Para qué |
|---|---|---|
| `POST · PUT · DELETE /v1/practicas[/{id}]` | Profesor | Diseñar la práctica (CU-01). |
| `GET /v1/practicas/{id}` | Profesor, MS-06 | Contenido y definición del formulario. |
| `POST /v1/programaciones` | Profesor | Programar contra grupo y fecha (CU-02). Valida con MS-02. |
| `GET /v1/programaciones?grupo=&desde=&hasta=` | MS-04, Alumno | Agenda por grupo y rango. |
| `GET /v1/programaciones/{id}` | MS-04, MS-05, MS-06 | Modalidad, máximo de integrantes, componentes. |

## MS-04 · `logistica-service`

| Método y ruta | Quién | Para qué |
|---|---|---|
| `POST · PUT · DELETE /v1/articulos[/{id}]` | Laboratorio | Inventario (CU-04). |
| `POST /v1/articulos/{id}/ajuste` | Laboratorio | Ajustar existencias con motivo. |
| `GET /v1/demanda/{programacion_id}` | Laboratorio | Cuánto material hace falta (CU-03). |
| `GET /v1/solicitudes?fecha=&grupo=` | Laboratorio | Fila de atención. |
| `POST /v1/solicitudes/{id}/preparar` | Laboratorio | Marcar kit listo para entrega. |
| `POST /v1/solicitudes/{id}/entregar` | Laboratorio | Entregar registrando al responsable (CU-05). |
| `POST /v1/solicitudes/{id}/devolver` | Laboratorio | Devolución + incidencias. Publica evento. |

## MS-05 · `equipos-service`

| Método y ruta | Quién | Para qué |
|---|---|---|
| `POST /v1/equipos` | Alumno | Crear equipo para una programación (CU-08). |
| `POST /v1/equipos/unirse` | Alumno | Unirse con el código. Valida inscripción contra MS-02. |
| `POST /v1/equipos/{id}/integrantes` | Alumno | Agregar por número de cuenta. |
| `POST /v1/equipos/{id}/cerrar` | Responsable | Cerrar el equipo. |
| `POST /v1/equipos/{id}/solicitar-kit` | Responsable | Publica `kit.solicitado` (CU-06). |
| `GET /v1/equipos/{id}` | MS-04, MS-06, MS-07 | Integrantes y responsable. |

## MS-06 · `ejecucion-service`

| Método y ruta | Quién | Para qué |
|---|---|---|
| `POST /v1/sesiones` | Alumno | Abrir la sesión de un equipo. |
| `PATCH /v1/sesiones/{id}/respuestas` | Alumno | Guardar progreso. |
| `POST /v1/sesiones/{id}/enviar` | Alumno | Envío final: bloquea y genera el reporte (CU-07). |
| `GET /v1/reportes/{id}` | Profesor, MS-07 | Reporte inmutable. |
| `WS /ws/ejecucion` | Alumno | Ver en vivo lo que capturan los compañeros. |

## MS-07 · `evaluacion-service`

| Método y ruta | Quién | Para qué |
|---|---|---|
| `GET /v1/por-calificar?grupo=` | Profesor | Reportes entregados pendientes. |
| `PUT /v1/calificaciones` | Profesor | Calificar. **Recibe un arreglo con una nota por cuenta** (CU-09). |
| `GET /v1/calificaciones?grupo=&programacion=` | Profesor | Acta. |
| `GET /v1/alumnos/{cuenta}/calificaciones` | Alumno | Sus propias notas. |
| `POST /v1/calificaciones/{id}/revision` | Alumno | Pedir revisión de su nota (CU-10). |
| `POST /v1/revisiones/{id}/resolver` | Profesor | Resolver, con nota corregida si aplica. |

**`PUT /v1/calificaciones` nunca recibe una sola nota para el equipo.** El cuerpo es
`{ "programacion_id": 882, "equipo_id": 412, "notas": [{ "cuenta": "...", "nota": 9.5 }, ...] }`.

---

## Forma de los errores

Los siete servicios responden igual:

```json
{ "error": "codigo_en_snake_case",
  "mensaje": "Explicación para humanos.",
  "detalles": {} }
```

| Código | Cuándo |
|---|---|
| `400` | Datos mal formados. |
| `401` | Sin token, token inválido o expirado. |
| `403` | Rol insuficiente, o el recurso es de otro. |
| `404` | No existe. |
| `409` | Conflicto de estado (equipo cerrado, práctica ya enviada). |
| `422` | Regla de negocio violada (alumno no inscrito, equipo lleno). |
