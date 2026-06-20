import { test, expect } from '@playwright/test';
import { register } from './helpers/auth';

test.describe('Notification Service', () => {
  test.beforeEach(async ({ page }) => {
    await register(page);
  });

  test('should display empty notifications page with no mark-all button', async ({ page }) => {
    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');

    await expect(page).toHaveURL(/\/notifications/);
    await expect(page.getByRole('heading', { name: /notifications/i })).toBeVisible();
    await expect(page.locator('text=No notifications yet')).toBeVisible({ timeout: 10000 });
    await expect(page.getByRole('button', { name: /mark all as read/i })).not.toBeVisible();
  });

  test('should display and toggle notification preferences in settings', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const notificationSection = page.getByRole('button', { name: /notifications/i }).first();
    await notificationSection.click();

    await expect(page.locator('text=Follows')).toBeVisible({ timeout: 8000 });
    await expect(page.locator('text=Posts from followed users')).toBeVisible();
    await expect(page.locator('text=Likes on your posts')).toBeVisible();
    await expect(page.locator('text=Comments on your posts')).toBeVisible();

    const followsToggle = page.getByRole('button', { name: /toggle follows/i });
    const initialClass = await followsToggle.getAttribute('class');
    await followsToggle.click();
    await page.waitForTimeout(1000);
    const updatedClass = await followsToggle.getAttribute('class');
    expect(updatedClass).not.toEqual(initialClass);
  });
});

test.describe('Notification Service — real notifications', () => {
  async function createPost(page: import('@playwright/test').Page, title: string) {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const createButton = page.getByRole('button', { name: /create new post/i });
    await expect(createButton).toBeVisible({ timeout: 10000 });
    await createButton.click();

    await page.waitForSelector('text=Create Post', { timeout: 10000 });

    await page.locator('label:has-text("Post Type") + button').click();
    await page.getByRole('button', { name: '✨ Other', exact: true }).click();

    await page.locator('input[name="title"]').fill(title);
    await page.locator('textarea[name="description"]').fill('E2E test post');

    await page.getByRole('button', { name: /create post/i }).click();
    await page.waitForSelector('text=Create Post', { state: 'hidden', timeout: 30000 });
  }

  test('should receive follow notification and mark all as read', async ({ page, browser }) => {
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

    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');

    await expect(page.getByText(new RegExp(`${userB.username}.*following`, 'i'))).toBeVisible({ timeout: 15000 });

    const markAllButton = page.getByRole('button', { name: /mark all as read/i });
    await expect(markAllButton).toBeVisible({ timeout: 15000 });
    await markAllButton.click();
    await expect(markAllButton).not.toBeVisible({ timeout: 10000 });
  });

  test('should receive notifications for likes, comments, and followed user posts', async ({ page, browser }) => {
    const userA = await register(page);
    const postTitle = `E2E-notif-${Date.now()}`;
    await createPost(page, postTitle);

    const contextB = await browser.newContext();
    const pageB = await contextB.newPage();
    const userB = await register(pageB);

    // User B likes and comments on User A's post
    await pageB.goto(`/profile/${userA.username}`);
    await pageB.waitForLoadState('networkidle');

    await pageB.getByRole('heading', { name: postTitle }).click();
    await pageB.waitForURL('**/posts/**', { timeout: 10000 });

    const likeIcon = pageB.locator('svg.cursor-pointer').first();
    await expect(likeIcon).toBeVisible({ timeout: 8000 });
    await likeIcon.click();
    await pageB.waitForTimeout(1000);

    const commentInput = pageB.locator('textarea[placeholder="Write a supportive comment..."]');
    await expect(commentInput).toBeVisible({ timeout: 8000 });
    await commentInput.fill('Great post!');
    await pageB.getByRole('button', { name: /post comment/i }).click();
    await pageB.waitForTimeout(1000);

    // User A follows User B, then User B publishes a post
    await page.goto(`/profile/${userB.username}`);
    await page.waitForLoadState('networkidle');

    const followButton = page.getByRole('button', { name: /^follow$/i });
    await followButton.waitFor({ state: 'visible', timeout: 10000 });
    await followButton.click();
    await expect(page.getByRole('button', { name: /unfollow/i })).toBeVisible({ timeout: 10000 });

    // Subscribe to User B's post notifications (bell icon appears after following)
    const bellButton = page.getByRole('button', { name: /turn on post notifications/i });
    await expect(bellButton).toBeVisible({ timeout: 5000 });
    await bellButton.click();
    await expect(page.getByRole('button', { name: /turn off post notifications/i })).toBeVisible({ timeout: 5000 });

    const followedPostTitle = `E2E-followed-${Date.now()}`;
    await createPost(pageB, followedPostTitle);

    await contextB.close();

    // User A should see all three notification types
    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');

    await expect(page.getByText(/liked your post/i)).toBeVisible({ timeout: 15000 });
    await expect(page.getByText(/commented on your post/i)).toBeVisible({ timeout: 15000 });
    await expect(page.getByText(new RegExp(`${userB.username}.*published`, 'i'))).toBeVisible({ timeout: 15000 });
  });
});
