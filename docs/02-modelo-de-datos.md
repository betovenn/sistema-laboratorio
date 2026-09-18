# 02 · Modelo de datos

Una base por servicio. **El número de cuenta o de empleado aparece como texto en varias de
ellas y nunca es llave foránea**: es el identificador compartido entre servicios, y ninguna
base puede validarlo contra otra. Esa es exactamente la frontera que se está comprando.

Las clases que materializan este esquema están en cada servicio. Ver §Implementación al final.

---

## `iam_db` — MS-01 Identidad

```sql
usuario(
  id             uuid PRIMARY KEY,
  identificador  varchar(15) UNIQUE NOT NULL,  -- número de cuenta o de empleado
  tipo           enum(CUENTA, EMPLEADO) NOT NULL,
  nombre         varchar(120) NOT NULL,
  correo         varchar(120) UNIQUE,
  password_hash  varchar(255) NOT NULL,        -- bcrypt o argon2, nunca en claro
  rol            enum(ALUMNO, PROFESOR, LABORATORIO) NOT NULL,
  activo         boolean NOT NULL DEFAULT true,
  creado_en      timestamptz NOT NULL
)

refresh_token(
  id          uuid PRIMARY KEY,
  usuario_id  uuid REFERENCES usuario(id),
  token_hash  varchar(255) NOT NULL,
  expira_en   timestamptz NOT NULL,
  revocado    boolean NOT NULL DEFAULT false
)
```

Es la **única base que conoce nombres y contraseñas**. Todas las demás guardan solo el
identificador.

---

## `escolar_db` — MS-02 Gestión Escolar

```sql
periodo(id, clave varchar(10) UNIQUE, fecha_inicio date, fecha_fin date)

materia(id, clave varchar(20) UNIQUE, nombre varchar(160), plan_estudios varchar(40))
-- Catálogo COMPARTIDO: la clave es única en todo el sistema, para que dos profesores
-- de la misma asignatura caigan forzosamente en el mismo registro.

grupo(
  id, materia_id FK, periodo_id FK,
  clave               varchar(10),
  horario             varchar(80),
  laboratorio         varchar(60),
  codigo_inscripcion  varchar(8) UNIQUE,
  UNIQUE(materia_id, periodo_id, clave)
)

asignacion_docente(
  id, grupo_id FK,
  empleado varchar(15),              -- identificador, no FK
  rol      enum(TITULAR, ADJUNTO),
  UNIQUE(grupo_id, empleado)
)

inscripcion(
  id, grupo_id FK,
  cuenta      varchar(15),           -- identificador, no FK
  estado      enum(ACTIVA, BAJA),
  inscrito_en timestamptz,
  UNIQUE(grupo_id, cuenta)
)
```

---

## `academico` — MS-03 Gestión Académica (MongoDB)

```js
practicas {
  _id, clave, titulo, contenido_teorico, materia_clave, autor_empleado, creada_en,
  formulario: [
    { id, tipo: "texto" | "numero" | "opcion_multiple",
      etiqueta, requerido, opciones: [] }
  ],
  componentes: [
    { clave_articulo, cantidad,
      unidad_consumo: "POR_EQUIPO" | "POR_ALUMNO" }
  ]
}

practicas_programadas {
  _id, practica_id, grupo_id, fecha, hora_inicio, hora_fin,
  modalidad: "INDIVIDUAL" | "EQUIPO",
  max_integrantes,
  estado: "PROGRAMADA" | "EN_CURSO" | "CERRADA"
}
```

**`unidad_consumo` es el campo del que depende todo el cálculo de demanda.** Un osciloscopio
se consume por equipo; unos guantes, por alumno. Sin este campo la multiplicación es
ambigua y la funcionalidad de más valor del sistema no se puede implementar.

---

## `logistica_db` — MS-04 Logística

