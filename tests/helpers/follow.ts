import { expect, Page } from "@playwright/test";
import { login, logout } from "./auth";

export async function userAfollowsUserB(
  page: Page,
  userFollowed: { username: string; email: string; password: string },
  userFollower: { username: string; email: string; password: string }
) {
  await login(page, userFollower.email, userFollower.password)

  await page.goto(`/profile/${userFollowed.username}`, {
    waitUntil: 'domcontentloaded',
    timeout: 60000
  });

  // Follow
  const followButton = page.getByRole('button', { name: /^follow$/i });
  await expect(followButton).toBeVisible({ timeout: 8000 });
  await followButton.click();
  await logout(page)
}