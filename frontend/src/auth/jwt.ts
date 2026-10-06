// Claims que emite el iam-service (ver AuthController.generarToken)
export interface Claims {
  sub: string
  cuenta: string
  rol: string
  iat: number
  exp: number
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
