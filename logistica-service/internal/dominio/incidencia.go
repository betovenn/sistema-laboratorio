package dominio

// TipoIncidencia clasifica lo que le pasó al material.
//
// Sin acentos ni eñe en los identificadores, por convención del proyecto: DANADO.
type TipoIncidencia string

const (
	IncidenciaQuemado TipoIncidencia = "QUEMADO"
	IncidenciaPerdido TipoIncidencia = "PERDIDO"
	IncidenciaDanado  TipoIncidencia = "DANADO"
)

// Incidencia es material quemado, perdido o dañado, reportado al devolver el kit.
//
// Al registrarse, este servicio publica el evento incidencia.registrada y
// evaluacion-service aplica la penalización a la calificación individual de CADA
// integrante del equipo, conforme al tabulador definido por la academia.
//
// El tabulador todavía no está definido: es una decisión académica, no técnica.
type Incidencia struct {
	ID           int64          `db:"id"            json:"id"`
	DevolucionID int64          `db:"devolucion_id" json:"devolucion_id"`
	ArticuloID   int64          `db:"articulo_id"   json:"articulo_id"`
	Tipo         TipoIncidencia `db:"tipo"          json:"tipo"`
	Cantidad     int            `db:"cantidad"      json:"cantidad"`
	Nota         string         `db:"nota"          json:"nota"`
}

// AfectaExistencias indica si hay que descontar del inventario.
//
// Un consumible quemado se descuenta para siempre. Un activo dañado no se descuenta:
// se marca la pieza como BAJA, porque se rastrea por número de serie.
func (i Incidencia) AfectaExistencias(a Articulo) bool {
	return a.Tipo == TipoConsumible
}
