import { test, expect } from '@playwright/test';
import { register } from './helpers/auth';

test.describe('Saved Lists Framework', () => {

    test.beforeEach(async ({ page }) => {
        // 1. Mock Auth Pipeline
        await page.route('**/api/auth/register', async (route) => {
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                json: { success: true, message: 'User created successfully' }
            });
        });

        // 2. Intelligent Library Microservice Router
        await page.route('**/api/library/**', async (route) => {
            const url = route.request().url();
            const method = route.request().method();

            // A. Handle Search Requests (POST)
            if (url.includes('/search')) {
                return route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    json: {
                        success: true,
                        data: [
                            {
                                resultType: 'book',
                                id: 'book-123',
                                isbn: ['9780134190440'],
                                title: 'Eloquent JavaScript',
                                authors: ['Marijn Haverbeke'],
                                thumbnailUrl: '',
                            },
                            {
                                resultType: 'book',
                                id: 'book-456',
                                isbn: ['9781491950296'],
                                title: 'Programming Rust',
                                authors: ['Jim Blandy'],
                                thumbnailUrl: '',
                            }
                        ]
                    }
                });
            }

            // B. Handle Data Fetches (GET)
            if (method === 'GET') {
                // Sniff if it's looking for resolved items within an active list
                if (url.includes('/items') || url.match(/\/lists\/[^/]+\//)) {
                    return route.fulfill({
                        status: 200,
                        contentType: 'application/json',
                        json: {
                            success: true,
                            data: [
                                { resultType: 'book', id: 'book-123', title: 'Eloquent JavaScript', authors: ['Marijn Haverbeke'], notes: 'Eloquent JavaScript' },
                                { resultType: 'book', id: 'book-456', title: 'Programming Rust', authors: ['Jim Blandy'], notes: 'Programming Rust' }
                            ]
                        }
                    });
                }

                // Otherwise, default to returning lists array for the sidebar
                return route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    json: { success: true, data: [] }
                });
            }

            // C. Handle Mutations (POST / PUT / DELETE)
            if (method === 'POST') {
                const postData = route.request().postDataJSON() || {};

                // Dynamic payload reflection keeps your UI text & styles completely accurate
                if (postData.name) {
                    return route.fulfill({
                        status: 200,
                        contentType: 'application/json',
                        json: {
                            success: true,
                            data: {
                                id: `list-${Date.now()}`,
                                name: postData.name,
                                description: postData.description || undefined
                            }
                        }
                    });
                }

                // Default fallback for items addition
                return route.fulfill({
                    status: 200,
                    contentType: 'application/json',
                    json: { success: true, message: 'Item added successfully' }
                });
            }

            // Pass through any unexpected methods safely
            await route.continue();
        });

        await register(page);
        await page.getByRole('link', { name: 'Saved' }).click();
    });

    test('should create a new list with an optional description', async ({ page }) => {
        await page.getByPlaceholder('List name...').fill('Tech Reading List');
        await page.getByPlaceholder('Description (optional)...').fill('Books to master development architectures.');
        await page.getByRole('button', { name: 'Create List' }).click();

        // Verify Title
        await expect(page.getByRole('heading', { name: 'Tech Reading List', exact: true })).toBeVisible();

        // Bypasses strict mode by picking the specific copy in the main viewport
        await expect(page.getByText('Books to master development architectures.').last()).toBeVisible();
    });

    test('should search and add items to an active list', async ({ page }) => {
        const sidebar = page.locator('.md\\:w-1\\/3');
        const mainPanel = page.locator('.md\\:flex-1');

        await sidebar.getByPlaceholder('List name...').fill('My Collection');
        await sidebar.getByRole('button', { name: 'Create List' }).click();

        // 1. Open the panel
        await mainPanel.getByRole('button', { name: '+ Add Item' }).click();

        // 2. Fill the input (This passes, confirming it's open!)
        const searchInput = mainPanel.getByPlaceholder('Type a book...');
        await expect(searchInput).toBeVisible();
        await searchInput.fill('JavaScript');

        // 3. FIX: Add exact: true so it doesn't accidentally target "− Close Search"
        await mainPanel.getByRole('button', { name: 'Search', exact: true }).click();

        // 4. Select result
        const targetResultRow = mainPanel.getByRole('listitem').filter({ hasText: 'Eloquent JavaScript' }).first();
        await expect(targetResultRow).toBeVisible();
        await targetResultRow.click();

        // 5. Verify stack
        const listStackItem = mainPanel.locator('div.group').filter({ hasText: 'Eloquent JavaScript' });
        await expect(listStackItem).toBeVisible();
    });

    test('should reorder items within the list tracking parameters', async ({ page }) => {
        const sidebar = page.locator('.md\\:w-1\\/3');
        const mainPanel = page.locator('.md\\:flex-1');

        await sidebar.getByPlaceholder('List name...').fill('Priority Stack');
        await sidebar.getByRole('button', { name: 'Create List' }).click();

        // Add Items via main panel search overlay
        await mainPanel.getByRole('button', { name: '+ Add Item' }).click();
        await mainPanel.getByPlaceholder('Type a book...').fill('JS');
        await mainPanel.getByRole('button', { name: 'Search' }).click();

        await mainPanel.getByRole('listitem').filter({ hasText: 'Eloquent JavaScript' }).first().click();
        await mainPanel.getByRole('listitem').filter({ hasText: 'Programming Rust' }).first().click();
        await mainPanel.getByRole('button', { name: '− Close Search' }).click();

        // Isolate row indexing strictly to active panel item lists
        const firstRow = mainPanel.locator('div.group').nth(0);
        const secondRow = mainPanel.locator('div.group').nth(1);

        await expect(firstRow).toContainText('Eloquent JavaScript');
        await expect(secondRow).toContainText('Programming Rust');

        await firstRow.getByRole('button', { name: '▼' }).click();

        await expect(firstRow).toContainText('Programming Rust');
        await expect(secondRow).toContainText('Eloquent JavaScript');
    });

    test('should remove an item from the designated list stack view', async ({ page }) => {
        const sidebar = page.locator('.md\\:w-1\\/3');
        const mainPanel = page.locator('.md\\:flex-1');

        await sidebar.getByPlaceholder('List name...').fill('Volatile List');
        await sidebar.getByRole('button', { name: 'Create List' }).click();

        await mainPanel.getByRole('button', { name: '+ Add Item' }).click();
        await mainPanel.getByPlaceholder('Type a book...').fill('Rust');
        await mainPanel.getByRole('button', { name: 'Search' }).click();
        await mainPanel.getByRole('listitem').filter({ hasText: 'Programming Rust' }).first().click();
        await mainPanel.getByRole('button', { name: '− Close Search' }).click();

        const targetRow = mainPanel.locator('div.group').filter({ hasText: 'Programming Rust' });
        await expect(targetRow).toBeVisible();

        await targetRow.getByRole('button', { name: 'Remove' }).click();

        await expect(targetRow).toBeHidden();
        await expect(mainPanel.getByText('This list is currently empty.')).toBeVisible();
    });

    test('should open item details modal and copy item to another list via drawer sheet', async ({ page }) => {
        const sidebar = page.locator('.md\\:w-1\\/3');
        const mainPanel = page.locator('.md\\:flex-1');

        // 1. Setup lists via sidebar parameters
        await sidebar.getByPlaceholder('List name...').fill('Source List');
        await sidebar.getByRole('button', { name: 'Create List' }).click();

        await sidebar.getByPlaceholder('List name...').fill('Destination List');
        await sidebar.getByRole('button', { name: 'Create List' }).click();

        // 2. Explicitly select list item row target within the sidebar container
        await sidebar.getByRole('paragraph').filter({ hasText: 'Source List' }).click();

        // 3. Populate Source List
        await mainPanel.getByRole('button', { name: '+ Add Item' }).click();
        await mainPanel.getByPlaceholder('Type a book...').fill('JavaScript');
        await mainPanel.getByRole('button', { name: 'Search' }).click();
        await mainPanel.getByRole('listitem').filter({ hasText: 'Eloquent JavaScript' }).first().click();
        await mainPanel.getByRole('button', { name: '− Close Search' }).click();

        // 4. Trigger Modal Detail Portal
        const targetStackRow = mainPanel.locator('div.group').filter({ hasText: 'Eloquent JavaScript' });
        await targetStackRow.click();

        // 5. Verify Modal contents (Portals live outside layout container blocks)
        await expect(page.getByRole('heading', { name: 'Eloquent JavaScript', level: 2 })).toBeVisible();
        await expect(page.getByText('By Marijn Haverbeke')).toBeVisible();

        // 6. Action drawer overlay layout mechanics
        await page.getByRole('button', { name: 'Save to List' }).click();
        await expect(page.getByText('Items can be added multiple times')).toBeVisible();

        const drawerListButton = page.getByRole('button').filter({ hasText: 'Destination List' });
        await expect(drawerListButton).toBeVisible();
        await drawerListButton.click();
        await expect(drawerListButton.getByText('In list (1x)')).toBeVisible();

        // 9. Close contexts cleanly
        await page.getByRole('button', { name: 'Close' }).click();
        await page.getByLabel('Close').click();

        // 10. Switch context inside sidebar and verify destination layout changes
        await sidebar.getByRole('paragraph').filter({ hasText: 'Destination List' }).click();
        await expect(mainPanel.getByRole('heading', { name: 'Destination List', exact: true })).toBeVisible();
        await expect(mainPanel.locator('div.group').filter({ hasText: 'Eloquent JavaScript' })).toBeVisible();
    });
});