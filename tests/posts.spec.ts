import { test, expect } from '@playwright/test';
import { login, logout, register } from './helpers/auth';

let userA: { username: string; email: string; password: string; name: string };
let userB: { username: string; email: string; password: string; name: string };

test.beforeAll(async ({ browser }) => {
  const [pageA, pageB] = await Promise.all([
    browser.newPage(),
    browser.newPage(),
  ]);

  [userA, userB] = await Promise.all([
    register(pageA),
    register(pageB),
  ]);

  await Promise.all([pageA.close(), pageB.close()]);
});

test.describe('Posts', () => {

  test('MUST HAVE: Gebruiker maakt een post aan', async ({ page }) => {
    await login(page, userA.email, userA.password);
    await page.goto('/', { waitUntil: 'domcontentloaded', timeout: 60000 });

    await page.getByRole('button', { name: /create new post/i }).click();
    await expect(page.getByRole('heading', { name: 'New Post' })).toBeVisible({ timeout: 5000 });

    await page.locator('input[name="title"]').fill('My new post');
    await page.locator('textarea[name="description"]').fill('Description of my new post');
    await page.locator('input[name="totalDuration"]').fill('30');

    await page.getByRole('button', { name: /add exercise/i }).click();
    await page.locator('input[placeholder="Exercise name"]').fill('Bench Press');
    await page.locator('input[placeholder="12"]').fill('10');
    await page.locator('input[placeholder="40"]').fill('135');
    await page.locator('input[placeholder="7"]').fill('8');

    await page.getByRole('button', { name: /create post/i }).click();
    await page.waitForURL('**/profile', { timeout: 15000 });

    await expect(page.getByRole('heading', { name: 'My new post' })).toBeVisible({ timeout: 10000 });
  });

  test('MUST HAVE: Gebruiker koppelt een hobby aan een post', async ({ page }) => {
    await login(page, userA.email, userA.password);
    await page.goto('/', { waitUntil: 'domcontentloaded', timeout: 60000 });

    await page.getByRole('button', { name: /create new post/i }).click();
    await expect(page.getByRole('heading', { name: 'New Post', exact: true })).toBeVisible({ timeout: 5000 });

    await page.locator('input[name="title"]').fill('Post with linked hobby');
    await page.locator('textarea[name="description"]').fill('This post has a linked hobby item');
    await page.locator('input[name="totalDuration"]').fill('30');

    await page.getByRole('button', { name: /add exercise/i }).click();
    await page.locator('input[placeholder="Exercise name"]').fill('Bench Press');
    await page.locator('input[placeholder="12"]').fill('10');
    await page.locator('input[placeholder="40"]').fill('135');
    await page.locator('input[placeholder="7"]').fill('8');

    await page.getByRole('button', { name: /add item/i }).click();
    await page.getByRole('button', { name: '🏋️ Exercise' }).click();
    const searchInput = page.locator('input[placeholder*="Type a"]');
    await expect(searchInput).toBeVisible({ timeout: 3000 });
    await searchInput.fill('bench');

    await page.getByRole('button', { name: /^Search$/i }).click();

    const firstResult = page.locator('ul li').first();
    const hasResult = await firstResult.isVisible({ timeout: 5000 }).catch(() => false);
    if (hasResult) {
      await firstResult.click();
    }

    await page.getByRole('button', { name: /create post/i }).click();
    await page.waitForURL('**/profile', { timeout: 15000 });

    await expect(page.getByRole('heading', { name: 'Post with linked hobby' })).toBeVisible({ timeout: 5000 });
  });

  test('MUST HAVE: Gebruiker verwijdert een eigen post (eigen userId)', async ({ page }) => {
    await login(page, userA.email, userA.password);
    await page.goto('/', { waitUntil: 'domcontentloaded', timeout: 60000 });

    await page.getByRole('button', { name: /create new post/i }).click();
    await expect(page.getByRole('heading', { name: 'New Post', exact: true })).toBeVisible({ timeout: 5000 });

    await page.locator('input[name="title"]').fill('Post to delete');
    await page.locator('textarea[name="description"]').fill('This post will be deleted');
    await page.locator('input[name="totalDuration"]').fill('30');
    await page.getByRole('button', { name: /add exercise/i }).click();
    await page.locator('input[placeholder="Exercise name"]').fill('Squat');
    await page.locator('input[placeholder="12"]').fill('8');
    await page.locator('input[placeholder="40"]').fill('185');
    await page.locator('input[placeholder="7"]').fill('7');

    await page.getByRole('button', { name: /create post/i }).click();
    await page.waitForURL('**/profile', { timeout: 15000 });

    await page.getByText('Post to delete').first().click();
    await page.waitForURL('**/posts/**', { timeout: 10000 });

    await page.getByRole('button', { name: /^Delete$/i }).click();
    await expect(page.getByText('Are you sure')).toBeVisible({ timeout: 5000 });
    await page.getByRole('button', { name: /Delete Post/i }).click();

    // Detect backend error before waiting for redirect
    await expect(page.getByText(/something went wrong/i)).not.toBeVisible({ timeout: 5000 })
      .catch(() => { throw new Error('Delete failed: backend returned an error'); });

    await page.waitForURL('**/profile', { timeout: 10000 });
    await expect(page.getByRole('heading', { name: 'Post to delete', exact: true })).not.toBeVisible({ timeout: 5000 });

  });

  test('MUST HAVE: Gebruiker probeert post van ander te verwijderen (403 Forbidden)', async ({ page }) => {
    await login(page, userA.email, userA.password);
    await page.goto('/', { waitUntil: 'domcontentloaded', timeout: 60000 });

    await page.getByRole('button', { name: /create new post/i }).click();
    await expect(page.getByRole('heading', { name: 'New Post', exact: true })).toBeVisible({ timeout: 5000 });

    await page.locator('input[name="title"]').fill("User A's post");
    await page.locator('textarea[name="description"]').fill("This is user A's post");
    await page.locator('input[name="totalDuration"]').fill('30');

    await page.getByRole('button', { name: /add exercise/i }).click();
    await page.locator('input[placeholder="Exercise name"]').fill('Deadlift');
    await page.locator('input[placeholder="12"]').fill('5');
    await page.locator('input[placeholder="40"]').fill('315');
    await page.locator('input[placeholder="7"]').fill('9');

    await page.getByRole('button', { name: /create post/i }).click();
    await page.waitForURL('**/profile', { timeout: 15000 });

    await page.getByText("User A's post").first().click();
    await page.waitForURL('**/posts/**', { timeout: 10000 });
    const postUrl = page.url();

    await logout(page);
    await login(page, userB.email, userB.password);
    await page.goto(postUrl, { waitUntil: 'domcontentloaded', timeout: 60000 });

    await expect(page.getByRole('button', { name: /^Delete$/i })).not.toBeVisible({ timeout: 5000 });
  });

  test('MUST HAVE: Gebruiker maakt een GYM post aan', async ({ page }) => {
    await login(page, userA.email, userA.password);
    await page.goto('/', { waitUntil: 'domcontentloaded', timeout: 60000 });

    await page.getByRole('button', { name: /create new post/i }).click();
    await expect(page.getByRole('heading', { name: 'New Post', exact: true })).toBeVisible({ timeout: 5000 });

    await page.locator('input[name="title"]').fill('Morning Gym Session');
    await page.locator('textarea[name="description"]').fill('Great workout today');
    await page.locator('input[name="totalDuration"]').fill('60');

    await page.getByRole('button', { name: /add exercise/i }).click();
    await page.locator('input[placeholder="Exercise name"]').fill('Bench Press');
    await page.locator('input[placeholder="12"]').fill('10');
    await page.locator('input[placeholder="40"]').fill('225');
    await page.locator('input[placeholder="7"]').fill('8');

    await page.getByRole('button', { name: /create post/i }).click();
    await page.waitForURL('**/profile', { timeout: 15000 });

    await expect(page.getByText('Morning Gym Session')).toBeVisible({ timeout: 10000 });
  });

  test('MUST HAVE: Gebruiker maakt een OTHER post aan', async ({ page }) => {
    await login(page, userA.email, userA.password);
    await page.goto('/', { waitUntil: 'domcontentloaded', timeout: 60000 });

    await page.getByRole('button', { name: /create new post/i }).click();
    await expect(page.getByRole('heading', { name: 'New Post', exact: true })).toBeVisible({ timeout: 5000 });

    await page.locator('label:has-text("Post Type") + button').click();
    await page.getByRole('button', { name: '✨ Other', exact: true }).click();

    await page.locator('input[name="title"]').fill('My other hobby');
    await page.locator('textarea[name="description"]').fill('This is a generic other post');

    await page.getByRole('button', { name: /create post/i }).click();
    await page.waitForURL('**/profile', { timeout: 15000 });

    await expect(page.getByText('My other hobby')).toBeVisible({ timeout: 10000 });
  });

  test('MUST HAVE: Gebruiker maakt een READING post aan', async ({ page }) => {
    await login(page, userA.email, userA.password);
    await page.goto('/', { waitUntil: 'domcontentloaded', timeout: 60000 });

    await page.getByRole('button', { name: /create new post/i }).click();
    await expect(page.getByRole('heading', { name: 'New Post', exact: true })).toBeVisible({ timeout: 5000 });

    await page.locator('label:has-text("Post Type") + button').click();
    await page.getByRole('button', { name: '📚 Reading', exact: true }).click();

    await page.locator('input[name="title"]').fill('React Best Practices');
    await page.locator('textarea[name="description"]').fill('Reading about React patterns');
    await page.locator('input[name="totalDuration"]').fill('45');
    await page.locator('input[name="bookTitle"]').fill('React: The Complete Guide');
    await page.locator('input[name="pagesRead"]').fill('50');
    await page.locator('input[name="totalPagesRead"]').fill('300');

    await page.getByRole('button', { name: /create post/i }).click();
    await page.waitForURL('**/profile', { timeout: 15000 });

    await expect(page.getByText('React Best Practices')).toBeVisible({ timeout: 10000 });
  });

});
