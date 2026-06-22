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
    await page.goto('/', { waitUntil: 'domcontentloaded', timeout: 30_000 });
    await page.waitForLoadState('load', { timeout: 15_000 });
});

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

async function openCreatePostModal(page: Page) {
    const trigger = page
        .getByRole('button', { name: /new post|create post/i })
        .or(page.locator('[aria-label*="new post" i]'))
        .first();

    await expect(trigger).toBeVisible({ timeout: 10_000 });
    await trigger.click();
    await expect(
        page.getByRole('heading', { name: 'New Post' })
    ).toBeVisible({ timeout: 10_000 });}

async function fillBaseFields(page: Page, title = 'E2E Gym Post', description = 'Playwright created this') {
    await page.locator('input[name="title"]').fill(title);
    await page.locator('textarea[name="description"]').fill(description);
}

async function linkItem(page: Page, type: 'Exercise' | 'Book' | 'Movie', query: string) {
    const addBtn = page.getByRole('button', { name: '+ Add item' });
    await expect(addBtn).toBeVisible({ timeout: 10_000 });
    await addBtn.click();

    const panel = page.locator('.np-search-panel');
    await expect(panel).toBeVisible({ timeout: 10_000 });

    const typeBtn = panel.locator('.np-type-btn', { hasText: type });
    await expect(typeBtn).toBeVisible({ timeout: 15_000 });
    await typeBtn.click({ timeout: 15_000 });

    const labelPattern = new RegExp(`Search for a.+${type.toLowerCase()}`, 'i');
    await expect(page.getByText(labelPattern)).toBeVisible({ timeout: 10_000 });

    const searchInput = page.locator('.np-input').last();
    await expect(searchInput).toBeVisible({ timeout: 10_000 });

    await searchInput.fill(query);
    await page.getByRole('button', { name: 'Search' }).last().click();

    const firstResult = page.locator('ul li').first();
    await expect(firstResult).toBeVisible({ timeout: 30_000 });
    await firstResult.click();

    // Close the search panel so subsequent linkItem calls can reopen it
    await expect(page.getByRole('button', { name: '− Close' })).toBeVisible({ timeout: 5_000 });
    await page.getByRole('button', { name: '− Close' }).click();
}

function linkedItemsArea(page: Page) {
    return page.locator('.np-chips');
}

async function selectPostType(page: Page, label: 'Gym' | 'Reading' | 'Other') {
    // Click the trigger to open the dropdown
    await page.locator('button').filter({ hasText: /gym|reading|other/i }).first().click();
    // The dropdown option has an emoji prefix (e.g. "📚 Reading"),
    // so use a case-insensitive contains match and pick the last one
    const option = page.getByRole('button', { name: new RegExp(label, 'i') }).last();
    await expect(option).toBeVisible({ timeout: 5_000 });
    await option.click();
}

