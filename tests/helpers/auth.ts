import { type Page, type APIRequestContext } from '@playwright/test';
import { Pool } from 'pg';

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
  const MAX_ATTEMPTS = 3;

  for (let attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
    const credentials = generateTestCredentials();

    await page.goto('/authentication', {
      waitUntil: 'domcontentloaded',
      timeout: 60000
    });

    const registerButton = page.getByRole('button', { name: /^Register$/ });
    await registerButton.waitFor({ state: 'visible' });

    const usernameInput = page.locator('input[name="username"]');
    for (let click = 0; click < 3; click++) {
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

    // Detect 500 error or success (TOS dialog)
    const result = await Promise.race([
      page.getByText('Continued use of the platform')
        .waitFor({ state: 'visible', timeout: 10000 })
        .then(() => 'success' as const),
      page.getByText(/Registration failed|500|server error/i)
        .waitFor({ state: 'visible', timeout: 10000 })
        .then(() => 'error' as const),
    ]).catch(() => 'error' as const);

    if (result === 'error') {
      console.warn(`Registration attempt ${attempt + 1} failed (500), retrying...`);
      continue;
    }

    // TOS flow
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

  throw new Error(`Registration failed after ${MAX_ATTEMPTS} attempts`);
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
export async function login(page: Page, email: string, password: string): Promise<void> {
  const MAX_ATTEMPTS = 3;

  for (let attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
    await page.goto('/authentication', { waitUntil: 'domcontentloaded', timeout: 60000 });

    const loginButton = page.getByRole('button', { name: /^Login$/ });
    await loginButton.waitFor({ state: 'visible' });

    for (let click = 0; click < 3; click++) {
      await loginButton.click();
      const appeared = await page.locator('input[name="email"]')
        .waitFor({ state: 'visible', timeout: 3000 }).then(() => true).catch(() => false);
      if (appeared) break;
    }

    await page.locator('input[name="email"]').fill(email);
    await page.locator('input[name="password"]').fill(password);
    await page.getByRole('button', { name: /^Login$/ }).last().click();

    const result = await Promise.race([
      page.waitForURL('/', { timeout: 10000 }).then(() => 'success' as const),
      page.getByText(/Failed to fetch|server error/i)
        .waitFor({ state: 'visible', timeout: 10000 })
        .then(() => 'error' as const),
    ]).catch(() => 'error' as const);

    if (result === 'success') return;

    console.warn(`Login attempt ${attempt + 1} failed, retrying...`);
    await page.waitForTimeout(1000);
  }

  throw new Error(`Login failed after ${MAX_ATTEMPTS} attempts`);
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

/**
 * Promotes a user to ROLE_ADMIN by inserting directly into the authentication database.
 * Returns true if the promotion succeeded.
 */
export async function promoteToAdmin(userId: string): Promise<boolean> {
  const pool = new Pool({
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT || '5432'),
    user: process.env.SPRING_DATASOURCE_USERNAME_AUTHENTICATION || 'authentication_database_user',
    password: process.env.SPRING_DATASOURCE_PASSWORD_AUTHENTICATION || 'di*8jdK',
    database: process.env.SPRING_DATASOURCE_AUTHENTICATION_DATABASE || 'authentication_service',
  });

  try {
    const roleResult = await pool.query("SELECT id FROM role WHERE name = 'ROLE_ADMIN'");
    if (roleResult.rows.length === 0) {
      console.warn('[promoteToAdmin] ROLE_ADMIN not found in database');
      return false;
    }
    const roleId = roleResult.rows[0].id;

    await pool.query(
      'INSERT INTO users_roles (user_id, role_id) VALUES ($1, $2) ON CONFLICT DO NOTHING',
      [userId, roleId]
    );

    return true;
  } catch (err) {
    console.warn('[promoteToAdmin] Failed to promote user:', err);
    return false;
  } finally {
    await pool.end();
  }
}

/**
 * Logs in via the API Gateway and returns token + userId.
 */
export async function loginViaAPI(
  request: APIRequestContext,
  email: string,
  password: string
): Promise<{ token: string; userId: string } | null> {
  const GATEWAY_URL = process.env.GATEWAY_URL || 'http://localhost:23738';
  try {
    const res = await request.post(`${GATEWAY_URL}/api/auth/login`, {
      data: { email, password },
      headers: { 'Content-Type': 'application/json' },
    });
    if (res.status() !== 200) return null;
    return await res.json();
  } catch {
    return null;
  }
}
