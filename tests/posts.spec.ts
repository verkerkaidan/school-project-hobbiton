import { test, expect } from '@playwright/test';
import { register } from './helpers/auth';

test.describe('Post Deletion', () => {
  test.beforeEach(async ({ page }) => {
    await register(page);
  });

  test('should delete own post from the post detail page', async ({ page }) => {
    // Create a post first
    const createButton = page.getByRole('button', { name: /create new post/i });
    await expect(createButton).toBeVisible();
    await createButton.click();

    await page.waitForSelector('text=Create Post');

    const postTypeSelect = page.locator('select');
    await postTypeSelect.selectOption('GYM');

    await page.locator('input[name="title"]').fill('Post To Be Deleted');
    await page.locator('textarea[name="description"]').fill('This post will be deleted in the test.');
    await page.locator('input[name="totalDuration"]').fill('30');

    await page.getByRole('button', { name: /create post/i }).click();
    await page.waitForURL('**/profile', { timeout: 10000 });

    // Navigate into the post detail page
    await page.locator('text=Post To Be Deleted').first().click();
    await page.waitForURL('**/posts/**', { timeout: 10000 });

    // Delete button should be visible for the post owner
    const deleteButton = page.getByRole('button', { name: /^Delete$/i });
    await expect(deleteButton).toBeVisible({ timeout: 10000 });
    await deleteButton.click();

    // Confirm modal should appear
    await expect(page.locator('text=Are you sure')).toBeVisible({ timeout: 5000 });

    // Click the confirm delete button in the modal
    await page.getByRole('button', { name: /Delete Post/i }).click();

    // Should be redirected back to profile after deletion
    await page.waitForURL('**/profile', { timeout: 10000 });
    await expect(page.locator('text=Post To Be Deleted')).not.toBeVisible({ timeout: 5000 });
  });
});

