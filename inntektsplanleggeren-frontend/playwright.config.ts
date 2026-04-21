import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
    testDir: './tests',
    fullyParallel: true,
    forbidOnly: !!process.env.CI,
    failOnFlakyTests: true,
    reporter: 'html',
    use: {
        baseURL: 'http://localhost:5173/uforetrygd/selvbetjening/inntektsplanleggeren',
        trace: 'on-first-retry',
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
        url: 'http://localhost:5173',
        reuseExistingServer: !process.env.CI,
    },
});
