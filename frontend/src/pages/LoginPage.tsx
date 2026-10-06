import { useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/useAuth'

export function LoginPage() {
  const { usuario, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const destino = (location.state as { from?: string } | null)?.from ?? '/'

  const [cuenta, setCuenta] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [cargando, setCargando] = useState(false)

  if (usuario) return <Navigate to={destino} replace />

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setCargando(true)
    try {
      await login(cuenta, password)
      navigate(destino, { replace: true })
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setError('Cuenta o contraseña incorrecta')
      } else {
        setError('No se pudo conectar con el servicio de autenticación')
      }
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="login-page">
      <form className="card login-card" onSubmit={onSubmit}>
        <h1>Iniciar sesión</h1>
        <p className="muted">Sistema de Laboratorio de Electrónica</p>

        <label>
          Cuenta
          <input
            value={cuenta}
            onChange={(e) => setCuenta(e.target.value)}
            autoComplete="username"
            autoFocus
            required
          />
        </label>

        <label>
          Contraseña
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </label>

        {error && <p className="error">{error}</p>}

        <button type="submit" disabled={cargando}>
          {cargando ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </div>
  )
}
