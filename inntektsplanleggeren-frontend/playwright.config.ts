import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
    testDir: './tests',
    globalSetup: './tests/global-setup.ts',
    fullyParallel: true,
    forbidOnly: !!process.env.CI,
    workers: process.env.CI ? 1 : 3,
    failOnFlakyTests: true,
    retries: process.env.CI ? 2 : 0,
    reporter: [['list'], ['html', { open: 'never' }]],
    use: {
        baseURL: 'http://localhost:5173/uforetrygd/selvbetjening/inntektsplanleggeren/',
        trace: 'retain-on-failure',
        screenshot: 'only-on-failure',
    },
    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] },
        },

        {
            name: 'firefox',
            use: { ...devices['Desktop Firefox'] },
        },

        {
            name: 'webkit',
            use: { ...devices['Desktop Safari'] },
        },
    ],
    webServer: {
        command: 'npm run mock',
        url: 'http://localhost:5173/uforetrygd/selvbetjening/inntektsplanleggeren/',
        reuseExistingServer: !process.env.CI,
    },
})
