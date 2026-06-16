import { Page } from '@playwright/test';

/**
 * Generates unique credentials for a test user
 * Uses timestamp to ensure uniqueness
 */
function generateTestCredentials() {
  const timestamp = Date.now();
  const randomNum = Math.floor(Math.random() * 10000);
  return {
    username: `testuser_${timestamp}_${randomNum}`,
    email: `test_${timestamp}_${randomNum}@test.com`,
    password: '@TestPassword123', // Must be 8+ chars, 1 special char, 1 capital letter
    name: `Test User ${timestamp}`,
  };
}

/**
 * Registers a new user by navigating to the authentication page, opening the register modal,
 * and submitting the registration form
 * @param page The Playwright page object
 * @returns The credentials used for registration
 */
export async function register(
  page: Page
): Promise<{ username: string; email: string; password: string; name: string }> {
  const credentials = generateTestCredentials();

  // Navigate to authentication page
  await page.goto('/authentication');
  await page.waitForLoadState('networkidle');

  // Click the Register button to open the register modal
  await page.getByRole('button', { name: /^Register$/ }).click();

  // Wait for the register modal to appear and the username input to be visible
  const usernameInput = page.locator('input[name="username"]');
  await usernameInput.waitFor({ state: 'visible', timeout: 5000 });

  // Fill in the registration form
  const nameInput = page.locator('input[name="name"]');
  const emailInput = page.locator('input[name="email"]');
  const passwordInput = page.locator('input[name="password"]');
  const confirmPasswordInput = page.locator('input[name="confirmPassword"]');

  await usernameInput.fill(credentials.username);
  await nameInput.fill(credentials.name);
  await emailInput.fill(credentials.email);
  await passwordInput.fill(credentials.password);
  await confirmPasswordInput.fill(credentials.password);

  // Click the register button in the form
  const submitButton = page.getByRole('button', { name: /^Register$/ }).last();
  await submitButton.click();

  // Wait for redirect to home page after successful registration
  // The register should redirect to '/' and refresh
  await page.waitForURL('/', { timeout: 10000 });
  await page.waitForLoadState('networkidle');

  // Verify we're logged in by checking if we're on the home page
  // and the page has loaded (the welcome message should show a username)
  await page.waitForSelector('text=Welcome', { timeout: 5000 });

  return credentials;
}

/**
 * Log's in user by navigating and filling out the email and password fields and
 * @param page The Playwright page object
 * @param email The test email
 * @param password The test password
 */
export async function login(
    page: Page,
    email: string,
    password: string
): Promise<void> {
  await page.goto('/authentication');
  await page.waitForLoadState('networkidle');

  await page.locator('input[name="email"]').fill(email);
  await page.locator('input[name="password"]').fill(password);

  await page.getByRole('button', { name: /^Login$/ }).click();

  await page.waitForURL('/', {
    timeout: 10000,
  });

  await page.waitForLoadState('networkidle');
}
/**
 * Logs out the user by navigating to home and finding the logout functionality
 * @param page The Playwright page object
 */
export async function logout(page: Page): Promise<void> {
  // Navigate to profile or settings where logout might be available
  // For now, we can just clear the cookies
  await page.context().clearCookies();
  await page.goto('/');
  await page.waitForLoadState('networkidle');
}

/**
 * Helper to check if user is logged in
 * @param page The Playwright page object
 * @returns true if user appears to be logged in
 */
export async function isLoggedIn(page: Page): Promise<boolean> {
  // Check if token cookie exists
  const cookies = await page.context().cookies();
  return cookies.some(cookie => cookie.name === 'token');
}

