import { test, expect } from '@playwright/test';
import { register, logout, isLoggedIn } from './helpers/auth';

test.describe('Authentication - Registration', () => {
    test('should register a new user successfully', async ({ page }) => {
        const credentials = await register(page);

        expect(await isLoggedIn(page)).toBeTruthy();

        await expect(page.locator('text=Welcome')).toContainText(
            credentials.username
        );
    });

    test('should show validation error for password mismatch', async ({ page }) => {
        await page.goto('/authentication');

        await page.getByRole('button', { name: /^Register$/ }).click();

        await page.locator('input[name="username"]').fill('testuser');
        await page.locator('input[name="name"]').fill('Test User');
        await page.locator('input[name="email"]').fill('test@test.com');
        await page.locator('input[name="password"]').fill('@Password123');
        await page.locator('input[name="confirmPassword"]').fill('@Password124');

        await page.getByRole('button', { name: /^Register$/ }).last().click();

        await expect(
            page.getByText(/passwords do not match/i)
        ).toBeVisible();
    });

    test('should require all mandatory fields', async ({ page }) => {
        await page.goto('/authentication');

        await page.getByRole('button', { name: /^Register$/ }).click();

        await page.getByRole('button', { name: /^Register$/ }).last().click();

        await expect(page.locator('input[name="username"]')).toBeVisible();

        // Adjust based on your actual validation messages
        await expect(page.getByText(/required/i).first()).toBeVisible();
    });

    test('should not allow duplicate registration', async ({ page }) => {
        const credentials = await register(page);

        await logout(page);

        await page.goto('/authentication');
        await page.getByRole('button', { name: /^Register$/ }).click();

        await page.locator('input[name="username"]').fill(credentials.username);
        await page.locator('input[name="name"]').fill(credentials.name);
        await page.locator('input[name="email"]').fill(credentials.email);
        await page.locator('input[name="password"]').fill(credentials.password);
        await page
            .locator('input[name="confirmPassword"]')
            .fill(credentials.password);

        await page.getByRole('button', { name: /^Register$/ }).last().click();

        await expect(
            page.getByText(/already exists|already registered/i)
        ).toBeVisible();
    });
});