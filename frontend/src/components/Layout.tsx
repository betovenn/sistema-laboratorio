import { Outlet } from 'react-router'
import { useAuth } from '../auth/useAuth'

export function Layout() {
  const { usuario, logout } = useAuth()

  return (
    <div className="layout">
      <header className="topbar">
        <span className="brand">Sistema de Laboratorio de Electrónica</span>
        <div className="topbar-user">
          <span>
            {usuario?.sub} · <span className="rol">{usuario?.rol}</span>
          </span>
          <button className="btn-secondary" onClick={logout}>
            Cerrar sesión
          </button>
        </div>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
