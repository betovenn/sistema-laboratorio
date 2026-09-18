# 09 · Decisiones de diseño

Registro de las decisiones que dieron forma al sistema, con su porqué. **Antes de cambiar
algo que contradiga una de estas, lee por qué se tomó.**

---

### D-01 · Siete microservicios, no cuatro
**Decisión.** El diseño original tenía cuatro servicios. Se agregó Gestión Escolar y el
cuarto se dividió en tres: Equipos, Ejecución y Evaluación.

**Por qué.** Ningún servicio era dueño de "materia" ni "grupo", y esas palabras aparecían en
cuatro requerimientos y tres casos de uso. El cuarto servicio agrupaba datos con ciclos de
vida incompatibles: los equipos viven una sesión, las respuestas se vuelven inmutables al
enviarse, las calificaciones son expediente permanente. La frontera natural es el envío
final que bloquea la edición.

---

### D-02 · Solicitar un kit es de Logística, no de Equipos
**Decisión.** `equipos-service` publica `kit.solicitado`; `logistica-service` es dueño de la
solicitud, su fila de atención y su estado.

**Por qué.** El equipo origina el aviso, pero un kit es dominio de inventario. Con la
asignación original, Ejecución habría sido dueño de un dato de inventario.

---

### D-03 · No hay rol administrador: la captura escolar es del Profesor
**Decisión.** Sin integración con los sistemas escolares de la facultad, la estructura
escolar se captura dentro del sistema, y esa responsabilidad recae en el Profesor. El
sistema conserva tres roles.

**Por qué.** Se evaluó agregar un cuarto rol (Coordinador) y se descartó: implicaba un CRUD
y tres casos de uso más para un actor que la facultad no tiene asignado.

**Consecuencia.** Inscribir a mano a 40 alumnos no es realista, así que la inscripción tiene
dos caminos: cargar la lista completa, o compartir un código de inscripción para que cada
alumno se agregue solo.

---

### D-04 · La calificación es individual, sin nota de equipo
**Decisión.** Una fila por `(practica_programada_id, cuenta)`. **No existe entidad de
calificación de equipo.**

**Por qué.** El requerimiento original se contradecía: decía a la vez que el profesor puede
"asignar una calificación de manera particular a cada integrante" y que "la calificación
asignada deberá reflejarse automáticamente para cada uno". La segunda mitad era redacción
heredada de un diseño grupal anterior. Se resolvió a favor de la calificación individual pura.

**Consecuencia.** Poner la misma nota a todo el equipo es un **atajo de captura** en la
interfaz que escribe N filas individuales. No se modela como nota compartida.

---

### D-05 · Cada usuario crea su propia cuenta
**Decisión.** Autorregistro declarando número de cuenta o de empleado. Ese identificador es
la llave que liga a la persona con inscripciones, equipos, kits y calificaciones.

**Por qué.** Al quitar el Coordinador y la integración escolar, ningún requerimiento decía
cómo un alumno obtiene su cuenta. El sistema arrancaba vacío sin forma de llenarse.

**Hueco abierto.** El rol queda autodeclarado: quien use un número de empleado entra como
Profesor. Ver [`05-seguridad.md`](05-seguridad.md) para los candados posibles.

---

### D-06 · La unidad de consumo se declara por componente
**Decisión.** Cada renglón del catálogo dice si su cantidad es `POR_EQUIPO` o `POR_ALUMNO`.

**Por qué.** El requerimiento decía "cantidades a cada práctica" sin decir a qué escala. El
ejemplo —10 equipos de 4 alumnos → 10 osciloscopios, 20 puntas, 50 resistencias— solo cuadra
si son por equipo; pero como la práctica también puede ser individual, la misma cifra tenía
que interpretarse distinto según la modalidad. Sin este campo, el cálculo de demanda —la
funcionalidad de más valor— es ambiguo.

---

### D-07 · El inventario distingue activos de consumibles
**Decisión.** Dos tablas: `activo_unidad` con número de serie y estado, `existencia` con
cantidad.

**Por qué.** Un osciloscopio se devuelve y su existencia no baja al prestarse; una
resistencia quemada se descuenta para siempre. El flujo de devolución no puede tratarlos igual.

---

### D-08 · Una práctica individual es un equipo de un integrante
**Decisión.** No hay flujo aparte para el trabajo individual.

