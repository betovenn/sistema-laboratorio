import { useAuth } from '../auth/useAuth'

export function HomePage() {
  const { usuario } = useAuth()
  if (!usuario) return null

  return (
    <section className="card">
      <h2>Bienvenido, {usuario.sub}</h2>
      <dl className="claims">
        <dt>Rol</dt>
        <dd>{usuario.rol}</dd>
        <dt>Sesión iniciada</dt>
        <dd>{new Date(usuario.iat * 1000).toLocaleString()}</dd>
        <dt>Expira</dt>
        <dd>{new Date(usuario.exp * 1000).toLocaleString()}</dd>
      </dl>
    </section>
  )
}
