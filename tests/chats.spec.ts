import { test, expect, Browser, Page } from '@playwright/test';
import { login, logout, register } from './helpers/auth';
import { userAfollowsUserB } from './helpers/follow';

let userFollowed: { username: string; email: string; password: string };
let userFollower: { username: string; email: string; password: string };
let userExtra: { username: string; email: string; password: string };

async function createChat(page: Page, owner: typeof userFollowed, participant: typeof userFollower, title: string): Promise<string> {
    await login(page, owner.email, owner.password);
    await page.goto('/chats', { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.getByRole('button', { name: /New Chat/i }).click();
    await page.locator('input[name="title"]').fill(title);
    await page.getByRole('button', { name: new RegExp(participant.username, 'i') }).click();
    await page.getByRole('button', { name: /Start Chatting/i }).click();
    await page.waitForURL(/\/chats\/[0-9a-f-]+/, { timeout: 10000 });
    return page.url().split('/chats/')[1];
}

test.beforeAll(async ({ browser }) => {
    const [pageA, pageB, pageC] = await Promise.all([
        browser.newPage(),
        browser.newPage(),
        browser.newPage(),
    ]);

    // Sequential to avoid hammering the registration endpoint simultaneously
    userFollowed = await register(pageA);
    userFollower = await register(pageB);
    userExtra = await register(pageC);

    await Promise.all([pageA.close(), pageB.close(), pageC.close()]);

    const page = await browser.newPage();
    await userAfollowsUserB(page, userFollowed, userFollower);
    await userAfollowsUserB(page, userExtra, userFollowed);
    await page.close();
});

test.describe('Chat Service', () => {

    test('MUST HAVE: Gebruiker start chat met volger', async ({ page }) => {
        await login(page, userFollowed.email, userFollowed.password);
        await page.goto('/chats', { waitUntil: 'domcontentloaded', timeout: 60000 });
        await page.getByRole('button', { name: /New Chat/i }).click();
        await page.locator('input[name="title"]').fill('Super amazing chat');
        await page.getByRole('button', { name: new RegExp(userFollower.username, 'i') }).click();
        await page.getByRole('button', { name: /Start Chatting/i }).click();
        await page.waitForURL(/\/chats\/[0-9a-f-]+/, { timeout: 10000 });

        await expect(page.getByRole('heading', { name: /Super amazing chat/i })).toBeVisible();
        await expect(page.getByText(/connected/i)).toBeVisible({ timeout: 5000 });
    });

    test('MUST HAVE: Gebruiker stuurt bericht, andere gebruiker ontvangt het', async ({ browser }) => {
        const [senderPage, receiverPage] = await Promise.all([
            browser.newPage(),
            browser.newPage(),
        ]);

        const chatId = await createChat(senderPage, userFollowed, userFollower, 'Message test');

        await login(receiverPage, userFollower.email, userFollower.password);

        // senderPage is already on the chat page after createChat, only navigate receiverPage
        await receiverPage.goto(`/chats/${chatId}`, { waitUntil: 'domcontentloaded', timeout: 60000 });

        await Promise.all([
            expect(senderPage.getByText(/connected/i)).toBeVisible({ timeout: 15000 }),
            expect(receiverPage.getByText(/connected/i)).toBeVisible({ timeout: 15000 }),
        ]);

        await senderPage.getByPlaceholder('Type a message...').fill('Hello from sender!');
        await senderPage.getByRole('button', { name: /Send/i }).click();

        await expect(
            senderPage.locator('.ml-auto', { hasText: 'Hello from sender!' })
        ).toBeVisible({ timeout: 5000 });

        await expect(
            receiverPage.locator('.mr-auto', { hasText: 'Hello from sender!' })
        ).toBeVisible({ timeout: 5000 });

        await Promise.all([senderPage.close(), receiverPage.close()]);
    });

    test('SHOULD HAVE: Melding bij nieuw bericht wanneer ontvanger niet in chat', async ({ browser }) => {
        const [senderPage, receiverPage] = await Promise.all([
            browser.newPage(),
            browser.newPage(),
        ]);

        const chatId = await createChat(senderPage, userFollowed, userFollower, 'Notification test');

        await login(receiverPage, userFollower.email, userFollower.password);
        await receiverPage.goto('/chats', { waitUntil: 'domcontentloaded', timeout: 60000 });

        await expect(senderPage.getByText(/connected/i)).toBeVisible({ timeout: 15000 });

        await senderPage.getByPlaceholder('Type a message...').fill('Notification test message');
        await senderPage.getByRole('button', { name: /Send/i }).click();

        const chatCard = receiverPage.locator(`a[href="/chats/${chatId}"]`);
        await expect(chatCard).toBeVisible({ timeout: 8000 });
        await expect(chatCard.locator('span')).toBeVisible({ timeout: 8000 });

        const badgeText = await chatCard.locator('span').innerText();
        expect(Number(badgeText)).toBeGreaterThan(0);

        await Promise.all([senderPage.close(), receiverPage.close()]);
    });

    test('MUST HAVE: Verwijderen van gebruiker uit chat zet andere gebruiker eruit', async ({ browser }) => {
        const [ownerPage, kickedPage] = await Promise.all([
            browser.newPage(),
            browser.newPage(),
        ]);

        const chatId = await createChat(ownerPage, userFollowed, userFollower, 'Remove user test');

        await login(kickedPage, userFollower.email, userFollower.password);
        await kickedPage.goto(`/chats/${chatId}`, { waitUntil: 'domcontentloaded', timeout: 60000 });

        await Promise.all([
            expect(ownerPage.getByText(/connected/i)).toBeVisible({ timeout: 15000 }),
            expect(kickedPage.getByText(/connected/i)).toBeVisible({ timeout: 15000 }),
        ]);

        await ownerPage.getByText('Manage users').click();
        await expect(ownerPage.getByText('Edit current users')).toBeVisible({ timeout: 5000 });

        const userRow = ownerPage.getByText('Existing users in chat')
            .locator('..')
            .getByText(userFollower.username);

        await expect(userRow).toBeVisible({ timeout: 5000 });
        await userRow.click();

        await expect(ownerPage.getByText('Edit current users')).not.toBeVisible({ timeout: 5000 });
        await kickedPage.waitForURL(/\/chats$/, { timeout: 20000 });

        await ownerPage.getByText('Manage users').click();
        await expect(ownerPage.getByText('Edit current users')).toBeVisible({ timeout: 5000 });
        await expect(
            ownerPage.getByText('Existing users in chat')
                .locator('..')
                .getByText(userFollower.username)
        ).not.toBeVisible({ timeout: 5000 });

        await Promise.all([ownerPage.close(), kickedPage.close()]);
    });

    test('MUST HAVE: Verlaten van chat als gebruiker, chat niet meer zichtbaar in lijst', async ({ browser }) => {
        const [ownerPage, userPage] = await Promise.all([
            browser.newPage(),
            browser.newPage(),
        ]);

        const chatId = await createChat(ownerPage, userFollowed, userFollower, 'Leave chat test');

        await login(userPage, userFollower.email, userFollower.password);
        await userPage.goto(`/chats/${chatId}`, { waitUntil: 'domcontentloaded', timeout: 60000 });
        await expect(userPage.getByText(/connected/i)).toBeVisible({ timeout: 5000 });

        await userPage.getByText('Leave chat', { exact: true }).click();
        await expect(userPage.getByText('Are you sure you want to leave this chat?')).toBeVisible();
        await userPage.getByRole('button', { name: /Leave chat/i }).click();
        await userPage.waitForURL('/chats', { timeout: 10000 });
        await expect(
            userPage.getByRole('link', { name: /Leave chat test/i })
        ).not.toBeVisible({ timeout: 5000 });

        await Promise.all([ownerPage.close(), userPage.close()]);
    });

    test('MUST HAVE: Bij verwijdering uit chat, chat niet meer zichtbaar voor gebruiker', async ({ browser }) => {
        const [ownerPage, kickedPage] = await Promise.all([
            browser.newPage(),
            browser.newPage(),
        ]);

        const chatId = await createChat(ownerPage, userFollowed, userFollower, 'Removed chat test');

        await login(kickedPage, userFollower.email, userFollower.password);
        await kickedPage.goto(`/chats/${chatId}`, { waitUntil: 'domcontentloaded', timeout: 60000 });

        await Promise.all([
            expect(ownerPage.getByText(/connected/i)).toBeVisible({ timeout: 15000 }),
            expect(kickedPage.getByText(/connected/i)).toBeVisible({ timeout: 15000 }),
        ]);

        await ownerPage.getByText('Manage users').click();
        await expect(ownerPage.getByText('Edit current users')).toBeVisible({ timeout: 5000 });

        const userRow = ownerPage.getByText('Existing users in chat')
            .locator('..')
            .getByText(userFollower.username);
        await expect(userRow).toBeVisible({ timeout: 5000 });
        await userRow.click();

        await expect(ownerPage.getByText('Edit current users')).not.toBeVisible({ timeout: 5000 });
        await kickedPage.waitForURL(/\/chats$/, { timeout: 20000 });

        await expect(
            kickedPage.getByRole('link', { name: /xRemoved chat test/i })
        ).not.toBeVisible({ timeout: 5000 });

        await Promise.all([ownerPage.close(), kickedPage.close()]);
    });

    test('SHOULD HAVE: Updaten van chat titel zichtbaar voor beide gebruikers', async ({ browser }) => {
        const [pageA, pageB] = await Promise.all([
            browser.newPage(),
            browser.newPage(),
        ]);

        const chatId = await createChat(pageA, userFollowed, userFollower, 'Original title');

        await login(pageB, userFollower.email, userFollower.password);
        await pageB.goto(`/chats/${chatId}`, { waitUntil: 'domcontentloaded', timeout: 60000 });

        await Promise.all([
            expect(pageA.getByText(/connected/i)).toBeVisible({ timeout: 15000 }),
            expect(pageB.getByText(/connected/i)).toBeVisible({ timeout: 15000 }),
        ]);

        await pageA.getByAltText('Edit title').click();
        await pageA.getByRole('textbox').first().fill('Updated title');
        await pageA.getByRole('textbox').first().press('Enter');

        await expect(pageA.getByRole('heading', { name: /Updated title/i })).toBeVisible({ timeout: 5000 });

        await pageB.reload({ waitUntil: 'domcontentloaded' });
        await expect(pageB.getByRole('heading', { name: /Updated title/i })).toBeVisible({ timeout: 5000 });

        await Promise.all([pageA.close(), pageB.close()]);
    });

    test('SHOULD HAVE: Als gebruiker A, volger toevoegen aan chat', async ({ browser }) => {
        const [ownerPage, extraPage] = await Promise.all([
            browser.newPage(),
            browser.newPage(),
        ]);

        const chatId = await createChat(ownerPage, userFollowed, userFollower, 'Add follower test');
        await login(extraPage, userFollower.email, userFollower.password);

        await ownerPage.getByText('Manage users').click();
        await expect(ownerPage.getByText('Edit current users')).toBeVisible({ timeout: 5000 });

        // Remove userFollower from chat so they appear in Followers list
        const userRow = ownerPage.getByText('Existing users in chat')
            .locator('..')
            .getByText(userFollower.username);
        await expect(userRow).toBeVisible({ timeout: 5000 });
        await userRow.click();

        await expect(ownerPage.getByText('Edit current users')).not.toBeVisible({ timeout: 5000 });

        // Re-open panel and add them back from Followers list
        await ownerPage.getByText('Manage users').click();
        await expect(ownerPage.getByText('Edit current users')).toBeVisible({ timeout: 5000 });

        const followerEntry = ownerPage.getByText('Followers')
            .locator('..')
            .getByText(userFollower.username);
        await expect(followerEntry).toBeVisible({ timeout: 10000 });
        await followerEntry.click();

        await expect(ownerPage.getByText('Edit current users')).not.toBeVisible({ timeout: 5000 });

        await extraPage.goto('/chats', { waitUntil: 'domcontentloaded', timeout: 60000 });
        await expect(
            extraPage.getByRole('link', { name: /Add follower test/i })
        ).toBeVisible({ timeout: 5000 });

        await Promise.all([ownerPage.close(), extraPage.close()]);
    });
});
