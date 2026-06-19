import { test, expect, Page } from '@playwright/test';
import { register } from './helpers/auth';

// ---------------------------------------------------------------------------
// Shared auth state
// ---------------------------------------------------------------------------
let savedCookies: any[];

test.beforeAll(async ({ browser }) => {
    let lastError: unknown;
    for (let attempt = 1; attempt <= 3; attempt++) {
        const context = await browser.newContext();
        const page    = await context.newPage();
        try {
            await register(page);
            savedCookies = await context.cookies();
            await context.close();
            return;
        } catch (err) {
            lastError = err;
            console.warn(`Register attempt ${attempt}/3 failed:`, err);
            await context.close();
        }
    }
    throw lastError;
});

test.beforeEach(async ({ page }) => {
    await page.context().addCookies(savedCookies);
    await page.goto('/saved', { waitUntil: 'domcontentloaded', timeout: 30_000 });
    await page.waitForLoadState('networkidle', { timeout: 15_000 });
    await expect(page.getByText('Your Lists')).toBeVisible({ timeout: 10_000 });
});

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

async function createList(page: Page, name: string, desc = '') {
    await page.locator('input[placeholder="List name..."]').fill(name);
    if (desc) await page.locator('input[placeholder="Description (optional)..."]').fill(desc);
    await page.getByRole('button', { name: 'Create List' }).click();
    await expect(page.locator(`text=${name}`).first()).toBeVisible({ timeout: 10_000 });
}

async function openAddItemPanel(page: Page) {
    await page.getByRole('button', { name: '+ Add Item' }).click();
    // Wait for the animated panel to finish expanding before interacting
    await expect(page.getByText('Book', { exact: true }).first()).toBeVisible({ timeout: 5_000 });
}

