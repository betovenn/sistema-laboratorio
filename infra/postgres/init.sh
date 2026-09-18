#!/bin/bash
# Se ejecuta UNA sola vez, cuando el volumen de PostgreSQL está vacío.
# Crea una base y un rol por servicio. Ningún rol puede conectarse a la base de otro:
# la frontera entre microservicios queda impuesta por el motor, no por disciplina.
set -e

crear_base() {
  local rol="$1" base="$2" clave="$3"
  echo "  -> $base (rol: $rol)"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<-SQL
    CREATE ROLE $rol LOGIN PASSWORD '$clave';
    CREATE DATABASE $base OWNER $rol;
    REVOKE CONNECT ON DATABASE $base FROM PUBLIC;
    GRANT CONNECT ON DATABASE $base TO $rol;
SQL
}

echo "Creando bases por servicio..."
crear_base iam        iam_db        "${IAM_DB_PASSWORD:?falta IAM_DB_PASSWORD}"
crear_base escolar    escolar_db    "${ESCOLAR_DB_PASSWORD:?falta ESCOLAR_DB_PASSWORD}"
crear_base logistica  logistica_db  "${LOGISTICA_DB_PASSWORD:?falta LOGISTICA_DB_PASSWORD}"
crear_base equipos    equipos_db    "${EQUIPOS_DB_PASSWORD:?falta EQUIPOS_DB_PASSWORD}"
crear_base ejecucion  ejecucion_db  "${EJECUCION_DB_PASSWORD:?falta EJECUCION_DB_PASSWORD}"
crear_base evaluacion evaluacion_db "${EVALUACION_DB_PASSWORD:?falta EVALUACION_DB_PASSWORD}"
echo "Listo: 6 bases, 6 roles aislados."
