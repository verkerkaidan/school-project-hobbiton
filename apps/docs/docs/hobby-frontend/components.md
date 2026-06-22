---
sidebar_position: 3
---

# Components

All reusable components live in `/components`, organized by feature.

## Layout

| Component | File | Description |
| --- | --- | --- |
| `ResponsiveNavigation` | `layout/ResponsiveNavigation.tsx` | Switches between desktop sidebar and mobile bottom nav based on viewport |
| `DesktopSidebar` | `layout/DesktopSidebar.tsx` | Sidebar navigation with icons, labels, and notification badges |
| `MobileBottomNav` | `layout/MobileBottomNav.tsx` | Fixed bottom navigation bar for mobile |

Navigation items are defined in `constants/navigation.ts` and include: Feed, Discover, Chats, Notifications, Saved, Profile, Settings, and Admin (admin-only).

## Feed

| Component | File | Description |
| --- | --- | --- |
| `Feed` | `feed/Feed.tsx` | Infinite-scroll masonry grid of posts using `useInfiniteQuery` |
| `FeedCard` | `feed/FeedCard.tsx` | Post card with image, hover effects, and content warning overlay |
| `FeedCardAuthor` | `feed/FeedCardAuthor.tsx` | Author info section on feed cards |
| `ProfileHeader` | `feed/ProfileHeader.tsx` | User profile header with avatar and stats |
| `UserPosts` | `feed/UserPosts.tsx` | Grid of a user's posts |

## Post

| Component | File | Description |
| --- | --- | --- |
| `PostPageClient` | `post/PostPageClient.tsx` | Client-side logic for the post detail page |
| `EditPostPageClient` | `post/EditPostPageClient.tsx` | Client-side edit post form |
| `PostAuthorCard` | `post/PostAuthorCard.tsx` | Author card shown on post detail |
| `DeletePostModal` | `post/DeletePostModal.tsx` | Confirmation dialog for post deletion |

## Create Post

| Component | File | Description |
| --- | --- | --- |
| `newPost` | `modals/newPost.tsx` | Post creation modal with hobby type selector |
| `GymExercise` | `createPost/GymExercise.tsx` | Inline form for adding gym exercises (reps, weight, RPE) |
| `ReadingFields` | `createPost/ReadingFields.tsx` | Fields for reading progress and book status |

## Interactions

| Component | File | Description |
| --- | --- | --- |
| `LikeButton` | `interaction/LikeButton.tsx` | Like/unlike toggle button |
| `completeInteraction` | `interaction/completeInteraction.tsx` | Comments section with input and display |

## Chat

| Component | File | Description |
| --- | --- | --- |
| `chatCard` | `chats/chatCard.tsx` | Chat list item with unread badge |
| `MessageBubble` | `chats/MessageBubble.tsx` | Individual message bubble |
| `NewChatModal` | `chats/NewChatModal.tsx` | Dialog to start a new chat |

## Discover

| Component | File | Description |
| --- | --- | --- |
| `DiscoverStepSelector` | `discover/DiscoverStepSelector.tsx` | Category picker (Books, Movies, Exercises, Users) |
| `SearchBar` | `discover/SearchBar.tsx` | Search input with submit |
| `ResultsList` | `discover/ResultsList.tsx` | Search result cards |
| `ResultDetailModal` | `discover/ResultDetailModal.tsx` | Modal showing result details |

## Profile

| Component | File | Description |
| --- | --- | --- |
| `ProfileView` | `profile/ProfileView.tsx` | Full profile display with follow/block actions |
| `EditProfilePictureModal` | `profile/EditProfilePictureModal.tsx` | Upload/change profile picture |
| `UserListModal` | `profile/UserListModal.tsx` | Followers/following list |
| `ReportModal` | `profile/ReportModal.tsx` | Report user dialog |

## Notifications

| Component | File | Description |
| --- | --- | --- |
| `NotificationProvider` | `notifications/NotificationProvider.tsx` | Context provider with WebSocket connection |
| `ChatNotificationProvider` | `notifications/ChatNotificationProvider.tsx` | Context provider for chat unread counts |
| `NotificationList` | `notifications/NotificationList.tsx` | Paginated notification display |

## Settings

| Component | File | Description |
| --- | --- | --- |
| `AccountSettings` | `settings/AccountSettings.tsx` | Username, email, profile info |
| `NotificationSettings` | `settings/NotificationSettings.tsx` | Toggle notification types |
| `SecuritySettings` | `settings/SecuritySettings.tsx` | Password change form |
| `BlockedUsersSection` | `settings/BlockedUsersSection.tsx` | Manage blocked users |
| `BugReportSection` | `settings/BugReportSection.tsx` | Submit bug reports |
| `DeletionSection` | `settings/DeletionSection.tsx` | Account deletion |

## Admin

| Component | File | Description |
| --- | --- | --- |
| `UsersOverview` | `admin/UsersOverview.tsx` | All users table |
| `ReportsTable` | `admin/ReportsTable.tsx` | User reports moderation |
| `BugReportsTable` | `admin/BugReportsTable.tsx` | Bug reports overview |
| `ImageReportCard` | `admin/ImageReportCard.tsx` | Flagged image card |
| `DeletePostsConfirmModal` | `admin/DeletePostsConfirmModal.tsx` | Confirm post deletion |
| `DeleteUserConfirmModal` | `admin/DeleteUserConfirmModal.tsx` | Confirm user deletion |

## UI Primitives

| Component | File | Description |
| --- | --- | --- |
| `UserAvatar` | `ui/UserAvatar.tsx` | Profile picture avatar with fallback |
| `ImageSlider` | `ui/ImageSlider.tsx` | Image carousel for multi-image posts |
| `ImageMenu` | `ui/ImageMenu.tsx` | Three-dot menu on images (report/delete) |
| `Modal` | `ui/Modal.tsx` | Generic modal container |
| `ErrorToastProvider` | `ui/ErrorToastProvider.tsx` | Error toast notifications |
| `ProfilePicProvider` | `ui/ProfilePicProvider.tsx` | Profile picture caching context |

## Auth Modals

| Component | File | Description |
| --- | --- | --- |
| `LoginModal` | `modals/LoginModal.tsx` | Login form with validation |
| `RegisterModal` | `modals/RegisterModal.tsx` | Registration form |
| `TermsOfServiceModal` | `modals/TermsOfServiceModal.tsx` | Terms of service display |
| `BugReportModal` | `modals/BugReportModal.tsx` | Bug report submission |
| `ChatManageUsersModal` | `modals/ChatManageUsersModal.tsx` | Add/remove chat members |
| `LeaveChatmodal` | `modals/LeaveChatmodal.tsx` | Confirm leaving a chat |
