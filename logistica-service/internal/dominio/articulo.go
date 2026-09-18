// Package dominio define las entidades de logistica-service (MS-04).
//
// Es el único servicio con consistencia fuerte obligatoria: dos empleados en el mostrador
// no pueden entregar la misma pieza. El bloqueo de renglón lo hace PostgreSQL
// (SELECT ... FOR UPDATE), no el lenguaje.
package dominio

import "time"

// TipoArticulo separa los dos comportamientos del inventario.
//
// Un ACTIVO se devuelve, tiene número de serie y su existencia NO disminuye al prestarse:
// solo cambia de estado. Un CONSUMIBLE se descuenta de las existencias al consumirse,
// perderse o dañarse. El flujo de devolución no puede tratarlos igual (D-07).
type TipoArticulo string

const (
	TipoActivo     TipoArticulo = "ACTIVO"
	TipoConsumible TipoArticulo = "CONSUMIBLE"
)

// EstadoActivo es el ciclo de vida de una pieza individual retornable.
type EstadoActivo string

const (
	ActivoDisponible EstadoActivo = "DISPONIBLE"
	ActivoPrestado   EstadoActivo = "PRESTADO"
	ActivoBaja       EstadoActivo = "BAJA"
)

// Articulo es una clase de material: "osciloscopio", "resistencia de 220 ohm".
type Articulo struct {
	ID     int64        `db:"id"      json:"id"`
	Clave  string       `db:"clave"   json:"clave"`  // referenciada desde academico-service
	Nombre string       `db:"nombre"  json:"nombre"`
	Tipo   TipoArticulo `db:"tipo"    json:"tipo"`
	Unidad string       `db:"unidad"  json:"unidad"` // "pieza", "metro"
}

// EsRetornable indica si hay que esperar que vuelva al devolver el kit.
func (a Articulo) EsRetornable() bool {
	return a.Tipo == TipoActivo
}

// ActivoUnidad es UNA pieza retornable concreta, rastreada por número de serie.
// Solo aplica a artículos de tipo ACTIVO.
type ActivoUnidad struct {
	ID         int64        `db:"id"          json:"id"`
	ArticuloID int64        `db:"articulo_id" json:"articulo_id"`
	NumSerie   string       `db:"num_serie"   json:"num_serie"`
	Estado     EstadoActivo `db:"estado"      json:"estado"`
}

// Existencia es la cantidad disponible de un CONSUMIBLE.
// Los activos no llevan existencia: se cuentan por unidad.
type Existencia struct {
	ArticuloID int64 `db:"articulo_id" json:"articulo_id"`
	Cantidad   int   `db:"cantidad"    json:"cantidad"`
}

// Movimiento es la bitácora de todo cambio de existencias, con su motivo.
// Sin esto, un descuadre de inventario es imposible de auditar.
type Movimiento struct {
	ID         int64     `db:"id"          json:"id"`
	ArticuloID int64     `db:"articulo_id" json:"articulo_id"`
	Delta      int       `db:"delta"       json:"delta"` // negativo al descontar
	Motivo     string    `db:"motivo"      json:"motivo"`
	Usuario    string    `db:"usuario"     json:"usuario"` // número de empleado
	CreadoEn   time.Time `db:"creado_en"   json:"creado_en"`
}

// RenglonDemanda es una línea del cálculo de material para una sesión programada.
type RenglonDemanda struct {
	ClaveArticulo string `json:"clave_articulo"`
	Nombre        string `json:"nombre"`
	Requerido     int    `json:"requerido"`
	Disponible    int    `json:"disponible"`
	Faltante      int    `json:"faltante"`
}

// CalcularRequerido traduce la cantidad declarada en la práctica a piezas reales.
//
// Es el corazón de la funcionalidad de más valor del sistema: el laboratorio deja de
// adivinar cuánto material preparar. La unidad de consumo la declara el Profesor en
// academico-service por cada componente (D-06).
//
//	POR_EQUIPO  -> cantidad x número de equipos
//	POR_ALUMNO  -> cantidad x número de alumnos inscritos
//
// Ejemplo del requerimiento: 10 equipos de 4 alumnos, con 1 osciloscopio, 2 puntas y
// 5 resistencias POR_EQUIPO, dan 10 osciloscopios, 20 puntas y 50 resistencias.
func CalcularRequerido(cantidad int, unidadConsumo string, equipos, alumnosInscritos int) int {
	switch unidadConsumo {
	case "POR_EQUIPO":
		return cantidad * equipos
	case "POR_ALUMNO":
		return cantidad * alumnosInscritos
	default:
		return 0
	}
}
