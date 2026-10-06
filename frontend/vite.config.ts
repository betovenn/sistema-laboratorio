import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // En desarrollo, Vite reenvía las peticiones a cada microservicio.
    // Así el navegador solo habla con un origen y no hace falta configurar CORS.
    // Cuando exista un API Gateway, todas estas rutas apuntarán a él.
    proxy: {
      '/api/iam': {
        target: 'http://localhost:8080',
        rewrite: (path) => path.replace(/^\/api\/iam/, ''),
      },
    },
  },
})
