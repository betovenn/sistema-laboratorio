# Contratos

Lo único compartido entre cuatro lenguajes. No hay una clase común que Java, Python, Node y
Go puedan importar: **el contrato es el acuerdo**.

```
contracts/
├── events/          Esquemas JSON de los dos eventos asíncronos
└── openapi/         Un OpenAPI por servicio (pendiente)
```

## Reglas

1. **Un cambio aquí es un cambio coordinado.** Mira en `docs/01-arquitectura.md` quién
   consume lo que vas a tocar.
2. **Agregar un campo opcional es compatible.** Quitar o renombrar uno, cambiar un tipo, o
   volver requerido algo opcional, **no lo es**: sube la versión.
3. Los eventos llevan `version`. Un consumidor que recibe una versión que no conoce la
   manda a la cola de muertos, no la adivina.
4. **Genera los clientes desde el contrato**, no los escribas a mano. Es lo que evita que un
   cambio de campo se descubra en producción.

## Pendiente

Los OpenAPI de los siete servicios. Mientras tanto, `docs/03-contratos-http.md` es la
referencia y debe mantenerse al día.
