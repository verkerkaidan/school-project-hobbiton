# Playwright Tests for Hobbithon

This directory contains end-to-end (E2E) tests for the Hobbithon social media application using Playwright.

## Test Files

- **posts.spec.ts** - Tests for creating posts (both gym and reading posts)
- **profile.spec.ts** - Tests for viewing user profiles
- **helpers/auth.ts** - Authentication helper functions

## Prerequisites

1. Ensure the Hobbithon application is running with Docker Compose:
   ```bash
   docker-compose up -d
   ```

2. Install dependencies (if not already installed):
   ```bash
   npm install
   ```

3. The tests automatically create new user accounts for each test run with unique credentials.

## Authentication

All tests require user authentication. The tests automatically handle this using the `register()` helper function from `helpers/auth.ts`:

```typescript
import { register } from './helpers/auth';

test.beforeEach(async ({ page }) => {
  await register(page); // Automatically registers a new test user
});
```

The registration process:
1. Navigates to the authentication page
2. Opens the register modal
3. Creates unique credentials for the test:
   - Username: `testuser_${timestamp}_${randomNum}`
   - Email: `test_${timestamp}_${randomNum}@test.com`
   - Password: `@TestPassword123`
   - Name: `Test User ${timestamp}`
4. Submits the registration form
5. Creates a user profile
6. Waits for successful redirect to home page
7. Verifies the registration was successful

## Running the Tests

### Run all tests
```bash
npx playwright test
```

### Run tests in a specific file
```bash
npx playwright test tests/posts.spec.ts
npx playwright test tests/profile.spec.ts
```

### Run tests in headed mode (see browser)
```bash
npx playwright test --headed
```

### Run tests in headed mode for a specific file
```bash
npx playwright test tests/posts.spec.ts --headed
```

### Run tests in debug mode
```bash
npx playwright test --debug
```

### Run a specific test
```bash
npx playwright test -g "should create a gym post successfully"
```

## Test Coverage

### Post Creation Tests (posts.spec.ts)
1. **Create a gym post** - Tests creating a gym workout post with exercises
2. **Create a reading post** - Tests creating a reading progress post
3. **Validation - empty title** - Verifies title validation
4. **Validation - missing duration** - Verifies duration validation
5. **Cancel post creation** - Tests the cancel functionality

### Profile Viewing Tests (profile.spec.ts)
1. **View own profile** - Tests accessing the user's own profile page
2. **Profile header display** - Verifies profile information is displayed
3. **Display user posts** - Verifies posts appear on the profile
4. **Navigate from sidebar** - Tests profile navigation from sidebar
5. **Display user information** - Tests that user info is visible
6. **Load profile with posts** - Tests profile loading with existing posts
7. **Display stats** - Tests profile statistics display
8. **Graceful loading** - Tests that profile loads without errors
9. **Profile by username** - Tests username-based profile navigation
10. **Scroll through posts** - Tests scrolling through profile posts

## Configuration

The test configuration is defined in `playwright.config.ts`:
- **Base URL**: `http://localhost:19293` (default frontend port)
- **Test directory**: `./tests`
- **Browsers**: Chromium, Firefox, WebKit
- **Report**: HTML report

## Viewing Test Results

After running tests, view the HTML report:
```bash
npx playwright show-report
```

## Debugging Failed Tests

1. **With screenshots**: Playwright automatically captures screenshots on test failure
2. **With traces**: Run `npx playwright test --trace on` to collect execution traces
3. **Interactive debug mode**: Run `npx playwright test --debug` for step-by-step debugging

## Notes

- Tests automatically register new unique user accounts before each test
- Each test gets its own isolated user account
- The frontend must be running on `http://localhost:19293`
- Tests use relative URLs based on the base URL configured in `playwright.config.ts`
- Tests wait for network idle state to ensure page loads completely
- Validation tests verify error messages appear correctly

## Requirements

- Node.js 16+
- Playwright 1.60.0+
- Docker and Docker Compose (to run the application)
- All microservices and frontend running via Docker Compose