```sql
articulo(id, clave UNIQUE, nombre, tipo enum(ACTIVO, CONSUMIBLE), unidad)

activo_unidad(id, articulo_id FK, num_serie UNIQUE,
              estado enum(DISPONIBLE, PRESTADO, BAJA))
-- Solo para ACTIVO: cada pieza se rastrea individualmente y su existencia
-- NO disminuye al prestarse, solo cambia de estado.

existencia(articulo_id PK FK, cantidad int)
-- Solo para CONSUMIBLE: se descuenta al consumirse, perderse o dañarse.

movimiento(id, articulo_id FK, delta int, motivo, usuario, creado_en)

solicitud_kit(id, practica_programada_id, equipo_id, grupo_id,
              estado enum(SOLICITADO, PREPARADO, ENTREGADO, DEVUELTO), solicitado_en)
kit_renglon(id, solicitud_id FK, articulo_id FK, cantidad, activo_unidad_id FK NULL)
entrega(id, solicitud_id FK, cuenta_responsable, entregado_por, entregado_en)
devolucion(id, solicitud_id FK, recibido_por, recibido_en)
incidencia(id, devolucion_id FK, articulo_id FK,
           tipo enum(QUEMADO, PERDIDO, DANADO), cantidad, nota)
```

Activos y consumibles son dos tablas porque son **dos comportamientos distintos al devolver**.

---

## `equipos_db` — MS-05 Equipos

```sql
equipo(id, practica_programada_id, grupo_id,
       codigo_union varchar(6), estado enum(ABIERTO, CERRADO),
       cuenta_responsable, creado_en)

integrante(id, equipo_id FK,
           practica_programada_id,          -- denormalizado a propósito
           cuenta varchar(15), es_responsable boolean,
           UNIQUE(equipo_id, cuenta),
           UNIQUE(practica_programada_id, cuenta))
```

La segunda llave única es la importante: **un alumno no puede estar en dos equipos de la
misma práctica programada**. La práctica se denormaliza en `integrante` justo para poder
imponer esa regla en la base en vez de en el código.

Redis: `union:{codigo} -> equipo_id`, con TTL de 2 horas. Expira solo.

---

## `ejecucion_db` — MS-06 Ejecución

```sql
sesion(id, practica_programada_id, equipo_id UNIQUE,
       estado enum(EN_PROGRESO, ENVIADA), creada_en)

respuesta(id, sesion_id FK, campo_id varchar(40), valor jsonb,
          actualizado_por, actualizado_en,
          UNIQUE(sesion_id, campo_id))

reporte(id, sesion_id FK UNIQUE, enviado_por, enviado_en,
        integrantes jsonb,   -- [{cuenta, nombre}] congelado al enviar
        respuestas jsonb)    -- copia inmutable
```

Dos naturalezas en una base: `respuesta` se sobrescribe cientos de veces durante la sesión;
`reporte` se escribe una vez y **nunca cambia**. El snapshot de integrantes se congela aquí:
si un alumno cambia de nombre en agosto, el reporte de marzo no cambia.

---

## `evaluacion_db` — MS-07 Evaluación

```sql
calificacion(
  id, practica_programada_id, equipo_id,
  cuenta        varchar(15),
  nota          numeric(5,2),
  penalizacion  numeric(5,2) DEFAULT 0,
  nota_final    numeric(5,2) GENERATED ALWAYS AS (nota - penalizacion) STORED,
  calificado_por, calificado_en,
  UNIQUE(practica_programada_id, cuenta)
)

penalizacion_aplicada(id, calificacion_id FK, incidencia_id,
                      tipo, puntos numeric(5,2), aplicada_en)

revision(id, calificacion_id FK, solicitada_por, motivo, solicitada_en,
         estado enum(ABIERTA, RESUELTA), resolucion,
         nota_corregida numeric(5,2), resuelta_por, resuelta_en)
```

Aquí queda materializada la decisión de **calificación individual**: la llave única es
`(practica_programada_id, cuenta)` y **no existe ninguna tabla de nota de equipo**, así que
el modelo no puede desviarse hacia allá por accidente. Poner la misma nota a todo el equipo
es un atajo de captura en la interfaz que escribe N filas.

---

## Implementación

| Base | Clases |
|---|---|
| `iam_db` | `iam-service/src/main/java/com/uaemex/laboratorio/iam/dominio/` |
| `escolar_db` | `escolar-service/escolar/models.py` |
| `academico` | `academico-service/src/practicas/esquemas/` |
| `logistica_db` | `logistica-service/internal/dominio/` |
| `equipos_db` | `equipos-service/src/dominio/` |
| `ejecucion_db` | `ejecucion-service/src/dominio/` |
| `evaluacion_db` | `evaluacion-service/app/modelos.py` |
