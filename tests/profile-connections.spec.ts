import { test, expect } from '@playwright/test';
import { register } from './helpers/auth';

test.describe('Profile Connection Service', () => {
  let userACredentials: { username: string; email: string; password: string };

  test.beforeEach(async ({ page }) => {
    userACredentials = await register(page);
  });

  test('should display follow button when viewing another user profile', async ({ page, browser }) => {
    // Register a second user in a separate context to get their username
    const ctx2 = await browser.newContext();
    const page2 = await ctx2.newPage();
    const secondUser = await register(page2);
    await ctx2.close();

    // Navigate to the second user's profile as the first user
    await page.goto(`/profile/${secondUser.username}`);
    await page.waitForLoadState('networkidle');

    const followButton = page.getByRole('button', { name: /^follow$/i });
    await expect(followButton).toBeVisible({ timeout: 8000 });
  });

  test('should follow another user and button changes to Unfollow', async ({ page, browser }) => {
    const ctx2 = await browser.newContext();
    const page2 = await ctx2.newPage();
    const secondUser = await register(page2);
    await ctx2.close();

    await page.goto(`/profile/${secondUser.username}`);
    await page.waitForLoadState('networkidle');

    const followButton = page.getByRole('button', { name: /^follow$/i });
    await expect(followButton).toBeVisible({ timeout: 8000 });
    await followButton.click();

    // Button should update to "Unfollow"
    const unfollowButton = page.getByRole('button', { name: /^unfollow$/i });
    await expect(unfollowButton).toBeVisible({ timeout: 8000 });
  });

  test('should unfollow a user after following them', async ({ page, browser }) => {
    const ctx2 = await browser.newContext();
    const page2 = await ctx2.newPage();
    const secondUser = await register(page2);
    await ctx2.close();

    await page.goto(`/profile/${secondUser.username}`);
    await page.waitForLoadState('networkidle');

    // Follow
    const followButton = page.getByRole('button', { name: /^follow$/i });
    await expect(followButton).toBeVisible({ timeout: 8000 });
    await followButton.click();

    const unfollowButton = page.getByRole('button', { name: /^unfollow$/i });
    await expect(unfollowButton).toBeVisible({ timeout: 8000 });

    // Unfollow
    await unfollowButton.click();
    const followAgain = page.getByRole('button', { name: /^follow$/i });
    await expect(followAgain).toBeVisible({ timeout: 8000 });
  });

  test('should display follower and following counts on profile', async ({ page }) => {
    await page.goto('/profile');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('text=Followers')).toBeVisible({ timeout: 8000 });
    await expect(page.locator('text=Following')).toBeVisible();
  });

  test('should update follower count after following a user', async ({ page, browser }) => {
    const ctx2 = await browser.newContext();
    const page2 = await ctx2.newPage();
    const secondUser = await register(page2);
    await ctx2.close();

    // View the second user's profile and record their follower count
    await page.goto(`/profile/${secondUser.username}`);
    await page.waitForLoadState('networkidle');

    const followersButton = page.getByRole('button').filter({ has: page.locator('text=Followers') });
    const beforeText = await followersButton.locator('span').first().textContent();
    const countBefore = parseInt(beforeText ?? '0', 10);

    // Follow
    const followButton = page.getByRole('button', { name: /^follow$/i });
    await expect(followButton).toBeVisible({ timeout: 8000 });
    await followButton.click();

    // Wait for the button to change to "Unfollow" — confirms the follow completed and state updated
    await expect(page.getByRole('button', { name: /^unfollow$/i })).toBeVisible({ timeout: 8000 });

    const afterText = await followersButton.locator('span').first().textContent();
    const countAfter = parseInt(afterText ?? '0', 10);

    expect(countAfter).toBe(countBefore + 1);
  });

  test('should open followers modal when clicking Followers count', async ({ page }) => {
    await page.goto('/profile');
    await page.waitForLoadState('networkidle');

    const followersButton = page.getByRole('button').filter({ has: page.locator('text=Followers') });
    await expect(followersButton).toBeVisible({ timeout: 8000 });
    await followersButton.click();

    // The modal renders an h2 with the title
    await expect(page.getByRole('heading', { name: 'Followers' })).toBeVisible({ timeout: 5000 });
  });

  test('should open following modal when clicking Following count', async ({ page }) => {
    await page.goto('/profile');
    await page.waitForLoadState('networkidle');

    const followingButton = page.getByRole('button').filter({ has: page.locator('text=Following') });
    await expect(followingButton).toBeVisible({ timeout: 8000 });
    await followingButton.click();

    await expect(page.getByRole('heading', { name: 'Following' })).toBeVisible({ timeout: 5000 });
  });

  test('should not show Follow button on own profile', async ({ page }) => {
    await page.goto('/profile');
    await page.waitForLoadState('networkidle');

    const followButton = page.getByRole('button', { name: /^follow$/i });
    await expect(followButton).not.toBeVisible();
  });

  test('should block another user from their profile', async ({ page, browser }) => {
    const ctx2 = await browser.newContext();
    const page2 = await ctx2.newPage();
    const secondUser = await register(page2);
    await ctx2.close();

    await page.goto(`/profile/${secondUser.username}`);
    await page.waitForLoadState('networkidle');

    // Open the settings dropdown (three-dot menu)
    const settingsButton = page.getByRole('button', { name: /settings/i });
    await expect(settingsButton).toBeVisible({ timeout: 8000 });
    await settingsButton.click();

    // Click Block
    const blockButton = page.getByRole('button', { name: /^block$/i });
    await expect(blockButton).toBeVisible({ timeout: 3000 });
    await blockButton.click();

    // After blocking, the follow button should show "User blocked" (dropdown closes automatically)
    const userBlockedText = page.getByRole('button', { name: /user blocked/i });
    await expect(userBlockedText).toBeVisible({ timeout: 8000 });
  });

  /** Helper: block targetPage's user from blockerPage. Navigates to the target profile and blocks them. */
  async function blockUser(blockerPage: import('@playwright/test').Page, targetUsername: string) {
    await blockerPage.goto(`/profile/${targetUsername}`);
    await blockerPage.waitForLoadState('networkidle');

    const settingsButton = blockerPage.getByRole('button', { name: /settings/i });
    await expect(settingsButton).toBeVisible({ timeout: 8000 });
    await settingsButton.click();

    const blockButton = blockerPage.getByRole('button', { name: /^block$/i });
    await expect(blockButton).toBeVisible({ timeout: 3000 });
    await blockButton.click();

    // After blocking, the follow button changes to "User blocked" (settings dropdown closes automatically)
    const userBlockedText = blockerPage.getByRole('button', { name: /user blocked/i });
    await expect(userBlockedText).toBeVisible({ timeout: 8000 });
  }

  test('blocker cannot view blocked user\'s profile content', async ({ page, browser }) => {
    // User A (page) blocks User B (page2). User A visiting User B's profile should see hidden content.
    const ctx2 = await browser.newContext();
    const page2 = await ctx2.newPage();
    const userB = await register(page2);
    await ctx2.close();

    await blockUser(page, userB.username);

    // After blocking, bio area should show hidden-content message
    await expect(page.getByText(/profile is hidden because you have blocked/i)).toBeVisible({ timeout: 8000 });

    // Posts section should also be hidden
    await expect(page.getByText(/posts are hidden because you have blocked/i)).toBeVisible({ timeout: 8000 });
  });

  test('blocked user cannot view blocker\'s profile content', async ({ page, browser }) => {
    // User A (page) blocks User B (page2). User B visiting User A's profile should see hidden content.
    const ctx2 = await browser.newContext();
    const page2 = await ctx2.newPage();
    const userB = await register(page2);

    // User A blocks User B
    await blockUser(page, userB.username);

    // User B navigates to User A's profile
    await page2.goto(`/profile/${userACredentials.username}`);
    const userAProfileHeading = page2.locator('h1').first();
    await expect(userAProfileHeading).toBeVisible({ timeout: 10000 });

    // User B (the blocked party) should see the profile as blocked/restricted
    await expect(page2.getByText(/profile is hidden because you have blocked/i)).toBeVisible({ timeout: 8000 });

    // User B should NOT be able to see User A's posts
    await expect(page2.getByText(/posts are hidden because you have blocked/i)).toBeVisible({ timeout: 8000 });

    await ctx2.close();
  });

  /** Helper: creates a minimal "Other" post from the home feed and waits for the profile redirect. */
  async function createPost(page: import('@playwright/test').Page, title: string): Promise<void> {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const createButton = page.getByRole('button', { name: /create new post/i });
    await expect(createButton).toBeVisible({ timeout: 10000 });
    await createButton.click();

    await page.waitForSelector('text=Create Post', { timeout: 10000 });

    // Open the custom post-type dropdown and select "Other"
    await page.locator('label:has-text("Post Type") + button').click();
    await page.getByRole('button', { name: '✨ Other', exact: true }).click();

    await page.locator('input[name="title"]').fill(title);
    await page.locator('textarea[name="description"]').fill('E2E test post – feed block filter');

    await page.getByRole('button', { name: /create post/i }).click();
    await page.waitForSelector('text=Create Post', { state: 'hidden', timeout: 30000 });
  }

  // test('blocker cannot see blocked user\'s posts in the feed', async ({ browser }) => {
  //   const ctxA = await browser.newContext();
  //   const ctxB = await browser.newContext();
  //   const pageA = await ctxA.newPage();
  //   const pageB = await ctxB.newPage();
  //
  //   const userA = await register(pageA);
  //   const userB = await register(pageB);
  //
  //   const postTitle = `E2E-block-feed-${Date.now()}`;
  //
  //   await createPost(pageA, postTitle);
  //
  //   // Post should appear in User B's feed before blocking
  //   await pageB.goto('/');
  //   await pageB.waitForLoadState('networkidle');
  //   await pageB.reload();
  //   await expect(pageB.getByText(postTitle)).toBeVisible({ timeout: 15000 });
  //
  //   // User B blocks User A
  //   await blockUser(pageB, userA.username);
  //
  //   // Post must no longer appear in User B's feed
  //   await pageB.goto('/');
  //   await pageB.waitForLoadState('networkidle');
  //   await pageB.waitForTimeout(2000);
  //   await expect(pageB.getByText(postTitle)).not.toBeVisible();
  //
  //   await ctxA.close();
  //   await ctxB.close();
  // });

  // test('blocked user cannot see blocker\'s posts in the feed', async ({ browser }) => {
  //   const ctxA = await browser.newContext();
  //   const ctxB = await browser.newContext();
  //   const pageA = await ctxA.newPage();
  //   const pageB = await ctxB.newPage();
  //
  //   const userA = await register(pageA);
  //   const userB = await register(pageB);
  //
  //   const postTitle = `E2E-blocked-feed-${Date.now()}`;
  //
  //   await createPost(pageA, postTitle);
  //
  //   // Post should appear in User B's feed before blocking
  //   await pageB.goto('/');
  //   await pageB.waitForLoadState('networkidle');
  //   await pageB.reload();
  //   await expect(pageB.getByText(postTitle)).toBeVisible({ timeout: 15000 });
  //
  //   // User A blocks User B
  //   await blockUser(pageA, userB.username);
  //
  //   // Post must no longer appear in User B's feed
  //   await pageB.goto('/');
  //   await pageB.waitForLoadState('networkidle');
  //   await pageB.waitForTimeout(2000);
  //   await expect(pageB.getByText(postTitle)).not.toBeVisible();
  //
  //   await ctxA.close();
  //   await ctxB.close();
  // });
});
