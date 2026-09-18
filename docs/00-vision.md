# 00 · Visión

## El problema

Un laboratorio de electrónica atiende varios grupos por semana. Cada práctica lleva una
lista distinta de componentes, y el personal tiene que adivinar cuánto material preparar.
Cuando algo se quema o no regresa, no hay forma sistemática de vincularlo con quien lo usó.
Por el otro lado, el profesor diseña la práctica en un documento suelto y recibe los
reportes en papel o por correo.

El sistema une las dos mitades: **lo académico y lo físico**.

## Actores

| Actor | Qué hace |
|---|---|
| **Profesor** | Captura su materia y grupo, inscribe alumnos, diseña prácticas, las programa y califica. |
| **Personal de Laboratorio** | Administra el inventario, consulta la demanda anticipada, arma y entrega kits, registra devoluciones e incidencias. |
| **Alumno** | Se inscribe a su grupo, forma equipo, pide el kit, resuelve el cuestionario y pide revisión. |

Son **tres roles**, no cuatro. No existe un administrador escolar: como el sistema no tiene
integración con los sistemas escolares de la facultad, esa captura recae en el Profesor.

## El ciclo completo

1. **Profesor** captura su materia, abre el grupo del periodo e inscribe a sus alumnos
   —a mano, por lista, o compartiendo un código de inscripción.
2. **Profesor** diseña la práctica: contenido teórico, cuestionario con campos de texto,
   numéricos y de opción múltiple, y la lista de componentes con su cantidad.
3. **Profesor** la programa para un grupo en una fecha, y decide si es individual o por
   equipos y de cuántos integrantes.
4. **Laboratorio** ve la agenda por fecha y grupo, y el sistema le dice cuánto material
   necesita: 10 equipos de 4 alumnos se traducen en 10 osciloscopios, 20 puntas y 50
   resistencias. Arma los kits y los marca listos.
5. **Alumno** forma su equipo con un código de unión. Solo entran alumnos inscritos en el
   grupo al que fue programada la práctica.
6. **Un integrante** solicita el kit en nombre de todos.
7. **Laboratorio** entrega el kit registrando al integrante responsable.
8. **El equipo** resuelve el cuestionario guardando avances. Al enviar, se bloquea la
   edición para todos y se genera un reporte único con los nombres y cuentas congelados.
9. **Laboratorio** recibe el kit de vuelta y reporta incidencias si las hubo.
10. **Profesor** califica **a cada integrante por separado**. Las incidencias del kit se
    descuentan de la calificación de todos los del equipo.
11. **Alumno** puede pedir revisión de su propia calificación.

## Dónde está el valor

El cruce entre las dos mitades. Como el profesor ya declaró los componentes de cada
práctica y el sistema sabe cuántos alumnos hay inscritos en el grupo, **el laboratorio deja
de adivinar la demanda**. Y como la entrega y la devolución quedan atadas a un equipo, una
incidencia se convierte en una penalización trazable.

## Escala

**40 usuarios concurrentes: un grupo completo trabajando en una sesión.** Esta cifra manda
sobre todas las decisiones de infraestructura. No es un sistema grande y no debe diseñarse
como si lo fuera.

## Fuera de alcance

- Integración con los sistemas escolares de la facultad.
- Gestión de horarios, aulas o cargas docentes más allá de lo que una práctica necesita.
- Compras, proveedores o presupuesto del laboratorio.
- Frontend: todavía no está descrito en ningún documento.
