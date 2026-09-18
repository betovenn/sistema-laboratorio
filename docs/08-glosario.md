# 08 · Glosario

Términos con significado fijo en todo el proyecto. **Confundir `grupo` con `equipo`, o los
dos códigos entre sí, rompe el modelo de datos.**

| Término | Significado |
|---|---|
| **Estructura escolar** | Periodos, materias, grupos, asignaciones de profesor e inscripciones. El Profesor la captura dentro del sistema porque no hay integración con los sistemas de la facultad. Vive en `escolar-service`. |
| **Periodo** | Ciclo escolar con clave y fechas: `2026-1`. Todo lo demás cuelga de aquí. |
| **Materia** | Asignatura del plan de estudios, con clave única **en todo el sistema**. El catálogo es compartido: dos profesores de la misma asignatura caen en el mismo registro. |
| **Grupo** | Sección escolar de una materia en un periodo, con horario y laboratorio. Tiene un profesor **titular**, opcionalmente un **adjunto**, y una lista de inscritos. Dura un semestre. ~40 alumnos. |
| **Equipo** | Conjunto temporal de **1 a N alumnos** que resuelven juntos una práctica programada. Dura **una sesión**. Una práctica individual es un equipo de un integrante. Vive en `equipos-service`. |
| **Integrante responsable** | El miembro del equipo que recibe físicamente el kit y contra cuyo número de cuenta se registra la entrega. También llamado *líder*. |
| **Práctica** | La **plantilla** que crea el Profesor: contenido teórico, formulario y catálogo de componentes. **No tiene fecha ni grupo.** Vive en `academico-service`. |
| **Práctica programada** | Una práctica asignada a un grupo, una fecha y una modalidad. Es la unidad contra la que se calcula la demanda, se entregan los kits y se califica. |
| **Modalidad** | `INDIVIDUAL` o `EQUIPO`, con máximo de integrantes. |
| **Unidad de consumo** | Si la cantidad de un componente se consume `POR_EQUIPO` o `POR_ALUMNO`. **Sin este dato el cálculo de demanda es ambiguo.** |
| **Activo** | Artículo retornable con número de serie, cuya existencia **no disminuye** al prestarse: solo cambia de estado. Un osciloscopio. |
| **Consumible** | Artículo que se descuenta de las existencias al consumirse, perderse o dañarse. Una resistencia. |
| **Kit** | Conjunto físico de componentes que se entrega a un equipo para una práctica programada. Tiene estado, responsable y registro de devolución. Vive en `logistica-service`. |
| **Demanda** | Cuánto material necesita el laboratorio para una sesión programada. Se calcula multiplicando las cantidades del catálogo por el número de equipos o de alumnos inscritos, según la unidad de consumo. |
| **Incidencia** | Componente quemado, perdido o dañado, reportado al devolver el kit. Se descuenta de la calificación de **todos** los integrantes. |
| **Código de inscripción** | Clave **de un grupo** que el Profesor comparte para que un alumno se inscriba al **grupo**. Dura lo que el profesor quiera; se puede regenerar. |
| **Código de unión** | Clave **de un equipo**, para una práctica concreta. Expira con un TTL de horas. **No confundir con el de inscripción.** |
| **Reporte** | Documento **inmutable** que se genera al enviar la práctica. Congela las respuestas y los nombres y cuentas de los integrantes tal como estaban al momento del envío. |
| **Calificación** | Nota **individual** de un alumno en una práctica programada. **No existe la calificación de equipo.** Poner la misma nota a todos es un atajo de captura que escribe N filas. |
| **Tabulador de penalizaciones** | Cuántos puntos descuenta cada tipo de incidencia. **Todavía no está definido**: es una decisión de la academia. |
| **Revisión** | Solicitud de un alumno para que se revise **su propia** calificación. No afecta la del resto del equipo. |

## Términos retirados

| Ya no se usa | Usa |
|---|---|
| **NIP** | `código de unión` (equipo) o `código de inscripción` (grupo), según corresponda. |
| **Coordinador** | No existe ese rol. La captura escolar es del **Profesor**. |
| **Calificación de equipo** | `calificación` es siempre individual. |
