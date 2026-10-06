import { apiFetch } from './client'

export interface LoginResponse {
  token: string
}

export function login(cuenta: string, password: string) {
  return apiFetch<LoginResponse>('/iam/auth/login', {
    method: 'POST',
    body: JSON.stringify({ cuenta, password }),
  })
}
