# frontend — prototipo

Prototipo de interfaz en React + TypeScript + Vite. Por ahora **solo cubre el inicio de
sesión** contra `iam-service` y muestra los datos del token.

La arquitectura del frontend (una SPA con rutas por rol o varias aplicaciones) **sigue sin
decidirse**: está en los pendientes de [`docs/09-decisiones.md`](../docs/09-decisiones.md).
Decídela con el equipo antes de hacer crecer este prototipo.

## Cómo correrlo

El frontend no habla directo con los microservicios: Vite reenvía todo `/api` al gateway
del compose (`http://localhost:8080`), igual que en producción.

```bash
# En la raíz del repositorio: infraestructura + iam-service.
# Antes, genera la llave de firma y copia .env.example a .env (ver ese archivo).
docker compose up -d

# Aquí
npm install
npm run dev        # http://localhost:5173
```

Si el gateway no está en `localhost:8080` (por ejemplo, porque otro programa ocupa ese
puerto), indica dónde está: `GATEWAY_URL=http://localhost:18080 npm run dev`.

Con `IAM_USUARIO_PRUEBA=true` en el `.env`, `iam-service` crea un usuario de prueba:
número de cuenta **11111**, contraseña **12345** (rol ALUMNO). Solo para desarrollo,
mientras no exista `/v1/registro`.

## Scripts

| Comando | Qué hace |
|---|---|
| `npm run dev` | Servidor de desarrollo con recarga en caliente. |
| `npm run build` | Revisión de tipos y compilación a `dist/`. |
| `npm run lint` | Oxlint. |
| `npm run preview` | Sirve la compilación de `dist/`. |

## Sesión

- El token de acceso (RS256, 60 minutos) se guarda en `localStorage`; la sesión se cierra
  sola cuando vence. Todavía no hay token de refresco.
- El token solo trae `sub` (número de cuenta o de empleado), `rol` y la vigencia. Lo que
  dependa de grupos o equipos se le pregunta al servicio correspondiente, no al token.
