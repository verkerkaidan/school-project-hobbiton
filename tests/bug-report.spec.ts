import { test, expect } from '@playwright/test';
import { register } from './helpers/auth';

test.describe('Bug Report Service', () => {
  test.beforeEach(async ({ page }) => {
    await register(page);
  });

  test('should display the Report a bug section in settings', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugSection = page.locator('text=Report a bug');
    await expect(bugSection).toBeVisible({ timeout: 8000 });
  });

  test('should open bug report modal when clicking Report a bug', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
    await expect(bugReportButton).toBeVisible({ timeout: 8000 });
    await bugReportButton.click();

    // The modal heading should appear
    const modalHeading = page.locator('text=Report a bug').nth(1);
    await expect(modalHeading).toBeVisible({ timeout: 5000 });

    // Title and description inputs should be present
    await expect(page.locator('input[placeholder*="description of the issue"]')).toBeVisible();
    await expect(page.locator('textarea[placeholder*="What happened"]')).toBeVisible();
  });

  test('should close bug report modal when clicking Cancel', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
    await bugReportButton.click();

    await expect(page.locator('input[placeholder*="description of the issue"]')).toBeVisible({ timeout: 5000 });

    const cancelButton = page.getByRole('button', { name: /cancel/i });
    await cancelButton.click();

    // Modal should close
    await expect(page.locator('input[placeholder*="description of the issue"]')).not.toBeVisible({ timeout: 5000 });
  });

  test('should close bug report modal when clicking the X button', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
    await bugReportButton.click();

    await expect(page.locator('input[placeholder*="description of the issue"]')).toBeVisible({ timeout: 5000 });

    const closeButton = page.getByRole('button', { name: /close/i });
    await closeButton.click();

    await expect(page.locator('input[placeholder*="description of the issue"]')).not.toBeVisible({ timeout: 5000 });
  });

  test('should disable submit button when fields are empty', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
    await bugReportButton.click();

    await expect(page.locator('input[placeholder*="description of the issue"]')).toBeVisible({ timeout: 5000 });

    const submitButton = page.getByRole('button', { name: /submit report/i });
    await expect(submitButton).toBeDisabled();
  });

  test('should disable submit button when only title is filled', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
    await bugReportButton.click();

    await expect(page.locator('input[placeholder*="description of the issue"]')).toBeVisible({ timeout: 5000 });

    await page.locator('input[placeholder*="description of the issue"]').fill('Some title');

    const submitButton = page.getByRole('button', { name: /submit report/i });
    await expect(submitButton).toBeDisabled();
  });

  test('should disable submit button when only description is filled', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
    await bugReportButton.click();

    await expect(page.locator('input[placeholder*="description of the issue"]')).toBeVisible({ timeout: 5000 });

    await page.locator('textarea[placeholder*="What happened"]').fill('Some description');

    const submitButton = page.getByRole('button', { name: /submit report/i });
    await expect(submitButton).toBeDisabled();
  });

  test('should enable submit button when both fields are filled', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
    await bugReportButton.click();

    await expect(page.locator('input[placeholder*="description of the issue"]')).toBeVisible({ timeout: 5000 });

    await page.locator('input[placeholder*="description of the issue"]').fill('Button click does nothing');
    await page.locator('textarea[placeholder*="What happened"]').fill('When I click the submit button nothing happens.');

    const submitButton = page.getByRole('button', { name: /submit report/i });
    await expect(submitButton).not.toBeDisabled();
  });

  test('should submit a bug report and show success confirmation', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
    await bugReportButton.click();

    await expect(page.locator('input[placeholder*="description of the issue"]')).toBeVisible({ timeout: 5000 });

    await page.locator('input[placeholder*="description of the issue"]').fill('E2E Test Bug Report');
    await page.locator('textarea[placeholder*="What happened"]').fill('This is an automated e2e test bug report. Please ignore.');

    const submitButton = page.getByRole('button', { name: /submit report/i });
    await submitButton.click();

    // Should show success state
    const successMessage = page.locator('text=Report submitted');
    await expect(successMessage).toBeVisible({ timeout: 10000 });

    // Confirmation copy should be present
    await expect(page.locator("text=Thanks for letting us know")).toBeVisible();
  });

  test('should close modal after successful submission', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
    await bugReportButton.click();

    await expect(page.locator('input[placeholder*="description of the issue"]')).toBeVisible({ timeout: 5000 });

    await page.locator('input[placeholder*="description of the issue"]').fill('Another E2E Bug');
    await page.locator('textarea[placeholder*="What happened"]').fill('This is another automated e2e test bug report.');

    await page.getByRole('button', { name: /submit report/i }).click();

    // Wait for success state
    const closeButton = page.getByRole('button', { name: /^close$/i });
    await expect(closeButton).toBeVisible({ timeout: 10000 });
    await closeButton.click();

    // Modal should be dismissed
    await expect(page.locator('text=Report submitted')).not.toBeVisible({ timeout: 5000 });
  });
});
