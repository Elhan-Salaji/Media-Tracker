import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// The backend address the frontend talks to, as the browser sees it.
// VITE_API_BASE_URL from the environment or frontend/.env wins; without it the
// local backend on port 8080 applies.
const DEFAULT_API_BASE_URL = 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_')
  return {
    plugins: [react()],
    define: env.VITE_API_BASE_URL
      ? {}
      : { 'import.meta.env.VITE_API_BASE_URL': JSON.stringify(DEFAULT_API_BASE_URL) },
  }
})
