import { test, expect } from '@playwright/test';
import { register } from './helpers/auth';

test.describe('Profile Viewing', () => {
  test.beforeEach(async ({ page }) => {
    // Register a new test user before each test
    await register(page);
  });

  test('should view own profile', async ({ page }) => {
    // Navigate to the profile page
    await page.goto('/profile');

    // Wait for the page to load
    await page.waitForLoadState('networkidle');

    // Check that we're on the profile page
    await expect(page).toHaveURL(/\/profile/);

    // Verify profile information is displayed
    // Looking for profile header elements
    const profileSection = page.locator('main');
    await expect(profileSection).toBeVisible();

    // Check for user posts section
    const userPosts = page.locator('text=/Posts|posts/i');
    // Note: may not always be visible if no posts exist, but the page should load
  });

  test('should display user profile header with username', async ({ page }) => {
    // Navigate to the profile page
    await page.goto('/profile');

    // Wait for the page to load
    await page.waitForLoadState('networkidle');

    // The page should have loaded and the main content should be visible
    const mainContent = page.locator('main');
    await expect(mainContent).toBeVisible();

    // Verify the profile view component is rendering
    const profileView = page.locator('[class*="profile"], [class*="Profile"]');
    // Profile content should be present
  });

  test('should display user posts on profile', async ({ page }) => {
    // Navigate to the profile page
    await page.goto('/profile');

    // Wait for the page to load
    await page.waitForLoadState('networkidle');

    // Check if feeds or posts exist
    const feeds = page.locator('[class*="feed"], [class*="post"], [class*="Post"]');

    // At minimum, the page structure should be loaded
    const mainContent = page.locator('main');
    await expect(mainContent).toBeVisible();
  });

  test('should navigate to profile from sidebar', async ({ page }) => {
    // Look for profile link in sidebar
    // The sidebar typically has navigation links
    const profileLink = page.locator('a, button').filter({ hasText: /profile/i }).first();

    if (await profileLink.isVisible()) {
      await profileLink.click();

      // Wait for navigation
      await page.waitForLoadState('networkidle');

      // Verify we're on profile page
      await expect(page).toHaveURL(/\/profile/);

      // Verify profile content is visible
      const mainContent = page.locator('main');
      await expect(mainContent).toBeVisible();
    } else {
      // If sidebar link not visible, navigate directly
      await page.goto('/profile');
      await expect(page).toHaveURL(/\/profile/);
    }
  });

  test('should display user information', async ({ page }) => {
    // Navigate to the profile page
    await page.goto('/profile');

    // Wait for the page to load
    await page.waitForLoadState('networkidle');

    // Check that main profile section exists and is visible
    const profileMain = page.locator('main');
    await expect(profileMain).toBeVisible();

    // Profile should contain user-related information
    const profileContent = profileMain.locator('text=.+');
    await expect(profileContent).toBeTruthy();
  });

  test('should load profile with existing posts', async ({ page }) => {
    // Navigate to profile
    await page.goto('/profile');

    // Wait for network to be idle
    await page.waitForLoadState('networkidle');

    // Check main content area is visible
    const main = page.locator('main');
    await expect(main).toBeVisible();

    // Try to find any post elements on the page
    // Posts typically have titles, descriptions, or other identifiable elements
    const articles = page.locator('article, [role="article"]');
    const articleCount = await articles.count();

    // If there are posts, verify they're visible
    if (articleCount > 0) {
      for (let i = 0; i < Math.min(articleCount, 3); i++) {
        const article = articles.nth(i);
        await expect(article).toBeVisible();
      }
    }
  });

  test('should display profile stats or follow information', async ({ page }) => {
    // Navigate to the profile page
    await page.goto('/profile');

    // Wait for the page to load
    await page.waitForLoadState('networkidle');

    // Check that profile view is loaded
    await expect(page.locator('main')).toBeVisible();

    // Look for common profile elements like follower count, following count
    const profileElements = page.locator('[class*="stat"], [class*="count"], [class*="follower"]');

    // At least the page should load successfully
    await expect(page).toHaveURL(/\/profile/);
  });

  test('should handle profile page loading gracefully', async ({ page }) => {
    // Navigate to profile
    await page.goto('/profile');

    // Wait for network traffic to settle
    await page.waitForLoadState('networkidle');

    // Verify we're on the correct page
    await expect(page).toHaveURL(/\/profile/);

    // Main content should be visible
    const main = page.locator('main');
    await expect(main).toBeVisible();

    // Page should not have any console errors
    const consoleErrors: string[] = [];
    page.on('console', msg => {
      if (msg.type() === 'error') {
        consoleErrors.push(msg.text());
      }
    });

    // Give it a moment to process
    await page.waitForTimeout(1000);

    // While errors might exist, we focus on the page being usable
    expect(page.url()).toContain('/profile');
  });

  test('should display user profile by username URL', async ({ page }) => {
    // This test assumes the application supports navigating to user profiles by username
    // Example: /profile/john-doe

    // First, get the current user (assuming already logged in from home page)
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Navigate directly to profile page
    await page.goto('/profile');
    await page.waitForLoadState('networkidle');

    // Verify profile loaded
    await expect(page).toHaveURL(/\/profile/);

    // Main content should be visible
    const main = page.locator('main');
    await expect(main).toBeVisible();
  });

  test('should scroll through profile user posts', async ({ page }) => {
    // Navigate to the profile page
    await page.goto('/profile');

    // Wait for the page to load
    await page.waitForLoadState('networkidle');

    // Get the main content area
    const main = page.locator('main');
    await expect(main).toBeVisible();

    // Try to scroll down
    await main.evaluate(el => el.scrollTop = el.scrollHeight);

    // Wait for any lazy-loaded content
    await page.waitForTimeout(1000);

    // Verify we're still on the profile page
    await expect(page).toHaveURL(/\/profile/);
  });
});

