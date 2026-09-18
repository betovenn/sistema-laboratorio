// Package eventos define los mensajes que logistica-service publica y consume.
//
// Los esquemas normativos viven en contracts/events/. Si cambias una estructura de aquí,
// cambia también el JSON Schema: son el contrato entre cuatro lenguajes distintos.
package eventos

import "time"

// Nombres de los dos únicos eventos asíncronos del sistema.
const (
	TipoKitSolicitado        = "kit.solicitado"
	TipoIncidenciaRegistrada = "incidencia.registrada"
)

// KitSolicitado lo publica equipos-service y lo consume este servicio
// en la cola logistica.kits.
type KitSolicitado struct {
	EventoID             string    `json:"evento_id"`
	Tipo                 string    `json:"tipo"`
	Version              int       `json:"version"`
	OcurridoEn           time.Time `json:"ocurrido_en"`
	EquipoID             int64     `json:"equipo_id"`
	PracticaProgramadaID int64     `json:"practica_programada_id"`
	GrupoID              int64     `json:"grupo_id"`
	CuentaResponsable    string    `json:"cuenta_responsable"`
	Integrantes          int       `json:"integrantes"`
}

// DetalleIncidencia es una línea de daño dentro del evento.
type DetalleIncidencia struct {
	IncidenciaID  int64  `json:"incidencia_id"`
	ClaveArticulo string `json:"clave_articulo"`
	Tipo          string `json:"tipo"`
	Cantidad      int    `json:"cantidad"`
}

// IncidenciaRegistrada la publica este servicio y la consume evaluacion-service
// en la cola evaluacion.incidencias.
type IncidenciaRegistrada struct {
	EventoID             string              `json:"evento_id"`
	Tipo                 string              `json:"tipo"`
	Version              int                 `json:"version"`
	OcurridoEn           time.Time           `json:"ocurrido_en"`
	EquipoID             int64               `json:"equipo_id"`
	PracticaProgramadaID int64               `json:"practica_programada_id"`
	Incidencias          []DetalleIncidencia `json:"incidencias"`
}
