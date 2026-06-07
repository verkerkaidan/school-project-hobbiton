import { test, expect } from '@playwright/test';

// Gateway address can be overridden with the GATEWAY_URL env var
const GATEWAY_URL = process.env.GATEWAY_URL || 'http://localhost:23738';

test.describe('Gateway /register API', () => {
  test('returns token, userId and profile on successful register', async ({ request }) => {
    const timestamp = Date.now();
    const randomNum = Math.floor(Math.random() * 10000);
    const username = `testuser_${timestamp}_${randomNum}`;
    const email = `test_${timestamp}_${randomNum}@test.com`;
    const password = '@TestPassword123';
    const name = `Test User ${timestamp}`;

    const res = await request.post(`${GATEWAY_URL}/api/register`, {
      data: {
        email,
        password,
        username,
        name,
      },
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Expect HTTP 200
    expect(res.ok()).toBeTruthy();

    const body = await res.json();

    // Basic shape assertions
    expect(body).toHaveProperty('token');
    expect(typeof body.token).toBe('string');
    expect(body.token.length).toBeGreaterThan(0);

    expect(body).toHaveProperty('userId');
    expect(body.userId).toBeTruthy();

    expect(body).toHaveProperty('profile');
    expect(body.profile).toBeTruthy();

    // If profile contains userId, ensure it matches the top-level userId
    if (body.profile.userId) {
      expect(String(body.profile.userId)).toBe(String(body.userId));
    }
  });
});
