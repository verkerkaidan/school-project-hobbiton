import { test, expect } from '@playwright/test';
import { register, login } from './helpers/auth';

test.describe('Profile Editing', () => {
  let credentials: { username: string; email: string; password: string };

  test.beforeEach(async ({ page }) => {
    credentials = await register(page);
  });

  test('should edit username successfully', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const profileSection = page.getByRole('button', { name: /Profile information/i });
    await profileSection.click();

    const editButton = page.locator('span:has-text("Username")').locator('..').getByRole('button', { name: /Edit/i });
    await editButton.click();

    const input = page.locator('input[placeholder="Your username"]');
    await input.clear();
    await input.fill('new_username_123');

    await page.getByRole('button', { name: /^Save$/ }).click();

    const displayText = page.locator('span:has-text("Username")').locator('..').locator('..').locator('p.text-sm');
    await expect(displayText).toHaveText('new_username_123');
  });

  test('should edit bio successfully', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const profileSection = page.getByRole('button', { name: /Profile information/i });
    await profileSection.click();

    const editButton = page.locator('span:has-text("Bio")').locator('..').getByRole('button', { name: /Edit/i });
    await editButton.click();

    const textarea = page.locator('textarea[placeholder="Tell people a bit about yourself"]');
    await textarea.clear();
    await textarea.fill('This is my new bio!');

    await page.getByRole('button', { name: /^Save$/ }).click();

    const displayText = page.locator('span:has-text("Bio")').locator('..').locator('..').locator('p.text-sm');
    await expect(displayText).toHaveText('This is my new bio!');
  });

  test('should show validation error for empty username', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const profileSection = page.getByRole('button', { name: /Profile information/i });
    await profileSection.click();

    const editButton = page.locator('span:has-text("Username")').locator('..').getByRole('button', { name: /Edit/i });
    await editButton.click();

    const input = page.locator('input[placeholder="Your username"]');
    await input.clear();
    await input.fill('');

    await page.getByRole('button', { name: /^Save$/ }).click();

    await expect(page.getByText('Username cannot be empty')).toBeVisible();
  });

  test('should cancel editing username', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const profileSection = page.getByRole('button', { name: /Profile information/i });
    await profileSection.click();

    const editButton = page.locator('span:has-text("Username")').locator('..').getByRole('button', { name: /Edit/i });
    await editButton.click();

    const input = page.locator('input[placeholder="Your username"]');
    await input.clear();
    await input.fill('temporary_value');

    await page.getByRole('button', { name: /Cancel/i }).click();

    const displayText = page.locator('span:has-text("Username")').locator('..').locator('..').locator('p.text-sm');
    await expect(displayText).not.toHaveText('temporary_value');
  });

  test('should edit email successfully', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const securitySection = page.getByRole('button', { name: /^Security$/ });
    await securitySection.click();

    const editButton = page.locator('span:has-text("Email address")').locator('..').getByRole('button', { name: /Edit/i });
    await editButton.click();

    const input = page.locator('input[placeholder="New email address"]');
    const newEmail = `updated_${Date.now()}@test.com`;
    await input.clear();
    await input.fill(newEmail);

    await page.getByRole('button', { name: /^Save$/ }).click();

    await expect(page.getByText('Email updated')).toBeVisible();
    const displayText = page.locator('span:has-text("Email address")').locator('..').locator('..').locator('p.text-sm');
    await expect(displayText).toHaveText(newEmail);
  });

  test('should show error for empty email', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const securitySection = page.getByRole('button', { name: /^Security$/ });
    await securitySection.click();

    const editButton = page.locator('span:has-text("Email address")').locator('..').getByRole('button', { name: /Edit/i });
    await editButton.click();

    const input = page.locator('input[placeholder="New email address"]');
    await input.clear();

    await page.getByRole('button', { name: /^Save$/ }).click();

    await expect(page.getByText('Email cannot be empty')).toBeVisible();
  });

  test('should change password successfully', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const securitySection = page.getByRole('button', { name: /^Security$/ });
    await securitySection.click();

    const changeButton = page.locator('span:has-text("Password")').locator('..').getByRole('button', { name: /Change/i });
    await changeButton.click();

    await page.locator('input[placeholder="Current password"]').fill(credentials.password);
    await page.locator('input[placeholder="New password"]').fill('@NewPassword456');
    await page.locator('input[placeholder="Confirm new password"]').fill('@NewPassword456');

    await page.getByRole('button', { name: /^Save$/ }).click();

    await expect(page.getByText('Password updated successfully')).toBeVisible();
  });

  test('should show validation error for password mismatch', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const securitySection = page.getByRole('button', { name: /^Security$/ });
    await securitySection.click();

    const changeButton = page.locator('span:has-text("Password")').locator('..').getByRole('button', { name: /Change/i });
    await changeButton.click();

    await page.locator('input[placeholder="Current password"]').fill(credentials.password);
    await page.locator('input[placeholder="New password"]').fill('@NewPassword456');
    await page.locator('input[placeholder="Confirm new password"]').fill('@DifferentPassword789');

    await page.getByRole('button', { name: /^Save$/ }).click();

    await expect(page.getByText('New passwords do not match')).toBeVisible();
  });

  test('should show error for incorrect current password', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const securitySection = page.getByRole('button', { name: /^Security$/ });
    await securitySection.click();

    const changeButton = page.locator('span:has-text("Password")').locator('..').getByRole('button', { name: /Change/i });
    await changeButton.click();

    await page.locator('input[placeholder="Current password"]').fill('WrongPass123!');
    await page.locator('input[placeholder="New password"]').fill('@NewPassword456');
    await page.locator('input[placeholder="Confirm new password"]').fill('@NewPassword456');

    await page.getByRole('button', { name: /^Save$/ }).click();

    await expect(page.getByText('current password is incorrect')).toBeVisible();
  });

  test('should show error for new password that is too short', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const securitySection = page.getByRole('button', { name: /^Security$/ });
    await securitySection.click();

    const changeButton = page.locator('span:has-text("Password")').locator('..').getByRole('button', { name: /Change/i });
    await changeButton.click();

    await page.locator('input[placeholder="Current password"]').fill(credentials.password);
    await page.locator('input[placeholder="New password"]').fill('Ab1!');
    await page.locator('input[placeholder="Confirm new password"]').fill('Ab1!');

    await page.getByRole('button', { name: /^Save$/ }).click();

    await expect(page.getByText('at least 8 characters')).toBeVisible();
  });

  test('should show error for new password missing special character', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const securitySection = page.getByRole('button', { name: /^Security$/ });
    await securitySection.click();

    const changeButton = page.locator('span:has-text("Password")').locator('..').getByRole('button', { name: /Change/i });
    await changeButton.click();

    await page.locator('input[placeholder="Current password"]').fill(credentials.password);
    await page.locator('input[placeholder="New password"]').fill('Password1');
    await page.locator('input[placeholder="Confirm new password"]').fill('Password1');

    await page.getByRole('button', { name: /^Save$/ }).click();

    await expect(page.getByText('special character')).toBeVisible();
  });

  test('should show error for new password missing digit', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const securitySection = page.getByRole('button', { name: /^Security$/ });
    await securitySection.click();

    const changeButton = page.locator('span:has-text("Password")').locator('..').getByRole('button', { name: /Change/i });
    await changeButton.click();

    await page.locator('input[placeholder="Current password"]').fill(credentials.password);
    await page.locator('input[placeholder="New password"]').fill('@Password!');
    await page.locator('input[placeholder="Confirm new password"]').fill('@Password!');

    await page.getByRole('button', { name: /^Save$/ }).click();

    await expect(page.getByText('digit')).toBeVisible();
  });

  test('should show error for new password missing uppercase', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const securitySection = page.getByRole('button', { name: /^Security$/ });
    await securitySection.click();

    const changeButton = page.locator('span:has-text("Password")').locator('..').getByRole('button', { name: /Change/i });
    await changeButton.click();

    await page.locator('input[placeholder="Current password"]').fill(credentials.password);
    await page.locator('input[placeholder="New password"]').fill('@password1');
    await page.locator('input[placeholder="Confirm new password"]').fill('@password1');

    await page.getByRole('button', { name: /^Save$/ }).click();

    await expect(page.getByText('upper')).toBeVisible();
  });

  test('should allow login with new password after change', async ({ page, browser }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const securitySection = page.getByRole('button', { name: /^Security$/ });
    await securitySection.click();

    const changeButton = page.locator('span:has-text("Password")').locator('..').getByRole('button', { name: /Change/i });
    await changeButton.click();

    await page.locator('input[placeholder="Current password"]').fill(credentials.password);
    const newPassword = '@NewPassword789';
    await page.locator('input[placeholder="New password"]').fill(newPassword);
    await page.locator('input[placeholder="Confirm new password"]').fill(newPassword);

    await page.getByRole('button', { name: /^Save$/ }).click();

    await expect(page.getByText('Password updated successfully')).toBeVisible();

    await page.context().clearCookies();
    await page.goto('/');

    await login(page, credentials.email, newPassword);

    await page.waitForURL('/', { timeout: 10000 });
    await expect(page).toHaveURL('/');
  });

  test('should cancel password change', async ({ page }) => {
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');

    const securitySection = page.getByRole('button', { name: /^Security$/ });
    await securitySection.click();

    const changeButton = page.locator('span:has-text("Password")').locator('..').getByRole('button', { name: /Change/i });
    await changeButton.click();

    await expect(page.locator('input[placeholder="Current password"]')).toBeVisible();

    await page.getByRole('button', { name: /Cancel/i }).click();

    await expect(page.locator('input[placeholder="Current password"]')).not.toBeVisible();
  });
});
