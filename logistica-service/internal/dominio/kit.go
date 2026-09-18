package dominio

import "time"

// EstadoSolicitud es el ciclo de vida de un kit, del aviso del equipo a la devolución.
type EstadoSolicitud string

const (
	SolicitudSolicitada EstadoSolicitud = "SOLICITADO" // llegó el evento kit.solicitado
	SolicitudPreparada  EstadoSolicitud = "PREPARADO"  // el laboratorio lo armó
	SolicitudEntregada  EstadoSolicitud = "ENTREGADO"  // lo recogió el responsable
	SolicitudDevuelta   EstadoSolicitud = "DEVUELTO"   // volvió, con o sin incidencias
)

// SolicitudKit es el material que un equipo pidió para una práctica programada.
//
// La crea el consumidor del evento kit.solicitado que publica equipos-service. El equipo
// origina el aviso, pero el kit, su fila de atención y su estado son datos de inventario:
// este servicio es su dueño (D-02).
type SolicitudKit struct {
	ID                   int64           `db:"id"                     json:"id"`
	PracticaProgramadaID int64           `db:"practica_programada_id" json:"practica_programada_id"`
	EquipoID             int64           `db:"equipo_id"              json:"equipo_id"`
	GrupoID              int64           `db:"grupo_id"               json:"grupo_id"`
	Estado               EstadoSolicitud `db:"estado"                 json:"estado"`
	SolicitadoEn         time.Time       `db:"solicitado_en"          json:"solicitado_en"`
}

// PuedeEntregarse evita entregar un kit que no está armado o que ya se entregó.
func (s SolicitudKit) PuedeEntregarse() bool {
	return s.Estado == SolicitudPreparada
}

// PuedeDevolverse evita registrar la devolución de algo que nunca salió.
func (s SolicitudKit) PuedeDevolverse() bool {
	return s.Estado == SolicitudEntregada
}

// KitRenglon es una línea del kit físico.
// Si el artículo es un ACTIVO, ActivoUnidadID apunta a la pieza concreta que salió.
type KitRenglon struct {
	ID             int64  `db:"id"               json:"id"`
	SolicitudID    int64  `db:"solicitud_id"     json:"solicitud_id"`
	ArticuloID     int64  `db:"articulo_id"      json:"articulo_id"`
	Cantidad       int    `db:"cantidad"         json:"cantidad"`
	ActivoUnidadID *int64 `db:"activo_unidad_id" json:"activo_unidad_id,omitempty"`
}

// Entrega registra quién se llevó el material.
//
// CuentaResponsable es el número de cuenta del integrante que lo recibe físicamente.
// Es un identificador de otro servicio, no una llave foránea.
type Entrega struct {
	ID                int64     `db:"id"                 json:"id"`
	SolicitudID       int64     `db:"solicitud_id"       json:"solicitud_id"`
	CuentaResponsable string    `db:"cuenta_responsable" json:"cuenta_responsable"`
	EntregadoPor      string    `db:"entregado_por"      json:"entregado_por"` // empleado
	EntregadoEn       time.Time `db:"entregado_en"       json:"entregado_en"`
}

// Devolucion registra el regreso del kit. Las incidencias cuelgan de aquí.
type Devolucion struct {
	ID          int64     `db:"id"            json:"id"`
	SolicitudID int64     `db:"solicitud_id"  json:"solicitud_id"`
	RecibidoPor string    `db:"recibido_por"  json:"recibido_por"` // empleado
	RecibidoEn  time.Time `db:"recibido_en"   json:"recibido_en"`
}
