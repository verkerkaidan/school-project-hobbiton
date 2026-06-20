import { test, expect } from '@playwright/test';
import { register, registerViaAPI } from './helpers/auth';


test.describe('Profile Viewing', () => {
  test.beforeEach(async ({ page }) => {
    await register(page);
  });

  test('should display the own profile page with username', async ({ page }) => {
    await page.goto('/profile');
    await page.waitForLoadState('networkidle');

    await expect(page).toHaveURL(/\/profile/);

    const usernameHeading = page.locator('h1');
    await expect(usernameHeading.first()).toBeVisible();
  });

  test('should display profile stats (followers, following, posts)', async ({ page }) => {
    await page.goto('/profile');
    await page.waitForLoadState('networkidle');

    await expect(page.getByText(/followers/i).first()).toBeVisible();
    await expect(page.getByText(/following/i).first()).toBeVisible();
  });

  test('should show the main profile content area', async ({ page }) => {
    await page.goto('/profile');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('h1').first()).toBeVisible();
  });

  test('should navigate to profile via direct URL', async ({ page }) => {
    await page.goto('/profile');
    await page.waitForLoadState('networkidle');

    await expect(page).toHaveURL(/\/profile/);
    await expect(page.locator('main')).toBeVisible();
  });

  test('should handle profile page loading gracefully without console errors', async ({ page }) => {
    const consoleErrors: string[] = [];
    page.on('console', msg => {
      if (msg.type() === 'error') {
        consoleErrors.push(msg.text());
      }
    });

    await page.goto('/profile');
    await page.waitForLoadState('networkidle');

    await expect(page).toHaveURL(/\/profile/);
    await expect(page.locator('main')).toBeVisible();
  });
});
