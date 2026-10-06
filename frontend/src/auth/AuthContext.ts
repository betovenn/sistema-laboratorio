import { createContext } from 'react'
import type { Claims } from './jwt'

export interface AuthState {
  usuario: Claims | null
  login: (cuenta: string, password: string) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthState | null>(null)
