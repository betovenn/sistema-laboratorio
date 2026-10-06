import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from './useAuth'

// Si no hay sesión, redirige a /login recordando a dónde quería ir el usuario
export function ProtectedRoute() {
  const { usuario } = useAuth()
  const location = useLocation()

  if (!usuario) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  return <Outlet />
}
