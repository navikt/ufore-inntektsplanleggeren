import { test, expect } from '@playwright/test';

test('Test enkel skjemaflyt', async ({ page }) => {
    await page.goto('/')

    const cookieJaKnapp = page.getByTestId('consent-banner-all')
    await expect(cookieJaKnapp).toBeVisible()
    await cookieJaKnapp.click()

    const mainHeading = page.getByRole('heading', { name: 'Inntektsplanleggeren', level: 1 })
    await expect(mainHeading).toBeVisible()

    const gåTilForventetInntektKnapp = page.getByRole('button', { name: 'Registrer inntekt for 2025' })
    await expect(gåTilForventetInntektKnapp).toBeVisible()
    await gåTilForventetInntektKnapp.click()

    await expect(page.getByText('Steg 1 av 3')).toBeVisible()

    const gåTilBeregningKnapp = page.getByRole('button', { name: 'Gå videre' })
    await expect(gåTilBeregningKnapp).toBeVisible()
    await gåTilBeregningKnapp.click()

    // Highchart animerer grafen, så uten timeout så ser grafen tom ut på Playwright report screenshot
    // await page.waitForTimeout(1000);

    await expect(page.getByText('Steg 2 av 3')).toBeVisible()
    const tabell = page.locator('svg.highcharts-root')
    await expect(tabell).toBeVisible()

    const gåTilOppsummeringKnapp = page.getByRole('button', { name: 'Gå videre' })
    await expect(gåTilOppsummeringKnapp).toBeVisible()
    await gåTilOppsummeringKnapp.click()

    await expect(page.getByText('Steg 3 av 3')).toBeVisible()

    const gåTilKvitteringKnapp = page.getByRole('button', { name: 'Send inn' })
    await expect(gåTilKvitteringKnapp).toBeVisible()
    await gåTilKvitteringKnapp.click()

    await page.waitForTimeout(3000)

    const kvitteringHeading = page.getByRole('heading', { name: 'Kvittering', level: 2 })
    await expect(kvitteringHeading).toBeVisible()
});