test.describe('Post Creation', () => {
  test.beforeEach(async ({ page }) => {
    // Register a new test user before each test
    await register(page);
  });

  test('should create a gym post successfully', async ({ page }) => {
    // Click the "Create new post!" button
    const createButton = page.getByRole('button', { name: /create new post/i });
    await expect(createButton).toBeVisible();
    await createButton.click();

    // Wait for the modal to appear
    await page.waitForSelector('text=Create Post');

    // Select Gym as post type (default)
    const postTypeSelect = page.locator('select');
    await postTypeSelect.selectOption('GYM');

    // Fill in the title
    const titleInput = page.locator('input[name="title"]');
    await titleInput.fill('Morning Gym Session');

    // Fill in the description
    const descriptionTextarea = page.locator('textarea[name="description"]');
    await descriptionTextarea.fill('Had a great workout today with some heavy lifting and cardio.');

    // Fill in the total duration
    const durationInput = page.locator('input[name="totalDuration"]');
    await durationInput.fill('60.5');

    // Add an exercise
    const addExerciseButton = page.getByRole('button', { name: /\+ Add exercise/i });
    await addExerciseButton.click();

    // Fill in the exercise details (title, reps, weight, rpe)
    const exerciseTitleInput = page.locator('input').filter({ hasText: '' }).first();
    const inputs = page.locator('input[type="text"], input[type="number"]');

    // Find and fill the exercise fields
    const allInputs = await page.locator('input').all();

    // Fill in exercise title (searching for text input after description)
    for (let i = 0; i < allInputs.length; i++) {
      const type = await allInputs[i].getAttribute('type');
      if (type === 'text') {
        // This is likely the exercise title field
        await allInputs[i].fill('Bench Press');
        break;
      }
    }

    // Fill exercise reps, weight, and rpe
    const numberInputs = await page.locator('input[type="number"]').all();
    if (numberInputs.length >= 4) {
      await numberInputs[numberInputs.length - 3].fill('10'); // reps
      await numberInputs[numberInputs.length - 2].fill('225'); // weight
      await numberInputs[numberInputs.length - 1].fill('8'); // rpe
    }

    // Submit the form
    const submitButton = page.getByRole('button', { name: /create post/i });
    await submitButton.click();

    // Verify the post was created by checking if we're redirected to profile
    await page.waitForURL('**/profile', { timeout: 100000 });
    await expect(page).toHaveURL(/\/profile/);
  });

  test('should create a reading post successfully', async ({ page }) => {
    // Click the "Create new post!" button
    const createButton = page.getByRole('button', { name: /create new post/i });
    await createButton.click();

    // Wait for the modal to appear
    await page.waitForSelector('text=Create Post');

    // Select Reading as post type
    const postTypeSelect = page.locator('select');
    await postTypeSelect.selectOption('READING');

    // Fill in the title
    const titleInput = page.locator('input[name="title"]');
    await titleInput.fill('React Best Practices Book');

    // Fill in the description
    const descriptionTextarea = page.locator('textarea[name="description"]');
    await descriptionTextarea.fill('Started reading this amazing book about React patterns and best practices.');

    // Fill in the total duration
    const durationInput = page.locator('input[name="totalDuration"]');
    await durationInput.fill('45');

    // Fill in book title
    const bookTitleInput = page.locator('input[name="bookTitle"]');
    await bookTitleInput.fill('React: The Complete Guide');

    // Fill in pages read
    const pagesReadInput = page.locator('input[name="pagesRead"]');
    await pagesReadInput.fill('50');

    // Fill in total pages
    const totalPagesInput = page.locator('input[name="totalPagesRead"]');
    await totalPagesInput.fill('300');

    // Submit the form
    const submitButton = page.getByRole('button', { name: /create post/i });
    await submitButton.click();

    // Verify the post was created by checking if we're redirected to profile
    await page.waitForURL('**/profile', { timeout: 10000 });
    await expect(page).toHaveURL(/\/profile/);
  });

  test('should show validation error when title is empty', async ({ page }) => {
    // Click the "Create new post!" button
    const createButton = page.getByRole('button', { name: /create new post/i });
    await createButton.click();

    // Wait for the modal to appear
    await page.waitForSelector('text=Create Post');

    // Leave title empty and try to submit
    const submitButton = page.getByRole('button', { name: /create post/i });
    await submitButton.click();

    // Check for error message
    const errorMessage = page.locator('text=Title is required');
    await expect(errorMessage).toBeVisible();
  });

  test('should show validation error when duration is missing', async ({ page }) => {
    // Click the "Create new post!" button
    const createButton = page.getByRole('button', { name: /create new post/i });
    await createButton.click();

    // Wait for the modal to appear
    await page.waitForSelector('text=Create Post');

    // Fill in title and description but skip duration
    const titleInput = page.locator('input[name="title"]');
    await titleInput.fill('Test Post');

    const descriptionTextarea = page.locator('textarea[name="description"]');
    await descriptionTextarea.fill('Test description');

    // Try to submit without duration
    const submitButton = page.getByRole('button', { name: /create post/i });
    await submitButton.click();

    // Check for error message about duration
    const errorMessage = page.locator('text=Total duration must be greater than 0');
    await expect(errorMessage).toBeVisible();
  });

  test('should cancel post creation', async ({ page }) => {
    // Click the "Create new post!" button
    const createButton = page.getByRole('button', { name: /create new post/i });
    await createButton.click();

    // Wait for the modal to appear
    await page.waitForSelector('text=Create Post');

    // Fill in some data
    const titleInput = page.locator('input[name="title"]');
    await titleInput.fill('Test Post');

    // Click cancel button
    const cancelButton = page.getByRole('button', { name: /cancel/i });
    await cancelButton.click();

    // Modal should no longer be visible
    const modal = page.locator('text=Create Post');
    await expect(modal).not.toBeVisible({ timeout: 5000 });

    // We should still be on the home page
    await expect(page).toHaveURL(/\/$|\/home/);
  });
});




