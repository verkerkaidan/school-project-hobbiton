import { test, expect, type Page } from '@playwright/test';
import { register } from './helpers/auth';
import {
  adminCredentials,
  authCookieValue,
  createUserReport,
  loginViaApi,
  type TestCredentials,
} from './helpers/api';

async function openAdminPage(adminPage: Page) {
  await adminPage.goto('/admin');
  await expect(adminPage.getByRole('heading', { name: /^admin$/i })).toBeVisible({ timeout: 15000 });
}

async function assertCannotLogin(page: Page, credentials: TestCredentials) {
  await page.goto('/authentication');
  await page.getByRole('button', { name: /^Login$/ }).click();
  await page.locator('input[name="email"]').fill(credentials.email);
  await page.locator('input[name="password"]').fill(credentials.password);
  await page.getByRole('button', { name: /^Login$/ }).last().click();
  await expect(page.getByText(/incorrect|invalid|failed|error|not found|disabled/i)).toBeVisible({ timeout: 10000 });
}

test.describe('Account deletion', () => {
  test('deletes a reported account as an admin', async ({ page, browser, request }) => {
    const reporter = await register(page);
    const reporterToken = await authCookieValue(page.context(), 'token');
    const reporterId = await authCookieValue(page.context(), 'userId');

    const targetContext = await browser.newContext();
    const targetPage = await targetContext.newPage();
    const target = await register(targetPage);
    const targetId = await authCookieValue(targetContext, 'userId');
    await targetContext.close();

    await createUserReport(request, reporterToken, reporterId, targetId);

    const adminContext = await browser.newContext();
    const adminPage = await adminContext.newPage();
    await loginViaApi(request, adminContext, adminCredentials());
    await openAdminPage(adminPage);

    const deleteUserButton = adminPage.getByTitle(`Delete user @${target.username}`);
    await expect(deleteUserButton).toBeVisible({ timeout: 15000 });
    await deleteUserButton.click();
    await expect(adminPage.getByText(new RegExp(`@${target.username}`))).toBeVisible();
    await adminPage.getByRole('button', { name: /confirm deletion/i }).click();
    await expect(adminPage.getByText(/account was deleted/i)).toBeVisible({ timeout: 15000 });
    await adminContext.close();

    await assertCannotLogin(page, target);
    expect(reporter.username).toBeTruthy();
  });

  test('deletes your own account', async ({ page }) => {
    const credentials = await register(page);

    await page.goto('/settings');
    await expect(page.getByRole('button', { name: /^delete account/i })).toBeVisible({ timeout: 10000 });
    await page.getByRole('button', { name: /^delete account/i }).click();
    await page.getByRole('button', { name: /delete my account/i }).click();
    await page.locator(`input[placeholder="${credentials.username}"]`).fill(credentials.username);
    await page.getByRole('button', { name: /^delete account$/i }).last().click();

    await page.waitForURL('**/authentication', { timeout: 15000 });
    await assertCannotLogin(page, credentials);
  });
});

