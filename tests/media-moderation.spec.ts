import { test, expect, type Page } from '@playwright/test';
import { Buffer } from 'node:buffer';
import { register } from './helpers/auth';
import {
  adminCredentials,
  authCookieValue,
  getPostByTitle,
  getReportedFiles,
  loginViaApi,
  reportMediaFile,
  type MediaItem,
  type ReportedFile,
} from './helpers/api';

const RED_PNG = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADUlEQVR42mP8z8BQDwAFgwJ/lK3Q6wAAAABJRU5ErkJggg==',
  'base64',
);
const BLUE_PNG = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADUlEQVR42mP8z/CfAQAF/wJ+6Rr8LwAAAABJRU5ErkJggg==',
  'base64',
);

function shortBase64Fragment(fileData: string): string {
  return fileData.slice(0, 80);
}

async function openCreatePostModal(page: Page) {
  const createButton = page.getByRole('button', { name: /create new post/i });
  await expect(createButton).toBeVisible();
  await createButton.click();
  await expect(page.getByRole('heading', { name: /new post/i })).toBeVisible();
}

async function createOtherPostWithTwoMedia(page: Page, title: string) {
  await openCreatePostModal(page);

  await page.locator('label:has-text("Post Type") + button').click();
  await page.getByRole('button').filter({ hasText: 'Other' }).click();

  await page.locator('input[name="title"]').fill(title);
  await page.locator('textarea[name="description"]').fill('E2E post with exactly two uploaded media files.');
  await page.locator('input[type="file"]').setInputFiles([
    { name: 'hobbithon-red-e2e.png', mimeType: 'image/png', buffer: RED_PNG },
    { name: 'hobbithon-blue-e2e.png', mimeType: 'image/png', buffer: BLUE_PNG },
  ]);

  await expect(page.getByText('2/3')).toBeVisible();
  await page.getByRole('button', { name: /create post/i }).click();
  await page.waitForURL('**/profile', { timeout: 30000 });
  await expect(page.getByText(title)).toBeVisible({ timeout: 15000 });
}

async function expectPostDetailsShowBothMedia(page: Page, postId: string, media: MediaItem[]) {
  await page.goto(`/posts/${postId}`);

  await expect(page.locator(`img[alt="Media 1"][src*="${shortBase64Fragment(media[0].fileData)}"]`)).toBeVisible();
  await expect(page.locator(`img[alt="Media 2"][src*="${shortBase64Fragment(media[1].fileData)}"]`)).toHaveCount(1);

  const nextButton = page.locator('button.absolute.right-3.top-1\\/2').first();
  await expect(nextButton).toBeVisible();
  await nextButton.click();
  await expect(page.locator(`img[alt="Media 2"][src*="${shortBase64Fragment(media[1].fileData)}"]`)).toBeVisible();
}

function reportedFileForMedia(reportedFiles: ReportedFile[], media: MediaItem): ReportedFile {
  const match = reportedFiles.find((file) => file.file === media.fileData);
  expect(match, `Expected reported file for media instance ${media.relevantId}`).toBeTruthy();
  return match!;
}

async function openAdminPage(adminPage: Page) {
  await adminPage.goto('/admin');
  await expect(adminPage.getByRole('heading', { name: /^admin$/i })).toBeVisible({ timeout: 15000 });
}

async function reportedImageCard(adminPage: Page, reportedFile: ReportedFile) {
  await adminPage.getByRole('button', { name: /image reports/i }).click();
  const image = adminPage.locator(`img[src*="${shortBase64Fragment(reportedFile.file)}"]`);
  await expect(image).toBeVisible({ timeout: 15000 });
  return image.locator('xpath=ancestor::div[contains(@class, "break-inside-avoid")][1]');
}

async function assignRiskInAdminUi(adminPage: Page, reportedFile: ReportedFile) {
  const card = await reportedImageCard(adminPage, reportedFile);
  await card.locator('input[name="note"]').fill('E2E admin marks this media as risk.');
  await card.locator('select[title="Change content flag"]').selectOption('RISK');
  await card.getByRole('button', { name: /submit contentflag/i }).click();
  await expect(adminPage.getByText(/content flag updated/i)).toBeVisible({ timeout: 10000 });
}

