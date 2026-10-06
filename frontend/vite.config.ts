import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // En desarrollo, Vite reenvía /api al gateway del compose (localhost:8080), que a su
    // vez recorta /api/<servicio> y lo manda al microservicio. Así el navegador solo habla
    // con un origen y no hace falta configurar CORS. Requiere `docker compose up -d`.
    // Si el gateway quedó en otro puerto: GATEWAY_URL=http://localhost:18080 npm run dev
    proxy: {
      '/api': process.env.GATEWAY_URL ?? 'http://localhost:8080',
    },
  },
})
