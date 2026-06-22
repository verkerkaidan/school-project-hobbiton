import { test, expect } from '@playwright/test';
import { register, logout, isLoggedIn, login, registerViaAPI, promoteToAdmin, loginViaAPI } from './helpers/auth';

test.describe('Authentication', () => {

  test.describe('Registration', () => {
    test('should register a new user successfully', async ({ page }) => {
      const credentials = await register(page);

      expect(await isLoggedIn(page)).toBeTruthy();

      await expect(page.locator('text=Welcome')).toContainText(
        credentials.username
      );
    });

    test('should show validation error for password mismatch', async ({ page }) => {
      await page.goto('/authentication');

      await page.getByRole('button', { name: /^Register$/ }).click();

      await page.locator('input[name="username"]').fill('testuser');
      await page.locator('input[name="email"]').fill('test@test.com');
      await page.locator('input[name="password"]').fill('@Password123');
      await page.locator('input[name="confirmPassword"]').fill('@Password124');

      await page.getByRole('button', { name: /^Register$/ }).last().click();

      await expect(
        page.getByText(/passwords do not match/i)
      ).toBeVisible();
    });

    test('should require all mandatory fields', async ({ page }) => {
      await page.goto('/authentication');

      await page.getByRole('button', { name: /^Register$/ }).click();

      await page.getByRole('button', { name: /^Register$/ }).last().click();

      await expect(page.getByText(/username is required/i)).toBeVisible();
      await expect(page.getByText(/email is required/i)).toBeVisible();
      await expect(page.getByText(/password is required/i)).toBeVisible();
    });

    test('should not allow duplicate registration', async ({ page }) => {
      const credentials = await register(page);

      await logout(page);

      await page.goto('/authentication');
      await page.getByRole('button', { name: /^Register$/ }).click();

      await page.locator('input[name="username"]').fill(credentials.username);
      await page.locator('input[name="email"]').fill(credentials.email);
      await page.locator('input[name="password"]').fill(credentials.password);
      await page.locator('input[name="confirmPassword"]').fill(credentials.password);

      await page.getByRole('button', { name: /^Register$/ }).last().click();

      await expect(
        page.getByText(/failed|already exists|already registered/i)
      ).toBeVisible({ timeout: 10000 });
    });

    test('should show password rules on focus', async ({ page }) => {
      await page.goto('/authentication');
      await page.getByRole('button', { name: /^Register$/ }).click();

      const passwordInput = page.locator('input[name="password"]');
      await passwordInput.focus();

      await expect(page.getByText(/at least 8 characters/i)).toBeVisible();
      await expect(page.getByText(/one uppercase letter/i)).toBeVisible();
      await expect(page.getByText(/one special character/i)).toBeVisible();
    });

    test('should show TOS modal after successful registration', async ({ page }) => {
      const credentials = {
        username: `tos_${Date.now()}`,
        email: `tos_${Date.now()}@test.com`,
        password: '@TestPassword123',
        name: 'TOS Test User',
      };

      await page.goto('/authentication');
      await page.waitForLoadState('networkidle');

      await page.getByRole('button', { name: /^Register$/ }).click();

      await page.locator('input[name="username"]').fill(credentials.username);
      await page.locator('input[name="email"]').fill(credentials.email);
      await page.locator('input[name="password"]').fill(credentials.password);
      await page.locator('input[name="confirmPassword"]').fill(credentials.password);

      await page.getByRole('button', { name: /^Register$/ }).last().click();

      await expect(page.getByRole('heading', { name: /terms of service/i })).toBeVisible({ timeout: 8000 });
    });
  });

  test.describe('Login', () => {
    test('should log in an existing user successfully', async ({ page }) => {
      const credentials = await register(page);

      await logout(page);

      await login(page, credentials.email, credentials.password);

      expect(await isLoggedIn(page)).toBeTruthy();
      await expect(page.locator('text=Welcome')).toContainText(credentials.username);
    });

    test('should show error for incorrect password', async ({ page }) => {
      const credentials = await register(page);

      await logout(page);

      await page.goto('/authentication');
      await page.getByRole('button', { name: /^Login$/ }).click();

      await page.locator('input[name="email"]').fill(credentials.email);
      await page.locator('input[name="password"]').fill('WrongPassword1!');

      await page.getByRole('button', { name: /^Login$/ }).last().click();

      await expect(
        page.getByText(/incorrect|invalid|failed|error/i)
      ).toBeVisible({ timeout: 8000 });
    });

    test('should require email and password fields for login', async ({ page }) => {
      await page.goto('/authentication');

      await page.getByRole('button', { name: /^Login$/ }).click();

      await page.getByRole('button', { name: /^Login$/ }).last().click();

      await expect(page.getByText(/email is required/i)).toBeVisible();
      await expect(page.getByText(/password is required/i)).toBeVisible();
    });

    test('should allow canceling the login modal', async ({ page }) => {
      await page.goto('/authentication');

      await page.getByRole('button', { name: /^Login$/ }).click();

      await page.locator('input[name="email"]').fill('test@test.com');

      await page.getByRole('button', { name: /cancel/i }).click();

      await expect(page.locator('input[name="email"]')).not.toBeVisible();
    });
  });

  test.describe('Logout', () => {
    test('should clear session on logout', async ({ page }) => {
      await register(page);

      expect(await isLoggedIn(page)).toBeTruthy();

      await logout(page);

      expect(await isLoggedIn(page)).toBeFalsy();
    });
  });

  test.describe('Admin', () => {
    const GATEWAY_URL = process.env.GATEWAY_URL || 'http://localhost:23738';

    test.describe('Authorization', () => {
      test('should redirect non-admin user from /admin page', async ({ page }) => {
        await register(page);

        await page.goto('/admin');
        await page.waitForLoadState('networkidle');

        expect(page.url()).not.toContain('/admin');
      });

      test('should reject unauthenticated access to admin API endpoints', async ({ request }) => {
        const res = await request.get(`${GATEWAY_URL}/api/admin/profiles`);
        expect(res.status()).toBe(401);
      });

      test('should reject non-admin token from admin API endpoints', async ({ request }) => {
        const { token } = await registerViaAPI(request);

        const res = await request.get(`${GATEWAY_URL}/api/admin/profiles`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        expect(res.status()).toBe(403);
      });
    });

    test.describe('Admin Flow', () => {
      test('should allow admin to access /admin page', async ({ page, request }) => {
        const credentials = await registerViaAPI(request);
        const promoted = await promoteToAdmin(credentials.userId);
        test.skip(!promoted, 'Skipping: could not promote user to admin');

        const session = await loginViaAPI(request, credentials.email, credentials.password);
        expect(session).not.toBeNull();

        await page.context().addCookies([
          { name: 'token', value: session!.token, domain: 'localhost', path: '/' },
        ]);

        await page.goto('/admin');
        await page.waitForLoadState('networkidle');

        await expect(page.getByRole('heading', { name: /users overview/i })).toBeVisible();
        await expect(page.getByRole('heading', { name: /reports/i })).toBeVisible();
      });

      test('should allow admin to list profiles via API', async ({ request }) => {
        const credentials = await registerViaAPI(request);
        const promoted = await promoteToAdmin(credentials.userId);
        test.skip(!promoted, 'Skipping: could not promote user to admin');

        const session = await loginViaAPI(request, credentials.email, credentials.password);
        expect(session).not.toBeNull();

        const res = await request.get(`${GATEWAY_URL}/api/admin/profiles`, {
          headers: { Authorization: `Bearer ${session!.token}` },
        });

        expect(res.status()).toBe(200);
      });

      test('should reject non-admin user from admin API even after promotion attempt fails', async ({ request }) => {
        const { token } = await registerViaAPI(request);

        const res = await request.get(`${GATEWAY_URL}/api/admin/profiles`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        expect(res.status()).toBe(403);
      });
    });
  });
});