async function searchAndAddItem(page: Page, type: 'Book' | 'Movie' | 'Exercise', query: string) {
    // Click the type pill
    await page.getByText(type, { exact: true }).first().click();

    // Wait for the search input label — same technique as discover spec
    const labelPattern = new RegExp(`Search for a.+${type.toLowerCase()}`, 'i');
    await expect(page.getByText(labelPattern)).toBeVisible({ timeout: 10_000 });

    const searchInput = page.locator('.np-input').last();
    await expect(searchInput).toBeVisible({ timeout: 5_000 });
    await searchInput.fill(query);
    await page.getByRole('button', { name: 'Search' }).last().click();

    const firstResult = page.locator('ul li').first();
    await expect(firstResult).toBeVisible({ timeout: 30_000 });
    await firstResult.click();
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

test.describe('Saved Lists', () => {

    // ── List lifecycle ─────────────────────────────────────────────────────────

    test('can create a new list', async ({ page }) => {
        await createList(page, 'My E2E Test List', 'Created by Playwright');
        await expect(page.locator('text=My E2E Test List').first()).toBeVisible();
        await expect(page.locator('text=Created by Playwright').first()).toBeVisible();
    });

    test('clicking a list opens its detail panel', async ({ page }) => {
        await createList(page, 'Click Test List');

        await expect(
            page.getByRole('heading', { name: 'Click Test List' })
        ).toBeVisible({ timeout: 10_000 });

        await expect(page.locator('h2').filter({ hasText: 'Click Test List' })).toBeVisible({ timeout: 5_000 });
        await expect(page.getByRole('button', { name: '+ Add Item' })).toBeVisible();
    });

    test('can delete a list', async ({ page }) => {
        const name = `Delete Me ${Date.now()}`;

        await createList(page, name);

        const listCard = page
            .locator('p.font-semibold')
            .filter({ hasText: name })
            .locator('..')
            .locator('..');

        await listCard.getByRole('button').click();

        await expect(
            page.locator('p.font-semibold').filter({ hasText: name })
        ).toHaveCount(0);
    });

    // ── Adding items ───────────────────────────────────────────────────────────

    test('can add a book to a list', async ({ page }) => {
        await createList(page, 'Book List');

        await expect(
            page.getByRole('heading', { name: 'Book List' })
        ).toBeVisible({ timeout: 10_000 });

        await openAddItemPanel(page);
        await searchAndAddItem(page, 'Book', 'Harry Potter');

        await expect(
            page.locator('.space-y-2').locator('text=Harry Potter').first()
        ).toBeVisible({ timeout: 15_000 });
    });

    test('can add an exercise to a list', async ({ page }) => {
        await createList(page, 'Exercise List');

        await expect(
            page.getByRole('heading', { name: 'Exercise List' })
        ).toBeVisible({ timeout: 10_000 });

        await openAddItemPanel(page);
        await searchAndAddItem(page, 'Exercise', 'push up');

        await expect(
            page.locator('[class*="uppercase"]').filter({ hasText: 'exercise' }).first()
        ).toBeVisible({ timeout: 15_000 });
    });

    test('can add a movie to a list', async ({ page }) => {
        await createList(page, 'Movie List');

        await expect(
            page.getByRole('heading', { name: 'Movie List' })
        ).toBeVisible({ timeout: 10_000 });

        await openAddItemPanel(page);
        await searchAndAddItem(page, 'Movie', 'Inception');

        await expect(
            page.locator('.space-y-2').locator('text=Inception').first()
        ).toBeVisible({ timeout: 15_000 });
    });

    // ── Removing items ─────────────────────────────────────────────────────────

    test('can remove an item from a list', async ({ page }) => {
        await createList(page, 'Remove Item List');

        await expect(
            page.getByRole('heading', { name: 'Remove Item List' })
        ).toBeVisible({ timeout: 10_000 });

        await openAddItemPanel(page);
        await searchAndAddItem(page, 'Movie', 'Inception');

        await expect(
            page.locator('.space-y-2').locator('text=Inception').first()
        ).toBeVisible({ timeout: 15_000 });

        await page.getByRole('button', { name: '− Close Search' }).click();

        await page.getByRole('button', { name: 'Remove' }).first().click();

        await expect(
            page.locator('.space-y-2 div[class*="group"]')
        ).toHaveCount(0);

        await expect(
            page.getByText('This list is currently empty.')
        ).toBeVisible();
    });

    // ── Reordering ─────────────────────────────────────────────────────────────

    test('can reorder items with up/down arrows', async ({ page }) => {
        await createList(page, 'Reorder List');

        await expect(
            page.getByRole('heading', { name: 'Reorder List' })
        ).toBeVisible({ timeout: 10_000 });

        await openAddItemPanel(page);
        await searchAndAddItem(page, 'Book', 'Harry Potter');
        await expect(page.locator('.space-y-2 div[class*="group"]')).toHaveCount(1, { timeout: 20_000 });

        await page.getByRole('button', { name: '− Close Search' }).click();
        await openAddItemPanel(page);
        await searchAndAddItem(page, 'Movie', 'Inception');
        await expect(page.locator('.space-y-2 div[class*="group"]')).toHaveCount(2, { timeout: 20_000 });

        const firstBefore = await page
            .locator('.space-y-2 div[class*="group"]').first()
            .locator('p.font-semibold').textContent();

        await page
            .locator('.space-y-2 div[class*="group"]').first()
            .locator('button', { hasText: '▼' }).click();

        // Wait for the reorder API call to settle
        await page.waitForTimeout(1_500);

        const firstAfter = await page
            .locator('.space-y-2 div[class*="group"]').first()
            .locator('p.font-semibold').textContent();

        expect(firstAfter).not.toBe(firstBefore);
    });

    // ── Item detail modal ──────────────────────────────────────────────────────

    test('clicking a saved item opens the detail modal', async ({ page }) => {
        await createList(page, 'Detail Modal List');

        await expect(
            page.getByRole('heading', { name: 'Detail Modal List' })
        ).toBeVisible();

        await openAddItemPanel(page);
        await searchAndAddItem(page, 'Movie', 'Inception');

        await expect(
            page.locator('.space-y-2').locator('text=Inception').first()
        ).toBeVisible({ timeout: 15_000 });

        await page
            .locator('.space-y-2 div[class*="group"]')
            .first()
            .click();

        const modal = page.locator('.fixed.inset-0').first();

        await expect(modal).toBeVisible({ timeout: 10_000 });

        await modal.getByRole('button', { name: /close/i }).click();

        await expect(modal).not.toBeVisible({ timeout: 5_000 });
    });
    // ── Search panel toggle ────────────────────────────────────────────────────

    test('closing search panel hides the search UI', async ({ page }) => {
        await createList(page, 'Toggle Test List');

        await expect(
            page.getByRole('heading', { name: 'Toggle Test List' })
        ).toBeVisible({ timeout: 10_000 });

        await openAddItemPanel(page);
        await expect(page.getByRole('button', { name: '− Close Search' })).toBeVisible();

        await page.getByRole('button', { name: '− Close Search' }).click();
        await expect(page.getByRole('button', { name: '+ Add Item' })).toBeVisible();
    });
});