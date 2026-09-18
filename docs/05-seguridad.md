# 05 · Seguridad

## El mecanismo en tres fases

**0 · Al arrancar cada servicio (una vez).** Descarga la clave pública de
`GET http://iam-service:8080/v1/.well-known/jwks.json` y la cachea en memoria.

**1 · Al iniciar sesión (una vez por usuario).** El navegador manda credenciales,
`iam-service` verifica y firma un JWT con **RS256** usando su clave privada.

**2 · En cada petición (siempre).** El gateway verifica la firma y corta lo que no traiga
token. El servicio destino **revalida con su clave en caché** y aplica rol y propiedad.

**Ninguna petición vuelve a llamar a `iam-service`.** Esto es lo que hace viable el
políglota: cada lenguaje solo necesita una librería JWT, no un SDK común. Y si
`iam-service` se cae, las sesiones ya abiertas siguen funcionando.

La doble validación no es redundancia inútil: el gateway corta el tráfico anónimo antes de
que gaste recursos, y el servicio decide lo que solo él sabe. Un servicio sigue siendo
seguro aunque alguien lo llame saltándose el gateway.

## Contenido del token

```json
{
  "iss": "iam-service",
  "sub": "318045772",     // número de cuenta o de empleado
  "rol": "ALUMNO",        // ALUMNO | PROFESOR | LABORATORIO
  "iat": 1789000000,
  "exp": 1789003600,      // 60 minutos
  "jti": "b3f1..."        // para poder revocar
}
```

**Nada de permisos finos dentro del token.** "¿Este profesor imparte este grupo?" se le
pregunta a `escolar-service`, no se lee del JWT. Si esa relación viviera en el token, cada
inscripción o cambio de titular obligaría a reemitir credenciales a medio semestre.

- Acceso: **60 minutos**. Refresco: **8 horas**, que cubre una jornada de laboratorio sin
  obligar a reautenticarse a media práctica.
- Contraseñas con **bcrypt o argon2**. Nunca en claro, nunca con un hash sin sal.
- La clave privada se monta como archivo en `iam-service`, **nunca como variable de entorno
  ni dentro de la imagen**.

## Las dos capas de autorización

**Rol** — la responde el token. "¿Eres Profesor?"

**Propiedad** — la responde `escolar-service`. "¿Eres profesor *de este* grupo?" Esta es la
que importa y la que se olvida. Ejemplos de reglas de propiedad que cada servicio debe
imponer:

| Servicio | Regla |
|---|---|
| MS-02 | Solo los profesores asignados a un grupo pueden modificarlo o tocar su lista. |
| MS-03 | Solo se puede programar contra un grupo que uno imparte. |
| MS-05 | Solo se une al equipo quien está inscrito en el grupo de esa práctica. |
| MS-06 | Solo los integrantes del equipo capturan en esa sesión. |
| MS-07 | El alumno solo ve y solo pide revisión de **su propia** calificación. |

## Hueco conocido: el rol es autodeclarado

Tal como está el requerimiento de autorregistro, **quien se registre con un número de
empleado entra como Profesor** y puede abrir grupos, programar prácticas y calificar.

Para un proyecto de semestre puede ser aceptable; para operar de verdad, no. Tres candados
posibles, de menor a mayor esfuerzo:

1. **Lista blanca de números de empleado** sembrada al desplegar. `iam-service` rechaza el
   registro de un empleado que no esté en ella. Cuesta una tarde.
2. **Validación por correo institucional**, con confirmación por liga.
3. **Aprobación manual** de la primera cuenta de profesor de cada materia.

Cuesta poco ahora; después es una migración de datos con cuentas ya creadas.

## Otras notas

- **CORS** se resuelve en el gateway, no en cada servicio.
- La red `interna` del compose es `internal: true`: los contenedores **no tienen salida a
  Internet**. Es deliberado. Si un servicio necesita salir, hay que decidirlo a propósito.
- El panel de Traefik (`:8090`) y el de RabbitMQ están **sin protección**: son solo para
  desarrollo. No se despliegan así.
- El `.env` nunca se sube. `.env.example` lista las variables sin valores.
