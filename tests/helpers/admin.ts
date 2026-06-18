import { Page } from '@playwright/test';

/**
 * Crafts a JWT with ROLE_ADMIN in the `role` claim and sets it as the `token`
 * cookie. The frontend decodes the payload without verifying the signature
 * (lib/jwt.ts), so this is enough to pass the client-side admin guard.
 * The API Gateway admin endpoints have no backend auth, so API calls go through.
 */
export async function elevateToAdmin(page: Page): Promise<void> {
  const userId = await page.evaluate(() => {
    const match = document.cookie.match(/(?:^|;\s*)userId=([^;]*)/);
    return match ? decodeURIComponent(match[1]) : '';
  });

  if (!userId) throw new Error('elevateToAdmin: no userId cookie found — register/login first');

  const adminToken = await page.evaluate((uid) => {
    function b64url(str: string) {
      return btoa(str).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    }
    const header = b64url(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = b64url(JSON.stringify({
      sub: 'User Details',
      userId: uid,
      role: ['ROLE_ADMIN', 'ROLE_USER'],
      iat: Math.floor(Date.now() / 1000),
      exp: Math.floor(Date.now() / 1000) + 6 * 3600,
      iss: 'auth-service',
    }));
    return `${header}.${payload}.test-signature`;
  }, userId);

  await page.context().addCookies([{
    name: 'token',
    value: adminToken,
    url: page.url().replace(/\/[^/]*$/, '/') || 'http://localhost:3000/',
    expires: Math.floor(Date.now() / 1000) + 6 * 3600,
  }]);
}

/**
 * Submits a bug report through the settings UI.
 * Assumes the user is already logged in.
 */
export async function submitBugReport(page: Page, title: string, description: string): Promise<void> {
  await page.goto('/settings');
  await page.waitForLoadState('networkidle');

  const bugReportButton = page.getByRole('button').filter({ has: page.locator('text=Report a bug') });
  await bugReportButton.waitFor({ state: 'visible', timeout: 10000 });
  await bugReportButton.click();

  await page.locator('input[placeholder*="description of the issue"]').waitFor({ state: 'visible', timeout: 5000 });
  await page.locator('input[placeholder*="description of the issue"]').fill(title);
  await page.locator('textarea[placeholder*="What happened"]').fill(description);

  await page.getByRole('button', { name: /submit report/i }).click();
  await page.locator('text=Report submitted').waitFor({ state: 'visible', timeout: 10000 });

  await page.getByRole('button', { name: /^close$/i }).last().click();
}
