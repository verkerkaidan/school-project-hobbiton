import { test, expect } from '@playwright/test';
import { register } from './helpers/auth';

test.describe('Profile Connection Service', () => {
  let userACredentials: { username: string; email: string; password: string };

  test.beforeEach(async ({ page }) => {
    userACredentials = await register(page);
  });

  test('should not show Follow button on own profile and display follower/following counts with modals', async ({ page }) => {
    await page.goto('/profile');
    await page.waitForLoadState('networkidle');

    await expect(page.getByRole('button', { name: /^follow$/i })).not.toBeVisible();
    await expect(page.locator('text=Followers')).toBeVisible({ timeout: 8000 });
    await expect(page.locator('text=Following')).toBeVisible();

    const followersButton = page.getByRole('button').filter({ has: page.locator('text=Followers') });
    await followersButton.click();
    const followersHeading = page.getByRole('heading', { name: 'Followers' });
    await expect(followersHeading).toBeVisible({ timeout: 5000 });
    await followersHeading.locator('..').locator('button').click();
    await expect(followersHeading).not.toBeVisible({ timeout: 5000 });

    const followingButton = page.getByRole('button').filter({ has: page.locator('text=Following') });
    await followingButton.click();
    await expect(page.getByRole('heading', { name: 'Following' })).toBeVisible({ timeout: 5000 });
  });

  test('should follow, update follower count, and unfollow another user', async ({ page, browser }) => {
    const ctx2 = await browser.newContext();
    const page2 = await ctx2.newPage();
    const secondUser = await register(page2);
    await ctx2.close();

    await page.goto(`/profile/${secondUser.username}`);
    await page.waitForLoadState('networkidle');

    const followersButton = page.getByRole('button').filter({ has: page.locator('text=Followers') });
    const beforeText = await followersButton.locator('span').first().textContent();
    const countBefore = parseInt(beforeText ?? '0', 10);

    const followButton = page.getByRole('button', { name: /^follow$/i });
    await expect(followButton).toBeVisible({ timeout: 8000 });
    await followButton.click();

    const unfollowButton = page.getByRole('button', { name: /^unfollow$/i });
    await expect(unfollowButton).toBeVisible({ timeout: 8000 });

    const afterText = await followersButton.locator('span').first().textContent();
    const countAfter = parseInt(afterText ?? '0', 10);
    expect(countAfter).toBe(countBefore + 1);

    await unfollowButton.click();
    await expect(page.getByRole('button', { name: /^follow$/i })).toBeVisible({ timeout: 8000 });
  });

  test('should block another user and hide profile content for both parties', async ({ page, browser }) => {
    const ctx2 = await browser.newContext();
    const page2 = await ctx2.newPage();
    const userB = await register(page2);

    await page.goto(`/profile/${userB.username}`);
    await page.waitForLoadState('networkidle');

    const settingsButton = page.getByRole('button', { name: /settings/i });
    await expect(settingsButton).toBeVisible({ timeout: 8000 });
    await settingsButton.click();

    const blockButton = page.getByRole('button', { name: /^block$/i });
    await expect(blockButton).toBeVisible({ timeout: 3000 });
    await blockButton.click();

    await expect(page.getByRole('button', { name: /user blocked/i })).toBeVisible({ timeout: 8000 });

    await expect(page.getByText(/profile is hidden because you have blocked/i)).toBeVisible({ timeout: 8000 });
    await expect(page.getByText(/posts are hidden because you have blocked/i)).toBeVisible({ timeout: 8000 });

    await page2.goto(`/profile/${userACredentials.username}`);
    await page2.locator('h1').first().waitFor({ state: 'visible', timeout: 10000 });
    await expect(page2.getByText(/profile is hidden because you have blocked/i)).toBeVisible({ timeout: 8000 });
    await expect(page2.getByText(/posts are hidden because you have blocked/i)).toBeVisible({ timeout: 8000 });

    await ctx2.close();

    // Blocked user should appear in the Blocked Users list in settings
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const blockedSection = page.getByRole('button', { name: /blocked users/i });
    await expect(blockedSection).toBeVisible({ timeout: 8000 });
    await blockedSection.click();

    await expect(page.getByText(userB.username)).toBeVisible({ timeout: 8000 });
  });

  test('should report another user from their profile', async ({ page, browser }) => {
    const ctx2 = await browser.newContext();
    const page2 = await ctx2.newPage();
    const userB = await register(page2);
    await ctx2.close();

    await page.goto(`/profile/${userB.username}`);
    await page.waitForLoadState('networkidle');

    const settingsButton = page.getByRole('button', { name: /settings/i });
    await expect(settingsButton).toBeVisible({ timeout: 8000 });
    await settingsButton.click();

    const reportButton = page.getByRole('button', { name: /^report$/i });
    await expect(reportButton).toBeVisible({ timeout: 3000 });
    await reportButton.click();

    await expect(page.getByText(new RegExp(`Report.*@${userB.username}`, 'i'))).toBeVisible({ timeout: 5000 });

    const descriptionInput = page.locator('textarea[placeholder="Provide additional context..."]');
    await descriptionInput.fill('E2E test report — please ignore.');

    await page.getByRole('button', { name: /submit report/i }).click();

    await expect(page.locator('textarea[placeholder="Provide additional context..."]')).not.toBeVisible({ timeout: 8000 });
  });
});
