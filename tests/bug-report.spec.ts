import { test, expect } from '@playwright/test';
import { register } from './helpers/auth';

test.describe('Bug Report Service', () => {
  test.beforeEach(async ({ page }) => {
    await register(page);
  });

  test('should open bug report modal with form fields and close via Cancel', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
    await expect(bugReportButton).toBeVisible({ timeout: 8000 });
    await bugReportButton.click();

    const titleInput = page.locator('input[placeholder*="description of the issue"]');
    const descriptionInput = page.locator('textarea[placeholder*="What happened"]');
    await expect(titleInput).toBeVisible({ timeout: 5000 });
    await expect(descriptionInput).toBeVisible();

    const submitButton = page.getByRole('button', { name: /submit report/i });
    await expect(submitButton).toBeDisabled();

    await titleInput.fill('Some title');
    await expect(submitButton).toBeDisabled();

    await titleInput.clear();
    await descriptionInput.fill('Some description');
    await expect(submitButton).toBeDisabled();

    await titleInput.fill('Some title');
    await expect(submitButton).not.toBeDisabled();

    const cancelButton = page.getByRole('button', { name: /cancel/i });
    await cancelButton.click();
    await expect(titleInput).not.toBeVisible({ timeout: 5000 });
  });

  test('should submit a bug report and show success confirmation', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
    await bugReportButton.click();

    await page.locator('input[placeholder*="description of the issue"]').fill('E2E Test Bug Report');
    await page.locator('textarea[placeholder*="What happened"]').fill('This is an automated e2e test bug report. Please ignore.');

    await page.getByRole('button', { name: /submit report/i }).click();

    await expect(page.locator('text=Report submitted')).toBeVisible({ timeout: 10000 });
    await expect(page.locator("text=Thanks for letting us know")).toBeVisible();

    const closeButton = page.getByRole('button', { name: /^close$/i }).last();
    await closeButton.click();
    await expect(page.locator('text=Report submitted')).not.toBeVisible({ timeout: 5000 });
  });
});
