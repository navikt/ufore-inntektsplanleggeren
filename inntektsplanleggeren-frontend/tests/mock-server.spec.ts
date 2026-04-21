import { test, expect } from '@playwright/test';

test('mock server page loads correctly', async ({ page }) => {
    await page.goto('/');

    // Check that the main heading is visible
    const mainHeading = page.getByRole('heading', { name: 'Inntektsplanleggeren', level: 1 });
    await expect(mainHeading).toBeVisible();

    // Check that at least one button element exists on the page
    const gåTilForventetInntektKnapp = page.getByRole('button', { name: 'Registrer inntekt for 2025' });
    await expect(gåTilForventetInntektKnapp).toBeVisible();
    await gåTilForventetInntektKnapp.click();

    await expect(page.getByText('Steg 1 av 3')).toBeVisible();

    const gåTilBeregningKnapp = page.getByRole('button', { name: 'Gå videre' });
    await expect(gåTilBeregningKnapp).toBeVisible();
    await gåTilBeregningKnapp.click();


    await page.waitForTimeout(1000);

    const tabell = page.locator('svg.highcharts-root');
    await expect(tabell).toBeVisible();


    const gåTilOppsummeringKnapp = page.getByRole('button', { name: 'Gå videre' });
    await expect(gåTilOppsummeringKnapp).toBeVisible()
    await gåTilOppsummeringKnapp.click()


    const gåTilKvitteringKnapp = page.getByRole('button', { name: 'Send inn' })
    await expect(gåTilKvitteringKnapp).toBeVisible()
    await gåTilKvitteringKnapp.click()
});

test('main content is accessible', async ({ page }) => {
    await page.goto('/');

    // Check that the main content area is present and accessible
    const mainContent = page.locator('#maincontent');
    await expect(mainContent).toBeVisible();

    // Verify it has the correct role
    const mainElement = page.locator('main[role="main"]');
    await expect(mainElement).toBeVisible();
});
