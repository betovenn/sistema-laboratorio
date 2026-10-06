import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { getToken, setToken } from '../api/client'
import * as iam from '../api/iam'
import { AuthContext } from './AuthContext'
import { decodeToken, isExpired, type Claims } from './jwt'

// Recupera la sesión guardada, descartándola si el token ya venció
function sesionInicial(): Claims | null {
  const token = getToken()
  const claims = token ? decodeToken(token) : null
  if (!claims || isExpired(claims)) {
    setToken(null)
    return null
  }
  return claims
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Claims | null>(sesionInicial)

  const logout = useCallback(() => {
    setToken(null)
    setUsuario(null)
  }, [])

  const login = useCallback(async (cuenta: string, password: string) => {
    const { token } = await iam.login(cuenta, password)
    setToken(token)
    setUsuario(decodeToken(token))
  }, [])

  // Cierra la sesión automáticamente cuando el token expira
  useEffect(() => {
    if (!usuario) return
    const restante = usuario.exp * 1000 - Date.now()
    const id = setTimeout(logout, restante)
    return () => clearTimeout(id)
  }, [usuario, logout])

  const value = useMemo(() => ({ usuario, login, logout }), [usuario, login, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
