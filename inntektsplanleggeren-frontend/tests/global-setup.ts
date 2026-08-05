import { chromium, expect, type FullConfig } from '@playwright/test'

// Playwright starter webServer før globalSetup, men webServer.url sjekker bare at Vite
// lytter på porten, ikke at den er klar. Her sikrer vi at alt er klart før første test.
export default async function globalSetup(config: FullConfig) {
    const baseURL = config.projects[0]?.use?.baseURL

    if (!baseURL) {
        throw new Error('Fant ingen baseURL i Playwright-konfigurasjonen – oppvarming kan ikke kjøre.')
    }

    const browser = await chromium.launch()
    const page = await browser.newPage()

    // Dekoratøren er ekstern og treg, og trengs ikke testes så vi unngår laste den inn
    await page.route(/nav\.no|uxsignals\.com/, (route) => route.abort())

    try {
        await expect(async () => {
            await page.goto(baseURL, { waitUntil: 'load' })
            await page.getByRole('heading', { name: 'Inntektsplanleggeren', level: 1 }).waitFor({ timeout: 5_000 })
        }).toPass({
            // Kald pre-bundling kan ta lang tid på en runner med få kjerner.
            timeout: 120_000,
            intervals: [1_000],
        })
    } finally {
        await browser.close()
    }
}
