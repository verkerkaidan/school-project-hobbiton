import { type Page, type APIRequestContext } from '@playwright/test';

function generateTestCredentials() {
  const timestamp = Date.now();
  const randomNum = Math.floor(Math.random() * 10000);
  return {
    username: `testuser_${timestamp}_${randomNum}`,
    email: `test_${timestamp}_${randomNum}@test.com`,
    password: '@TestPassword123',
    name: `Test User ${timestamp}`,
  };
}

export async function register(
    page: Page
): Promise<{ username: string; email: string; password: string; name: string }> {
  const credentials = generateTestCredentials();

  await page.goto('/authentication', {
    waitUntil: 'domcontentloaded',
    timeout: 60000
  });

  const registerButton = page.getByRole('button', { name: /^Register$/ });
  await registerButton.waitFor({ state: 'visible' });

  const usernameInput = page.locator('input[name="username"]');
  for (let attempt = 0; attempt < 3; attempt++) {
    await registerButton.click();
    const appeared = await usernameInput.waitFor({ state: 'visible', timeout: 3000 }).then(() => true).catch(() => false);
    if (appeared) break;
  }
  await usernameInput.waitFor({ state: 'visible', timeout: 5000 });

  await usernameInput.fill(credentials.username);
  await page.locator('input[name="email"]').fill(credentials.email);
  await page.locator('input[name="password"]').fill(credentials.password);
  await page.locator('input[name="confirmPassword"]').fill(credentials.password);

  const submitButton = page.getByRole('button', { name: /^Register$/ }).last();
  await submitButton.click();

  const tosHeader = page.getByText('Continued use of the platform');
  await tosHeader.waitFor({ state: 'visible', timeout: 10000 });

  await page.getByText('Welcome to Hobbiton.').evaluate((el) => {
    const container = el.closest('.overflow-y-auto');
    if (container) {
      container.scrollTop = container.scrollHeight;
      container.dispatchEvent(new Event('scroll'));
    }
  });

  const checkbox1 = page.getByRole('checkbox', { name: 'I have read and agree to the' });
  const checkbox2 = page.getByRole('checkbox', { name: 'I understand that posting' });

  await checkbox1.check();
  await checkbox2.check();

  await page.getByRole('button', { name: 'Accept & Continue' }).click();

  await page.waitForURL('/', { timeout: 10000 });
  await page.waitForSelector('text=Welcome', { timeout: 10000 });

  return credentials;
}

/**
 * Registers a new test user via the API Gateway /api/register endpoint.
 * Returns credentials plus token and userId.
 */
export async function registerViaAPI(
    request: APIRequestContext
): Promise<{ username: string; email: string; password: string; name: string; token: string; userId: string }> {
  const credentials = generateTestCredentials();
  const GATEWAY_URL = process.env.GATEWAY_URL || 'http://localhost:23738';

  const res = await request.post(`${GATEWAY_URL}/api/register`, {
    data: {
      email: credentials.email,
      password: credentials.password,
      username: credentials.username,
      name: credentials.name,
    },
    headers: { 'Content-Type': 'application/json' },
  });

  const body = await res.json();

  return {
    ...credentials,
    token: body.token,
    userId: body.userId,
  };
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

  const loginButton = page.getByRole('button', { name: /^Login$/ });
  await loginButton.waitFor({ state: 'visible' });

  for (let attempt = 0; attempt < 3; attempt++) {
    await loginButton.click();
    const appeared = await page.locator('input[name="email"]').waitFor({ state: 'visible', timeout: 3000 }).then(() => true).catch(() => false);
    if (appeared) break;
  }

  await page.locator('input[name="email"]').fill(email);
  await page.locator('input[name="password"]').fill(password);

  await page.getByRole('button', { name: /^Login$/ }).last().click();

  await page.waitForURL('/', { timeout: 10000 });
}
/**
 * Logs out the user by navigating to home and finding the logout functionality
 * @param page The Playwright page object
 */
export async function logout(page: Page): Promise<void> {
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
  const cookies = await page.context().cookies();
  return cookies.some(cookie => cookie.name === 'token');
}
