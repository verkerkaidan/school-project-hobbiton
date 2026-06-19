import { test, expect, Page } from '@playwright/test';
import { register } from './helpers/auth';

// ---------------------------------------------------------------------------
// Stable search terms
// ---------------------------------------------------------------------------
const SEARCH = {
    book:     { query: 'Harry Potter', expectedTitle: 'Harry Potter' },
    exercise: { query: 'push up',      expectedName:  'push'         },
    movie:    { query: 'Inception',    expectedTitle: 'Inception'    },
} as const;

// ---------------------------------------------------------------------------
// Shared auth state
// ---------------------------------------------------------------------------
let savedCookies: any[];

test.beforeAll(async ({ browser }) => {
    // Retry registration up to 3 times — Next.js hydration can be slow on first
    // load, causing the Register button click to silently do nothing.
    let lastError: unknown;
    for (let attempt = 1; attempt <= 3; attempt++) {
        const context = await browser.newContext();
        const page    = await context.newPage();
        try {
            await register(page);
            savedCookies = await context.cookies();
            await context.close();
            return; // success — exit beforeAll
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

    await page.goto('/discover', { waitUntil: 'domcontentloaded', timeout: 30_000 });

    // Wait for JS hydration to complete before touching anything.
    // This is critical: framer-motion buttons look visible before React has
    // attached event handlers, causing silent click failures.
    await page.waitForLoadState('networkidle', { timeout: 15_000 });

    // Confirm all three hobby buttons are fully rendered (last one takes ~1.1s
    // due to staggered framer-motion delays)
    await expect(page.getByRole('button', { name: 'Movie' })).toBeVisible({ timeout: 10_000 });
});

// ---------------------------------------------------------------------------
// Core helper
// ---------------------------------------------------------------------------

/**
 * Clicks a hobby button and waits for the SearchBar to be ready.
 * We wait for the *label text* that SearchBar renders — it is unique and only
 * appears after the component finishes mounting and animating in.
 * Only then do we fill the np-input and submit.
 */
async function runSearch(page: Page, hobbyLabel: string, query: string) {
    const hobbyButton = page.getByRole('button', { name: hobbyLabel });

    // Extra safety: scroll into view and wait for it to be fully stable
    await hobbyButton.scrollIntoViewIfNeeded();
    await expect(hobbyButton).toBeVisible({ timeout: 10_000 });

    await hobbyButton.click();

    // Wait for the SearchBar label — this is the reliable signal that the
    // component has mounted and the input is ready to receive input.
    // e.g. "Search for a book:", "Search for an exercise:", etc.
    const labelPattern = new RegExp(`Search for a.+${hobbyLabel.toLowerCase()}`, 'i');
    await expect(page.getByText(labelPattern)).toBeVisible({ timeout: 10_000 });

    // Now the np-input is safe to fill
    const input = page.locator('.np-input');
    await expect(input).toBeVisible({ timeout: 5_000 });
    await input.fill(query);

    await page.getByRole('button', { name: 'Search' }).click();

    // External APIs can be slow — give them plenty of time
    const firstResult = page.locator('ul li').first();
    await expect(firstResult).toBeVisible({ timeout: 30_000 });
    return firstResult;
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

test.describe('Discover page', () => {

    // ── Books ─────────────────────────────────────────────────────────────────

    test('can search for a book and see results', async ({ page }) => {
        const firstResult = await runSearch(page, 'Book', SEARCH.book.query);
        await expect(firstResult).toContainText(SEARCH.book.expectedTitle, { ignoreCase: true });
    });

    test('can open book detail modal and it shows expected fields', async ({ page }) => {
        await runSearch(page, 'Book', SEARCH.book.query);
        await page.locator('ul li').first().click();

        const modal = page.locator('.fixed.inset-0 .glass-card');
        await expect(modal).toBeVisible();

        await expect(
            modal.getByRole('button', { name: 'Save to List' })
        ).toBeVisible();
    });

    test('back button from results returns to search bar', async ({ page }) => {
        await runSearch(page, 'Book', SEARCH.book.query);
        await page.getByRole('button', { name: /← Back/i }).click();

        // SearchBar should be visible again, step-selector should be gone
        await expect(page.locator('.np-input')).toBeVisible({ timeout: 5_000 });
        await expect(page.getByText('What do you want to discover?')).not.toBeVisible();
    });

    test('back button from search bar returns to category selector', async ({ page }) => {
        await page.getByRole('button', { name: 'Book' }).click();
        // Wait for SearchBar to mount before trying to click back
        await expect(page.getByText(/Search for a.+book/i)).toBeVisible({ timeout: 10_000 });

        await page.getByRole('button', { name: /← Back/i }).click();
        await expect(page.getByText('What do you want to discover?')).toBeVisible({ timeout: 5_000 });
    });

    // ── Exercises ──────────────────────────────────────────────────────────────

    test('can search for an exercise and see gif thumbnail', async ({ page }) => {
        await runSearch(page, 'Exercise', SEARCH.exercise.query);

        const img = page.locator('ul li').first().locator('img');
        await expect(img).toBeVisible({ timeout: 10_000 });
    });

    test('can open exercise detail modal and see instructions', async ({ page }) => {
        await runSearch(page, 'Exercise', SEARCH.exercise.query);
        await page.locator('ul li').first().click();

        const modal = page.locator('.glass-card').filter({ hasText: /Step-by-Step Instructions/i }).first();
        await expect(modal).toBeVisible({ timeout: 10_000 });
        await expect(modal.locator('ol li').first()).toBeVisible();
    });

    // ── Movies ────────────────────────────────────────────────────────────────

    test('can search for a movie and see results', async ({ page }) => {
        const firstResult = await runSearch(page, 'Movie', SEARCH.movie.query);
        await expect(firstResult).toContainText(SEARCH.movie.expectedTitle, { ignoreCase: true, timeout: 10_000 });
    });

    test('can open movie detail modal and shows plot', async ({ page }) => {
        await runSearch(page, 'Movie', SEARCH.movie.query);
        await page.locator('ul li').first().click();

        const modal = page.locator('.fixed.inset-0 .glass-card');
        await expect(modal).toBeVisible();

        await expect(modal.getByRole('button', { name: 'Save to List' }))
            .toBeVisible();

        await expect(modal.getByText('Plot')).toBeVisible();
    });

    // ── Edge cases ────────────────────────────────────────────────────────────

    test('searching with empty input shows validation error', async ({ page }) => {
        await page.getByRole('button', { name: 'Book' }).click();
        await expect(page.getByText(/Search for a.+book/i)).toBeVisible({ timeout: 10_000 });

        await page.getByRole('button', { name: 'Search' }).click();
        await expect(page.getByText('Please enter a search term.')).toBeVisible({ timeout: 3_000 });
    });

    test('searching a gibberish query returns no results gracefully', async ({ page }) => {
        await page.getByRole('button', { name: 'Book' }).click();
        await expect(page.getByText(/Search for a.+book/i)).toBeVisible({ timeout: 10_000 });

        await page.locator('.np-input').fill('xyzzy_no_result_12345');
        await page.getByRole('button', { name: 'Search' }).click();

        await expect(page.getByText('Results')).toBeVisible({ timeout: 20_000 });
        await expect(page.locator('ul li')).toHaveCount(0, { timeout: 5_000 });
    });
});