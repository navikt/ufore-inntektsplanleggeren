import { test, expect } from '@playwright/test';

test('mock server page loads correctly', async ({ page }) => {
  await page.goto('/');

  // Check that the main heading is visible
  const mainHeading = page.getByRole('heading', { name: 'Inntektsplanleggeren', level: 1 });
  await expect(mainHeading).toBeVisible();
});

test('page contains button elements', async ({ page }) => {
  await page.goto('/');

  // Check that at least one button element exists on the page
  const buttons = page.getByRole('button');
  const buttonCount = await buttons.count();
  expect(buttonCount).toBeGreaterThan(0);
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
