import { test, expect, Browser, Page } from '@playwright/test';
import { login, logout, register } from './helpers/auth';
import { userAfollowsUserB } from './helpers/follow';

// Shared state across tests in this file
let userFollowed: { username: string; email: string; password: string };
let userFollower: { username: string; email: string; password: string };
let chatId: string;

// Register both users in parallel, then set up follow relationship
test.beforeAll(async ({ browser }) => {
    // Register both users in parallel
    const [pageA, pageB] = await Promise.all([
        browser.newPage(),
        browser.newPage(),
    ]);

    [userFollowed, userFollower] = await Promise.all([
        register(pageA),
        register(pageB),
    ]);

    await Promise.all([pageA.close(), pageB.close()]);

    // Follow relationship
    const page = await browser.newPage();
    await userAfollowsUserB(page, userFollowed, userFollower);

    // Create the chat so chatId is available to all tests
    await login(page, userFollowed.email, userFollowed.password);
    await page.goto('/chats', { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.getByRole('button', { name: /New Chat/i }).click();
    await page.locator('input[name="title"]').fill('Super amazing chat');
    await page.getByRole('button', { name: new RegExp(userFollower.username, 'i') }).click();
    await page.getByRole('button', { name: /Start Chatting/i }).click();
    await page.waitForURL(/\/chats\/[0-9a-f-]+/, { timeout: 10000 });

    chatId = page.url().split('/chats/')[1];
    await page.close();
});

test.describe('Chat Service', () => {

    test('Should create a new chat successfully', async ({ page }) => {
        await login(page, userFollowed.email, userFollowed.password);
        await page.goto(`/chats/${chatId}`, { waitUntil: 'domcontentloaded', timeout: 60000 });

        await expect(page.getByRole('heading', { name: /Super amazing chat/i })).toBeVisible();
        await expect(page.getByText(/connected/i)).toBeVisible({ timeout: 5000 });
    });

    test('Should send a message and have the other user receive it', async ({ browser }) => {
        const [senderPage, receiverPage] = await Promise.all([
            browser.newPage(),
            browser.newPage(),
        ]);

        await Promise.all([
            login(senderPage, userFollowed.email, userFollowed.password),
            login(receiverPage, userFollower.email, userFollower.password),
        ]);

        await Promise.all([
            senderPage.goto(`/chats/${chatId}`, { waitUntil: 'domcontentloaded', timeout: 60000 }),
            receiverPage.goto(`/chats/${chatId}`, { waitUntil: 'domcontentloaded', timeout: 60000 }),
        ]);

        // Both users connected
        await Promise.all([
            expect(senderPage.getByText(/connected/i)).toBeVisible({ timeout: 5000 }),
            expect(receiverPage.getByText(/connected/i)).toBeVisible({ timeout: 5000 }),
        ]);

        // Sender sends a message
        await senderPage.getByPlaceholder('Type a message...').fill('Hello from sender!');
        await senderPage.getByRole('button', { name: /Send/i }).click();

        // Sender sees message on the right (own bubble)
        await expect(
            senderPage.locator('.ml-auto', { hasText: 'Hello from sender!' })
        ).toBeVisible({ timeout: 5000 });

        // Receiver sees message on the left (other bubble)
        await expect(
            receiverPage.locator('.mr-auto', { hasText: 'Hello from sender!' })
        ).toBeVisible({ timeout: 5000 });

        await Promise.all([senderPage.close(), receiverPage.close()]);
    });

    test('Should show unread notification badge in sidebar when receiving a message', async ({ browser }) => {
        const [senderPage, receiverPage] = await Promise.all([
            browser.newPage(),
            browser.newPage(),
        ]);

        await Promise.all([
            login(senderPage, userFollowed.email, userFollowed.password),
            login(receiverPage, userFollower.email, userFollower.password),
        ]);

        // Receiver stays on /chats list to see badge, not inside the chat
        await Promise.all([
            senderPage.goto(`/chats/${chatId}`, { waitUntil: 'domcontentloaded', timeout: 60000 }),
            receiverPage.goto('/chats', { waitUntil: 'domcontentloaded', timeout: 60000 }),
        ]);

        await expect(senderPage.getByText(/connected/i)).toBeVisible({ timeout: 15000 });

        // Sender sends a message
        await senderPage.getByPlaceholder('Type a message...').fill('Notification test message');
        await senderPage.getByRole('button', { name: /Send/i }).click();

        // Receiver should see unread badge on the chat card
        const chatCard = receiverPage.locator(`a[href="/chats/${chatId}"]`);
        await expect(chatCard).toBeVisible({ timeout: 8000 });
        await expect(chatCard.locator('span')).toBeVisible({ timeout: 8000 });

        // Badge should show a number > 0
        const badgeText = await chatCard.locator('span').innerText();
        expect(Number(badgeText)).toBeGreaterThan(0);

        await Promise.all([senderPage.close(), receiverPage.close()]);
    });

    test('Should remove a user from the chat', async ({ browser }) => {
        const [ownerPage, kickedPage] = await Promise.all([
            browser.newPage(),
            browser.newPage(),
        ]);

        await Promise.all([
            login(ownerPage, userFollowed.email, userFollowed.password),
            login(kickedPage, userFollower.email, userFollower.password),
        ]);

        await Promise.all([
            ownerPage.goto(`/chats/${chatId}`, { waitUntil: 'domcontentloaded', timeout: 60000 }),
            kickedPage.goto(`/chats/${chatId}`, { waitUntil: 'domcontentloaded', timeout: 60000 }),
        ]);

        await Promise.all([
            expect(ownerPage.getByText(/connected/i)).toBeVisible({ timeout: 15000 }),
            expect(kickedPage.getByText(/connected/i)).toBeVisible({ timeout: 15000 }),
        ]);

        // Owner opens manage users modal and removes the other user
        await ownerPage.getByText('Manage users').click();
        await expect(ownerPage.getByText('Edit current users')).toBeVisible({ timeout: 5000 });

        const userRow = ownerPage.getByText('Existing users in chat')
            .locator('..')
            .getByText(userFollower.username);

        await expect(userRow).toBeVisible({ timeout: 5000 });
        await userRow.click();

        // Modal closes automatically after deletion
        await expect(ownerPage.getByText('Edit current users')).not.toBeVisible({ timeout: 5000 });

        // Kicked user should be redirected to /chats
        await kickedPage.waitForURL(/\/chats$/, { timeout: 20000 });

        // Verify user is no longer in the chat user list
        await ownerPage.getByText('Manage users').click();
        await expect(ownerPage.getByText('Edit current users')).toBeVisible({ timeout: 5000 });
        await expect(
            ownerPage.getByText('Existing users in chat')
                .locator('..')
                .getByText(userFollower.username)
        ).not.toBeVisible({ timeout: 5000 });

        await Promise.all([ownerPage.close(), kickedPage.close()]);
    });
});