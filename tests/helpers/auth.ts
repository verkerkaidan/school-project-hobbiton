import { Page } from '@playwright/test';

function generateTestCredentials() {
  const timestamp = Date.now();
  const randomNum = Math.floor(Math.random() * 10000);
  return {
    username: `testuser_${timestamp}_${randomNum}`,
    email: `test_${timestamp}_${randomNum}@test.com`,
    password: '@TestPassword123',
  };
}

export async function register(
    page: Page
): Promise<{ username: string; email: string; password: string }> {
  const credentials = generateTestCredentials();

  // Give the server up to 60 seconds to compile, but don't wait for background networks to freeze
  await page.goto('/authentication', {
    waitUntil: 'domcontentloaded',
    timeout: 60000
  });

  // Explicitly look for the button instead of a generic networkidle wait
  await page.getByRole('button', { name: /^Register$/ }).waitFor({ state: 'visible' });
  await page.getByRole('button', { name: /^Register$/ }).click();

  // 2. Fill in the credentials
  const usernameInput = page.locator('input[name="username"]');
  await usernameInput.waitFor({ state: 'visible', timeout: 5000 });

  await usernameInput.fill(credentials.username);
  await page.locator('input[name="email"]').fill(credentials.email);
  await page.locator('input[name="password"]').fill(credentials.password);
  await page.locator('input[name="confirmPassword"]').fill(credentials.password);

  // 3. Submit the registration form
  const submitButton = page.getByRole('button', { name: /^Register$/ }).last();
  await submitButton.click();

// ====================================================================
  // 4. Handle the Terms of Service step right here on /authentication
  // ====================================================================

  // Wait for the ToS text header to become visible so we know the step loaded
  const tosHeader = page.getByText('Continued use of the platform');
  await tosHeader.waitFor({ state: 'visible', timeout: 5000 });

  // Find the inner text box using a phrase inside it, scroll it to the bottom,
  // and fire a scroll event so React updates its `hasScrolledToBottom` state.
  await page.getByText('Welcome to Hobbiton.').evaluate((el) => {
    const container = el.closest('.overflow-y-auto');
    if (container) {
      container.scrollTop = container.scrollHeight;
      container.dispatchEvent(new Event('scroll'));
    }
  });

  // Locate the checkboxes (which are now successfully enabled!)
  const checkbox1 = page.getByRole('checkbox', { name: 'I have read and agree to the' });
  const checkbox2 = page.getByRole('checkbox', { name: 'I understand that posting' });

  // Check them normally
  await checkbox1.check();
  await checkbox2.check();

  // Click Accept & Continue to finally trigger the redirect
  await page.getByRole('button', { name: 'Accept & Continue' }).click();

  // ====================================================================

  // 5. Wait for final redirect to home page
  await page.waitForURL('/', { timeout: 10000 });
  await page.waitForLoadState('networkidle');
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
  await page.goto('/authentication', {
    waitUntil: 'domcontentloaded',
    timeout: 60000
  });

  await page.locator('input[name="email"]').waitFor({ state: 'visible' });
  await page.locator('input[name="email"]').fill(email);
  await page.locator('input[name="password"]').fill(password);

  await page.getByRole('button', { name: /^Login$/ }).click();

  await page.waitForURL('/', { timeout: 10000 });
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

