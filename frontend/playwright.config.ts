import { defineConfig, devices } from '@playwright/test'
import { resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const frontendDir = fileURLToPath(new URL('.', import.meta.url))
const backendDir = resolve(frontendDir, '..', 'backend')
const backendPort = Number(process.env.E2E_BACKEND_PORT ?? 18080)
const frontendPort = Number(process.env.E2E_FRONTEND_PORT ?? 5174)
const backendUrl = `http://127.0.0.1:${backendPort}`
const frontendUrl = `http://127.0.0.1:${frontendPort}`
const isWindows = process.platform === 'win32'
const mavenWrapper = isWindows ? '.\\mvnw.cmd' : './mvnw'

export default defineConfig({
  testDir: './e2e',
  timeout: 90_000,
  expect: {
    timeout: 15_000,
  },
  fullyParallel: false,
  workers: 1,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [['list'], ['html', { open: 'never' }]] : [['list']],
  use: {
    baseURL: frontendUrl,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  webServer: [
    {
      command: `${mavenWrapper} -Dspring-boot.run.profiles=test -Dspring-boot.run.arguments=--server.port=${backendPort} spring-boot:test-run`,
      cwd: backendDir,
      url: `${backendUrl}/actuator/health`,
      timeout: 120_000,
      reuseExistingServer: false,
      stdout: 'pipe',
      stderr: 'pipe',
      env: {
        ...process.env,
        APP_SEED_ENABLED: 'true',
        JWT_SECRET: 'bookos-e2e-secret-bookos-e2e-secret-bookos-e2e-secret-1234',
      },
    },
    {
      command: `npm run build && npm run preview -- --host 127.0.0.1 --port ${frontendPort} --strictPort`,
      cwd: frontendDir,
      url: frontendUrl,
      timeout: 90_000,
      reuseExistingServer: false,
      stdout: 'pipe',
      stderr: 'pipe',
      env: {
        ...process.env,
        VITE_API_BASE_URL: `${backendUrl}/api`,
      },
    },
  ],
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
