import { test, expect, type Page } from '@playwright/test';
import { register } from './helpers/auth';
import { authCookieValue, getPostById, getPostByTitle } from './helpers/api';

async function createOtherPost(page: Page, title: string) {
  await page.goto('/');
  await page.waitForLoadState('load');

  const createButton = page.getByRole('button', { name: /create new post/i });
  await expect(createButton).toBeVisible({ timeout: 10000 });
  await createButton.click();

  await expect(page.getByRole('heading', { name: /new post/i })).toBeVisible({ timeout: 10000 });

  await page.locator('label:has-text("Post Type") + button').click();
  await page.getByRole('button').filter({ hasText: 'Other' }).click();

  await page.locator('input[name="title"]').fill(title);
  await page.locator('textarea[name="description"]').fill('E2E interaction test post.');

  await page.getByRole('button', { name: /create post/i }).click();
  await page.waitForURL('**/profile', { timeout: 30000 });
  await expect(page.getByText(title)).toBeVisible({ timeout: 15000 });
}

function likeButton(page: Page) {
  return page.locator('svg.cursor-pointer').first();
}

function likeCount(page: Page) {
  return likeButton(page).locator('xpath=following-sibling::span[1]');
}

async function expectLikeCount(page: Page, count: number) {
  await expect(likeCount(page)).toHaveText(String(count), { timeout: 10000 });
}

async function expectLiked(page: Page, liked: boolean) {
  await expect
    .poll(async () => {
      const classes = await likeButton(page).getAttribute('class');
      return classes?.includes('fill-rose-500') ?? false;
    }, { timeout: 10000 })
    .toBe(liked);
}

test.describe('Post interactions', () => {
  test('likes an existing post, refreshes, removes the like, and refreshes again with up-to-date data', async ({ page, request }) => {
    const title = `E2E like refresh ${Date.now()}`;
    await register(page);
    const token = await authCookieValue(page.context(), 'token');
    const userId = await authCookieValue(page.context(), 'userId');

    await createOtherPost(page, title);
    const post = await getPostByTitle(request, token, userId, title);
    expect(post.interaction.likes).toBe(0);

    await page.goto(`/posts/${post.id}`);
    await expect(page.getByRole('heading', { name: title })).toBeVisible({ timeout: 15000 });
    await expectLikeCount(page, 0);
    await expectLiked(page, false);

    await likeButton(page).click();
    await expectLikeCount(page, 1);
    await expectLiked(page, true);
    await expect.poll(async () => (await getPostById(request, token, post.id)).interaction.likes).toBe(1);

    await page.reload();
    await expect(page.getByRole('heading', { name: title })).toBeVisible({ timeout: 15000 });
    await expectLikeCount(page, 1);
    await expectLiked(page, true);

    await likeButton(page).click();
    await expectLikeCount(page, 0);
    await expectLiked(page, false);
    await expect.poll(async () => (await getPostById(request, token, post.id)).interaction.likes).toBe(0);

    await page.reload();
    await expect(page.getByRole('heading', { name: title })).toBeVisible({ timeout: 15000 });
    await expectLikeCount(page, 0);
    await expectLiked(page, false);
  });

  test('leaves a comment on an existing post', async ({ page, request }) => {
    const title = `E2E comment ${Date.now()}`;
    const comment = `E2E comment body ${Date.now()}`;
    await register(page);
    const token = await authCookieValue(page.context(), 'token');
    const userId = await authCookieValue(page.context(), 'userId');

    await createOtherPost(page, title);
    const post = await getPostByTitle(request, token, userId, title);
    expect(post.interaction.comments).toHaveLength(0);

    await page.goto(`/posts/${post.id}`);
    await expect(page.getByRole('heading', { name: title })).toBeVisible({ timeout: 15000 });

    const commentInput = page.locator('textarea[placeholder="Write a supportive comment..."]');
    await expect(commentInput).toBeVisible({ timeout: 10000 });
    await commentInput.fill(comment);
    await page.getByRole('button', { name: /post comment/i }).click();

    await expect(page.getByText(comment)).toBeVisible({ timeout: 15000 });
    await expect(page.getByRole('heading', { name: 'Discussion' }).locator('xpath=following-sibling::span[1]')).toHaveText('1');
    await expect
      .poll(async () => (await getPostById(request, token, post.id)).interaction.comments.map((item) => item.comment))
      .toContain(comment);
  });
});

