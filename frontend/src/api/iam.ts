import { apiFetch } from './client'

export interface LoginResponse {
  token_acceso: string
}

// identificador = número de cuenta (alumno) o de empleado (profesor, laboratorio)
export function login(identificador: string, password: string) {
  return apiFetch<LoginResponse>('/auth/v1/login', {
    method: 'POST',
    body: JSON.stringify({ identificador, password }),
  })
}
