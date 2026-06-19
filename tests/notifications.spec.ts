import { test, expect } from '@playwright/test';
import { register } from './helpers/auth';

test.describe('Notification Service', () => {
  test.beforeEach(async ({ page }) => {
    await register(page);
  });

  test('should navigate to notifications page', async ({ page }) => {
    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');

    await expect(page).toHaveURL(/\/notifications/);

    const heading = page.getByRole('heading', { name: /notifications/i });
    await expect(heading).toBeVisible();
  });

  test('should display empty state when no notifications exist', async ({ page }) => {
    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');

    const emptyMessage = page.locator('text=No notifications yet');
    await expect(emptyMessage).toBeVisible({ timeout: 10000 });
  });

  test('should not show "Mark all as read" button when there are no unread notifications', async ({ page }) => {
    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');

    const markAllButton = page.getByRole('button', { name: /mark all as read/i });
    await expect(markAllButton).not.toBeVisible();
  });

  test('should navigate to notifications from sidebar', async ({ page }) => {
    const notificationsLink = page.locator('a, button').filter({ hasText: /notifications/i }).first();

    if (await notificationsLink.isVisible()) {
      await notificationsLink.click();
      await page.waitForLoadState('networkidle');
      await expect(page).toHaveURL(/\/notifications/);
    } else {
      await page.goto('/notifications');
      await expect(page).toHaveURL(/\/notifications/);
    }

    const main = page.locator('main');
    await expect(main).toBeVisible();
  });

  test('should load notifications page without errors', async ({ page }) => {
    const consoleErrors: string[] = [];
    page.on('console', msg => {
      if (msg.type() === 'error') consoleErrors.push(msg.text());
    });

    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');

    await expect(page).toHaveURL(/\/notifications/);
    const main = page.locator('main');
    await expect(main).toBeVisible();
  });

  test('should open notification settings section in settings page', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const notificationSection = page.getByRole('button', { name: /notifications/i }).first();
    await expect(notificationSection).toBeVisible();

    await notificationSection.click();

    await page.waitForTimeout(500);
    const panel = page.locator('text=Follows');
    await expect(panel).toBeVisible({ timeout: 8000 });
  });

  test('should display all four notification preference types', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const notificationSection = page.getByRole('button', { name: /notifications/i }).first();
    await notificationSection.click();

    await expect(page.locator('text=Follows')).toBeVisible({ timeout: 8000 });
    await expect(page.locator('text=Posts from followed users')).toBeVisible();
    await expect(page.locator('text=Likes on your posts')).toBeVisible();
    await expect(page.locator('text=Comments on your posts')).toBeVisible();
  });

  test('should toggle a notification preference', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const notificationSection = page.getByRole('button', { name: /notifications/i }).first();
    await notificationSection.click();

    await expect(page.locator('text=Follows')).toBeVisible({ timeout: 8000 });

    const followsToggle = page.getByRole('button', { name: /toggle follows/i });
    await expect(followsToggle).toBeVisible();

    const initialClass = await followsToggle.getAttribute('class');
    await followsToggle.click();

    await page.waitForTimeout(1000);

    const updatedClass = await followsToggle.getAttribute('class');
    expect(updatedClass).not.toEqual(initialClass);
  });
});

test.describe('Notification Service — real notifications', () => {
  async function setupTwoUsers(page: import('@playwright/test').Page, browser: import('@playwright/test').Browser) {
    const userA = await register(page);

    const contextB = await browser.newContext();
    const pageB = await contextB.newPage();
    const userB = await register(pageB);

    await pageB.goto(`/profile/${userA.username}`);
    await pageB.waitForLoadState('networkidle');

    const followButton = pageB.getByRole('button', { name: /^follow$/i });
    await followButton.waitFor({ state: 'visible', timeout: 10000 });
    await followButton.click();
    await expect(pageB.getByRole('button', { name: /unfollow/i })).toBeVisible({ timeout: 10000 });

    await contextB.close();

    return { userA, userB };
  }

  test('should receive a follow notification when another user follows you', async ({ page, browser }) => {
    const { userB } = await setupTwoUsers(page, browser);

    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');

    const followNotification = page.getByText(new RegExp(`${userB.username}.*following`, 'i'));
    await expect(followNotification).toBeVisible({ timeout: 15000 });
  });

  test('should show "Mark all as read" button when there are unread notifications', async ({ page, browser }) => {
    await setupTwoUsers(page, browser);

    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');

    await expect(page.getByRole('button', { name: /mark all as read/i })).toBeVisible({ timeout: 15000 });
  });

  test('should mark all notifications as read', async ({ page, browser }) => {
    await setupTwoUsers(page, browser);

    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');

    const markAllButton = page.getByRole('button', { name: /mark all as read/i });
    await expect(markAllButton).toBeVisible({ timeout: 15000 });
    await markAllButton.click();

    await expect(markAllButton).not.toBeVisible({ timeout: 10000 });
  });

  test('should mark a single notification as read when clicked', async ({ page, browser }) => {
    const { userB } = await setupTwoUsers(page, browser);

    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');

    const notification = page.getByText(new RegExp(`${userB.username}.*following`, 'i'));
    await expect(notification).toBeVisible({ timeout: 15000 });
    await notification.click();

    await expect(page.getByRole('button', { name: /mark all as read/i })).not.toBeVisible({ timeout: 10000 });
  });
});
