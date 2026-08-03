import { expect, test } from '@playwright/test'

test('Test enkel skjemaflyt', async ({ page }) => {
    await page.goto('/')

    const cookieJaKnapp = page.getByTestId('consent-banner-all')
    // Lukk banner hvis det finnes, uten å feile om det ikke er der
    if (await cookieJaKnapp.isVisible()) {
        await cookieJaKnapp.click()
    }

    const mainHeading = page.getByRole('heading', { name: 'Inntektsplanleggeren', level: 1 })
    await expect(mainHeading, 'Tittel på hovedside vises').toBeVisible()

    const gåTilForventetInntektKnapp = page.getByRole('button', { name: /Start inntektsplanlegger|Registrer inntekt for/ })
    await expect(gåTilForventetInntektKnapp, 'Gå til forventet inntekt knapp vises').toBeVisible()
    await gåTilForventetInntektKnapp.click()

    await expect(page.getByText('Steg 1 av 3'), 'Stegindikator vises (1 av 3)').toBeVisible()

    const gåTilBeregningKnapp = page.getByRole('button', { name: 'Gå videre' })
    await expect(gåTilBeregningKnapp, 'Gå til beregning knapp vises').toBeVisible()
    await gåTilBeregningKnapp.click()

    // Highchart animerer grafen, så uten timeout så ser grafen tom ut på Playwright report screenshot
    // await page.waitForTimeout(1000);

    await expect(page.getByText('Steg 2 av 3'), 'Stegindikator vises (2 av 3)').toBeVisible()
    const tabell = page.locator('svg.highcharts-root')
    await expect(tabell, 'Inntektstabell vises').toBeVisible()

    const gåTilOppsummeringKnapp = page.getByRole('button', { name: 'Gå videre' })
    await expect(gåTilOppsummeringKnapp, 'Gå til oppsummering knapp vises').toBeVisible()
    await gåTilOppsummeringKnapp.click()

    await expect(page.getByText('Steg 3 av 3'), 'Stegindikator vises (3 av 3)').toBeVisible()

    const gåTilKvitteringKnapp = page.getByRole('button', { name: 'Send inn' })
    await expect(gåTilKvitteringKnapp, 'Gå til kvittering knapp vises').toBeVisible()
    await gåTilKvitteringKnapp.click()

    await page.waitForTimeout(3000)

    const kvitteringHeading = page.getByRole('heading', { name: 'Kvittering', level: 2 })
    await expect(kvitteringHeading, 'Tittel på kvitteringsside vises').toBeVisible()
})