**Por qué.** La entrega del kit, el responsable, el reporte y la calificación estaban
descritos solo para equipos. Modelarlo como equipo de uno evita duplicar cuatro flujos y
elimina casos especiales en Logística y Evaluación.

---

### D-09 · El reporte congela los nombres al enviarse
**Decisión.** `reporte.integrantes` guarda `[{cuenta, nombre}]` al momento del envío.

**Por qué.** El reporte debe incluir nombres, pero los nombres viven en `iam-service`. Si
Ejecución los consultara cada vez que se abre el reporte, quedaría acoplado a Identidad para
siempre. Además es un documento histórico: si un alumno cambia de nombre en agosto, el
reporte de marzo no debe cambiar.

---

### D-10 · Solo dos eventos asíncronos
**Decisión.** `kit.solicitado` y `incidencia.registrada`. Todo lo demás es HTTP síncrono.

**Por qué.** Un bus para todo convierte cada consulta en un problema de depuración
distribuida. Estos dos cumplen las tres condiciones: quien publica no necesita el resultado,
que el consumidor esté caído no debe impedir la operación, y el hecho ya ocurrió.

---

### D-11 · Token validado localmente, sin llamada por petición
**Decisión.** RS256 + JWKS. Cada servicio descarga la clave pública al arrancar y la cachea.

**Por qué.** Es lo que hace viable el políglota: cada lenguaje solo necesita una librería
JWT, no un SDK común. Y evita que `iam-service` sea el cuello de botella y el punto único de
falla del sistema entero.

**Corolario.** La autorización de negocio **no** va en el token: si la lista de grupos del
profesor viviera ahí, cada inscripción obligaría a reemitir credenciales.

---

### D-12 · El catálogo de materias es compartido
**Decisión.** `materia.clave` es única en todo el sistema, no por profesor.

**Por qué.** Si cada profesor diera de alta las suyas sin restricción, "Electrónica
Analógica", "Electronica Analogica" y "ELE-0421" acabarían siendo tres materias distintas y
el filtrado por materia dejaría de servir. Solo el **grupo** tiene dueño; la materia es de
todos.

---

### D-13 · Cuatro lenguajes, no seis
**Decisión.** Java, Python, Node y Go.

**Por qué.** Con la escala fijada en 40 concurrentes, Elixir/Phoenix para Ejecución perdió
su justificación: 40 alumnos autoguardando son 2 a 5 escrituras por segundo, que mueve
cualquier runtime. Go se conservó, pero por otra razón de la inicial: el bloqueo de renglón
lo hace PostgreSQL, no el lenguaje; está ahí porque Logística es el servicio más aislado y
el mejor lugar para un cuarto lenguaje.

**Nota.** La infraestructura es idéntica sin importar los lenguajes. Cambiar uno es cambiar
la imagen base y el puerto de un contenedor.

---

### D-14 · Un PostgreSQL con seis bases, no seis instancias
**Decisión.** Un contenedor, seis bases, seis roles sin permiso cruzado.

**Por qué.** Seis instancias gastarían ~3 GB de RAM ociosa para un tráfico que una sola
atiende. Lo que importa de "una base por servicio" —que nadie pueda hacer `JOIN` contra las
tablas de otro— se garantiza con permisos.

**Reversible.** Si un servicio necesita su propio servidor, es un `pg_dump` y tres líneas de
compose. Lo irreversible sería dejar que dos servicios compartan tablas, y eso aquí es
imposible.

---

## Pendientes de decidir

| Qué | Quién decide | Bloquea |
|---|---|---|
| **Tabulador de penalizaciones** por tipo de incidencia | La academia | `evaluacion-service` no sabe cuántos puntos descontar. Modelarlo como datos, no como código: una tabla configurable. Si se programa a mano, cada cambio de criterio es un despliegue. |
| **Frontend**: una SPA con rutas por rol, o tres aplicaciones | El equipo | Recomendación: **una sola SPA**. Comparten sesión y componentes, y el rol ya viene en el token. Tres aplicaciones triplican el trabajo para públicos que se traslapan. |
| **Candado del rol** al registrarse | El equipo | Ver [`05-seguridad.md`](05-seguridad.md). |
| **Retención** del expediente y **ventana** para pedir revisión | La academia | Para arrancar: retención indefinida (el volumen es trivial) y sin ventana, validando solo que exista calificación previa. Ambas se endurecen después sin cambiar el esquema. |
| **Formato de la carga de lista** de alumnos | El equipo | Un CSV de una columna con números de cuenta cubre el caso real. |
