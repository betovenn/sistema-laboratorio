// Claims que emite el iam-service (ver docs/05-seguridad.md y EmisorTokens.emitirAcceso)
export interface Claims {
  iss: string
  sub: string // número de cuenta o de empleado
  rol: 'ALUMNO' | 'PROFESOR' | 'LABORATORIO'
  iat: number
  exp: number
  jti: string
}

export function decodeToken(token: string): Claims | null {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(payload)) as Claims
  } catch {
    return null
  }
}

export function isExpired(claims: Claims): boolean {
  return claims.exp * 1000 <= Date.now()
}