async function deleteMediaInAdminUi(adminPage: Page, reportedFile: ReportedFile) {
  const card = await reportedImageCard(adminPage, reportedFile);
  await card.getByTitle(/ban image and delete all associated reports/i).click();
  await expect(adminPage.locator(`img[alt="File to be deleted"][src*="${shortBase64Fragment(reportedFile.file)}"]`)).toBeVisible();
  await adminPage.getByRole('button', { name: /confirm deletion/i }).click();
  await expect(adminPage.getByText(/file was deleted/i)).toBeVisible({ timeout: 10000 });
}

test.describe('Media moderation', () => {
  test('creates a post with 2 media files, reports both files, flags one, and deletes the other', async ({ page, browser, request }) => {
    const title = `E2E two media moderation ${Date.now()}`;
    const owner = await register(page);
    const ownerToken = await authCookieValue(page.context(), 'token');
    const ownerId = await authCookieValue(page.context(), 'userId');

    let post;
    let mediaToDelete: MediaItem;
    let mediaToFlag: MediaItem;
    let reportedToDelete: ReportedFile;
    let reportedToFlag: ReportedFile;

    await test.step('Create a post with 2 media files attached', async () => {
      await createOtherPostWithTwoMedia(page, title);
      post = await getPostByTitle(request, ownerToken, ownerId, title);
      expect(post.mediaSet).toHaveLength(2);
      [mediaToDelete, mediaToFlag] = post.mediaSet;
    });

    await test.step('View both media files in post details', async () => {
      await expectPostDetailsShowBothMedia(page, post.id, post.mediaSet);
    });

    await test.step('Report 2 different media files', async () => {
      await reportMediaFile(request, ownerToken, ownerId, mediaToDelete.relevantId);
      await reportMediaFile(request, ownerToken, ownerId, mediaToFlag.relevantId);

      const reportedFiles = await getReportedFiles(request, ownerToken);
      reportedToDelete = reportedFileForMedia(reportedFiles, mediaToDelete);
      reportedToFlag = reportedFileForMedia(reportedFiles, mediaToFlag);
      expect(reportedToDelete.hash).not.toBe(reportedToFlag.hash);
    });

    const adminContext = await browser.newContext();
    const adminPage = await adminContext.newPage();
    const admin = adminCredentials();
    await loginViaApi(request, adminContext, admin);

    await test.step('As an admin, assign contentFlag.RISK to one reported file', async () => {
      await openAdminPage(adminPage);
      await assignRiskInAdminUi(adminPage, reportedToFlag);
      await expect
        .poll(async () => {
          const postAfterFlag = await getPostByTitle(request, ownerToken, ownerId, title);
          return postAfterFlag.mediaSet.find((media) => media.relevantId === mediaToFlag.relevantId)?.contentFlag;
        }, { timeout: 15000 })
        .toBe('RISK');
    });

    await test.step('As an admin, delete the other media file from the platform', async () => {
      await deleteMediaInAdminUi(adminPage, reportedToDelete);
      await expect
        .poll(async () => {
          const postAfterDelete = await getPostByTitle(request, ownerToken, ownerId, title);
          return postAfterDelete.mediaSet.some((media) => media.relevantId === mediaToDelete.relevantId);
        }, { timeout: 15000 })
        .toBe(false);
    });

    await adminContext.close();

    await test.step('Check the risky file has a content warning on the feed and deleted file is gone', async () => {
      await page.goto('/');
      await expect(page.getByRole('heading', { name: title })).toBeVisible({ timeout: 15000 });
      const card = page
        .locator('div')
        .filter({ has: page.getByRole('heading', { name: title }) })
        .filter({ hasText: 'View Post' })
        .first();

      await expect(card.getByText('Content Warning')).toBeVisible({ timeout: 15000 });
      await expect(card.locator(`img[src*="${shortBase64Fragment(mediaToDelete.fileData)}"]`)).toHaveCount(0);
      await expect(card.locator(`img[src*="${shortBase64Fragment(mediaToFlag.fileData)}"]`)).toHaveCount(1);
    });

    expect(owner.username).toBeTruthy();
  });
});