async function addExercise(page: Page, name = 'Bench Press') {
    await page.getByRole('button', { name: '+ Add exercise' }).click();
    const lastRow = page.locator('.np-exercises > div').last();
    await lastRow.locator('input').nth(0).fill(name);
    await lastRow.locator('input[type="number"]').nth(0).fill('10'); // reps
    await lastRow.locator('input[type="number"]').nth(1).fill('80'); // weight
    await lastRow.locator('input[type="number"]').nth(2).fill('8');  // rpe
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

test.describe('Create Post — linked items', () => {

    // ── Modal open / close ─────────────────────────────────────────────────────

    test('Create Post modal opens and closes', async ({ page }) => {
        await openCreatePostModal(page);
        await page.getByRole('button', { name: 'Cancel' }).click();
        await expect(
            page.getByRole('heading', { name: 'New Post' })
        ).not.toBeVisible();    });

    // ── Add item panel toggle ──────────────────────────────────────────────────

    test('"+ Add item" toggles the search panel open and closed', async ({ page }) => {
        await openCreatePostModal(page);

        await page.getByRole('button', { name: '+ Add item' }).click();
        await expect(page.locator('.np-search-panel .np-type-btn', { hasText: 'Exercise' })).toBeVisible({ timeout: 5_000 });

        await page.getByRole('button', { name: '− Close' }).click();
        await expect(page.getByRole('button', { name: '+ Add item' })).toBeVisible();
    });

    // ── Linking individual item types ──────────────────────────────────────────

    test('can link a book to a post', async ({ page }) => {
        await openCreatePostModal(page);
        await linkItem(page, 'Book', 'Harry Potter');

        const chip = linkedItemsArea(page).locator('.np-chip').first();
        await expect(chip).toBeVisible({ timeout: 15_000 });
        await expect(chip).toContainText('Harry Potter', { ignoreCase: true });
        await expect(chip.locator('.np-chip-type')).toContainText('BOOK', { ignoreCase: true });
    });

    test('can link an exercise to a post', async ({ page }) => {
        await openCreatePostModal(page);
        await linkItem(page, 'Exercise', 'push up');

        const chip = linkedItemsArea(page).locator('.np-chip').first();
        await expect(chip).toBeVisible({ timeout: 15_000 });
        await expect(chip.locator('.np-chip-type')).toContainText('EXERCISE', { ignoreCase: true });
    });

    test('can link a movie to a post', async ({ page }) => {
        await openCreatePostModal(page);
        await linkItem(page, 'Movie', 'Inception');

        const chip = linkedItemsArea(page).locator('.np-chip').first();
        await expect(chip).toBeVisible({ timeout: 15_000 });
        await expect(chip).toContainText('Inception', { ignoreCase: true });
        await expect(chip.locator('.np-chip-type')).toContainText('MOVIE', { ignoreCase: true });
    });

    // ── Chip behaviour ─────────────────────────────────────────────────────────

    test('duplicate linked items are not added twice', async ({ page }) => {
        await openCreatePostModal(page);
        await linkItem(page, 'Book', 'Harry Potter');
        await expect(linkedItemsArea(page).locator('.np-chip')).toHaveCount(1, { timeout: 15_000 });

        await linkItem(page, 'Book', 'Harry Potter');
        await page.waitForTimeout(1_500);

        await expect(linkedItemsArea(page).locator('.np-chip')).toHaveCount(1);
    });

    test('can link multiple different items', async ({ page }) => {
        await openCreatePostModal(page);

        await linkItem(page, 'Book', 'Harry Potter');
        await expect(linkedItemsArea(page).locator('.np-chip')).toHaveCount(1, { timeout: 15_000 });

        await linkItem(page, 'Movie', 'Inception');
        await expect(linkedItemsArea(page).locator('.np-chip')).toHaveCount(2, { timeout: 15_000 });
    });

    test('can remove a linked item chip', async ({ page }) => {
        await openCreatePostModal(page);
        await linkItem(page, 'Book', 'Harry Potter');
        await expect(linkedItemsArea(page).locator('.np-chip')).toHaveCount(1, { timeout: 15_000 });

        await linkedItemsArea(page).locator('.np-chip-remove').first().click();
        await expect(linkedItemsArea(page).locator('.np-chip')).toHaveCount(0, { timeout: 5_000 });
    });

    // ── Full post submissions ──────────────────────────────────────────────────

    test('can submit a Gym post with a linked exercise', async ({ page }) => {
        await openCreatePostModal(page);
        await fillBaseFields(page, 'E2E Bench Day', 'Playwright gym session');
        await page.locator('input[name="totalDuration"]').fill('60');
        await addExercise(page, 'Bench Press');

        await linkItem(page, 'Exercise', 'bench press');
        await expect(linkedItemsArea(page).locator('.np-chip')).toHaveCount(1, { timeout: 15_000 });

        await page.getByRole('button', { name: 'Create Post' }).click();
        await page.waitForURL('/profile', { timeout: 20_000 });
    });

    test('can submit a Reading post with a linked book', async ({ page }) => {
        await openCreatePostModal(page);
        await selectPostType(page, 'Reading');
        await fillBaseFields(page, 'E2E Reading Session', 'Playwright read some pages');
        await page.locator('input[name="totalDuration"]').fill('45');
        await page.locator('input[name="bookTitle"]').fill("Harry Potter and the Philosopher's Stone");
        await page.locator('input[name="pagesRead"]').fill('50');
        await page.locator('input[name="totalPagesRead"]').fill('300');

        await linkItem(page, 'Book', 'Harry Potter');
        await expect(linkedItemsArea(page).locator('.np-chip')).toHaveCount(1, { timeout: 15_000 });

        await page.getByRole('button', { name: 'Create Post' }).click();
        await page.waitForURL('/profile', { timeout: 20_000 });
    });

    // ── Validation errors ──────────────────────────────────────────────────────

    test('missing title shows validation error', async ({ page }) => {
        await openCreatePostModal(page);
        await page.locator('textarea[name="description"]').fill('Some description');
        await page.getByRole('button', { name: 'Create Post' }).click();
        await expect(page.locator('.np-error')).toContainText('Title is required');
    });

    test('missing description shows validation error', async ({ page }) => {
        await openCreatePostModal(page);
        await page.locator('input[name="title"]').fill('My Title');
        await page.getByRole('button', { name: 'Create Post' }).click();
        await expect(page.locator('.np-error')).toContainText('Description is required');
    });

    test('Gym post with zero exercises shows validation error', async ({ page }) => {
        await openCreatePostModal(page);
        await fillBaseFields(page);
        await page.locator('input[name="totalDuration"]').fill('30');
        await page.getByRole('button', { name: 'Create Post' }).click();
        await expect(page.locator('.np-error')).toContainText('Add at least one exercise');
    });

    test('pages read > total pages shows validation error', async ({ page }) => {
        await openCreatePostModal(page);
        await selectPostType(page, 'Reading');
        await fillBaseFields(page);
        await page.locator('input[name="totalDuration"]').fill('30');
        await page.locator('input[name="bookTitle"]').fill('Some Book');
        await page.locator('input[name="pagesRead"]').fill('999');
        await page.locator('input[name="totalPagesRead"]').fill('100');
        await page.getByRole('button', { name: 'Create Post' }).click();
        await expect(page.locator('.np-error')).toContainText('Pages read cannot exceed total pages');
    });

    // ── Photo upload ───────────────────────────────────────────────────────────

    test('can upload an image and see a preview', async ({ page }) => {
        await openCreatePostModal(page);

        const [fileChooser] = await Promise.all([
            page.waitForEvent('filechooser'),
            page.locator('.np-dropzone').click(),
        ]);

        await fileChooser.setFiles({
            name: 'test-image.png',
            mimeType: 'image/png',
            buffer: Buffer.from(
                'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwADhQGAWjR9awAAAABJRU5ErkJggg==',
                'base64',
            ),
        });

        await expect(page.locator('.np-preview-img').first()).toBeVisible({ timeout: 5_000 });
        await expect(page.locator('.np-img-count')).toHaveText('1/3');
    });
});