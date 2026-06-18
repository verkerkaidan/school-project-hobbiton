import { test, expect, type Page } from '@playwright/test';
import { register } from './helpers/auth';
import { elevateToAdmin, submitBugReport } from './helpers/admin';

// Targets the specific report row by its p-5 class (each row in BugReportsTable)
// rather than a generic div, which would match ancestor wrappers containing all rows.
const getBugReportRow = (page: Page, title: string) => page.locator('.p-5').filter({ hasText: title });

// ReportsTable has "All" too, so nth(1) picks the BugReportsTable one.
// "Open" only exists in BugReportsTable so no index needed.
const bugReportTab = (page: Page, name: 'Open' | 'All') =>
  name === 'All'
    ? page.getByRole('button', { name: 'All', exact: true }).nth(1)
    : page.getByRole('button', { name: 'Open', exact: true });

test.describe('Admin — Bug Report Flow', () => {
  test.beforeEach(async ({ page }) => {
    await register(page);
  });

  test('should show the admin panel when logged in as admin', async ({ page }) => {
    await elevateToAdmin(page);
    await page.goto('/admin');
    await page.waitForLoadState('networkidle');

    await expect(page).toHaveURL(/\/admin/);
    await expect(page.getByRole('heading', { name: /admin/i })).toBeVisible({ timeout: 10000 });
    await expect(page.getByText('Bug Reports')).toBeVisible();
  });

  test('should redirect non-admin users away from /admin', async ({ page }) => {
    await page.goto('/admin');

    // Client-side guard fires a router.replace('/') — wait for the navigation to complete
    await expect(page).not.toHaveURL(/\/admin/, { timeout: 15000 });
    await expect(page).toHaveURL(/localhost/);
  });

  test('should display a submitted bug report in the admin panel', async ({ page }) => {
    const title = `E2E Bug ${Date.now()}`;
    const description = 'Automated test report — please ignore.';

    await submitBugReport(page, title, description);
    await elevateToAdmin(page);

    await page.goto('/admin');
    await page.waitForLoadState('networkidle');

    const row = getBugReportRow(page, title);
    await expect(row).toBeVisible({ timeout: 15000 });
    await expect(row.getByText(description)).toBeVisible();
  });

  test('should show OPEN status badge on a new bug report', async ({ page }) => {
    const title = `E2E Open Badge ${Date.now()}`;

    await submitBugReport(page, title, 'Checking the OPEN badge.');
    await elevateToAdmin(page);

    await page.goto('/admin');
    await page.waitForLoadState('networkidle');

    const reportRow = getBugReportRow(page, title);
    await expect(reportRow.getByText('OPEN', { exact: true })).toBeVisible({ timeout: 15000 });
  });

  test('should close a bug report and show CLOSED status', async ({ page }) => {
    const title = `E2E Close Test ${Date.now()}`;

    await submitBugReport(page, title, 'This report should be closed by the admin.');
    await elevateToAdmin(page);

    await page.goto('/admin');
    await page.waitForLoadState('networkidle');

    const reportRow = getBugReportRow(page, title);
    await expect(reportRow).toBeVisible({ timeout: 15000 });

    await reportRow.getByRole('button', { name: /^close$/i }).click();

    await expect(reportRow.getByText('CLOSED')).toBeVisible({ timeout: 10000 });
    await expect(reportRow.getByRole('button', { name: /^close$/i })).not.toBeVisible();
  });

  test('should hide a closed report when filtering by Open tab', async ({ page }) => {
    const title = `E2E Filter Test ${Date.now()}`;

    await submitBugReport(page, title, 'This report will be closed and filtered out.');
    await elevateToAdmin(page);

    await page.goto('/admin');
    await page.waitForLoadState('networkidle');

    const reportRow = getBugReportRow(page, title);
    await expect(reportRow).toBeVisible({ timeout: 15000 });
    await reportRow.getByRole('button', { name: /^close$/i }).click();
    await expect(reportRow.getByText('CLOSED')).toBeVisible({ timeout: 10000 });

    await bugReportTab(page, 'All').click();
    await expect(page.getByText(title)).toBeVisible();

    await bugReportTab(page, 'Open').click();
    await expect(page.getByText(title)).not.toBeVisible({ timeout: 5000 });
  });

  test('should filter bug reports by search query', async ({ page }) => {
    const uniqueWord = `UniqueSearch${Date.now()}`;
    const title = `${uniqueWord} Bug`;

    await submitBugReport(page, title, 'Searchable report description.');
    await elevateToAdmin(page);

    await page.goto('/admin');
    await page.waitForLoadState('networkidle');

    await expect(page.getByText(title)).toBeVisible({ timeout: 15000 });

    await page.locator('input[placeholder*="Search title"]').fill(uniqueWord);
    await expect(page.getByText(title)).toBeVisible();

    await page.locator('input[placeholder*="Search title"]').fill('xyzzy_no_match');
    await expect(page.getByText(title)).not.toBeVisible();
  });
});
