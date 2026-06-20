# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [1.53.1] - 2026-06-20

### Changed
- **chore**: tests

## [1.53.0] - 2026-06-20

### Added
- **feat**: Editing posts & Fixing post view

## [1.52.0] - 2026-06-20

### Added
- **feat**: Added not found page

### Changed
- **chore**: Reduce playwright tests for Aidan's services

## [1.51.0] - 2026-06-19

### Added
- **feat**: Delete lists with userID

## [1.50.11] - 2026-06-19

### Changed
- **chore**: Logging for media/interaction/recongition

### Added
- **feat**: Users can now delete their own posts from their profile page; gateway enforces ownership check; E2E test added

### Changed
- **chore**: added end to end testing for chats with full coverage both users side
- **refactor**: Added loki url to gitlab-ci.yml

### Fixed
- **fix**: Fixed the styling of frontend with light mode - dark mode

## [1.50.10] - 2026-06-19

### Changed
- **chore**: Grafana implementation for Bug report, notifications, profile connections

## [1.50.9] - 2026-06-19

### Security
- **security**: Added jwt validation for media-service-elx

## [1.50.8] - 2026-06-19

### Fixed
- **fix**: Remove notifcations wss loop

## [1.50.7] - 2026-06-19

### Changed
- **chore**: Fix grafana ports
- **refactor**: Documentation for posts

## [1.50.6] - 2026-06-19

### Fixed
- **fix**: E2E tests and docusaurus documentation for library service

## [1.50.5] - 2026-06-19

### Fixed
- **hotfix**: grafana rename folder
- 
## [1.50.4] - 2026-06-19

### Changed
- **chore**: CI improvements - split logging, docs, etc.

## [1.50.3] - 2026-06-19

### Fixed
- **hotfix**: grafana

## [1.50.2] - 2026-06-19

### Fixed
- **fix**: Grafana folder fix

## [1.50.1] - 2026-06-19

### Fixed
- **fix**: grafana production fix

## [1.50.0] - 2026-06-19

### Added
- **feat**: User search can now be done

## [1.49.7] - 2026-06-19

### Fixed
- **fix**: Grafana fixing

### Changed
- **refactor**: Added unit testing to production for post-service
- **refactor**: Added grafana testing for chats

## [1.49.6] - 2026-06-19

### Fixed
- **fix**: Grafana fixing


## [1.49.5] - 2026-06-19

### Fixed
- **fix**: admin token fix bug reports

## [1.49.4] - 2026-06-19

### Fixed
- **fix**: fix admin tokens from frontend

## [1.49.3] - 2026-06-19

### Changed
- **refactor**: Page size to 8 to reduce amount of posts loaded
## [1.49.2] - 2026-06-19

### Added
- **feat**: profile & password changes
- 
## [1.49.1] - 2026-06-19

### Added
- **feat**: JWT expiry and redirection in FE

## [1.49.0] - 2026-06-19

### Added
- **feat**: Added the ability to remove everything from a user from the chats.

## [1.48.6] - 2026-06-19

### Added
- **feat**: make sure there is an authentication check for admin functionality in the apigateway

## [1.48.6] - 2026-06-18

### Changed
- **chore**: tests for auth

## [1.48.5] - 2026-06-18

### Changed
- **chore**: remove scps for now to fix prod

## [1.48.4] - 2026-06-18

### Changed
- **chore**: ci

## [1.48.3] - 2026-06-18

### Changed
- **docs**: Docs for Auth, Profile and RabbitMQ

## [1.48.2] - 2026-06-18

### Changed
- **chore**: fix ci

## [1.48.1] - 2026-06-18

### Changed
- **chore**: improve cicd to not die if monitoring breaks

## [1.48.0] - 2026-06-18

### Security
- **security**: Release docs to prod, but protect them by password

## [1.47.2] - 2026-06-18

### Changed
- **refactor**: Adjust content on landing page
- **docs**: Notification docs update
- **chore**: add agent md files to services

## [1.47.1] - 2026-06-18

### Fixed
- **fix**: Container names where sometimes explicitly set, now they are dynamic

## [1.47.0] - 2026-06-18

### Added
- **feat**: Grafana

## [1.46.0] - 2026-06-18

### Added
- **feat**: Added caching in frontend feed
- **feat**: Blocked users don't appear in feed anymore.

## [1.45.0] - 2026-06-18

### Added
- **feat**: Allow user to delete all mentions in notification service

### Changed
- **chore**: Move changelog job to validate stage
- **chore**: E2E tests: Notifications, Bug Report, Profile Connections

## [1.44.0] - 2026-06-18

### Changed
- **refactor**: Add pagination to feed, make admin scrollable, improve request handling by batches

## [1.43.8] - 2026-06-17

### Fixed
- **fix**: auth helper for e2e tests fix

### Changed
- **chore**: Tests for Bug Report service
- **chore**: tests for notification service
- **chore**: Tests for media-service-elx/mediaService en media-service-elx/reportService

## [1.43.7] - 2026-06-17

### Fixed
- **fix**: Banned media check, image upload type, cookies in tos

## [1.43.6] - 2026-06-17

### Changed
- **chore**: hobiist instead of user on landing page

## [1.43.5] - 2026-06-17

### Fixed
- **hotfix**: chat dockerfile again

## [1.43.4] - 2026-06-17

### Changed
- **chore**: remove display name from app
- **chore**: remove certain settings

## [1.43.3] - 2026-06-17

### Fixed
- **hotfix**: dockerfile fix chats

## [1.43.2] - 2026-06-17

### Changed
- **chore**: Display Admin or Hobiist on sidebar instead of hardcoded Owner

## [1.43.1] - 2026-06-17

### Fixed
- **hotfix**: Move chats dockerfile back

## [1.43.0] - 2026-06-17

### Added
- **feat**: added testing to .net, also refactored to fit best practice

## [1.42.1] - 2026-06-17

### Fixed
- **fix**: If database is unavailable or fails, the entire service stops

## [1.42.0] - 2026-06-17

### Changed
- **refactor**: Changed the downloading of the ai model so no code changes are necessary when updating the model version, only cicd/env

## [1.41.1] - 2026-06-17

### Fixed
- **hotfix**: Fix library vars

## [1.41.0] - 2026-06-17

### Fixed
- **fix**: Users weren't able to add or delete other users in a chat

## [1.40.7] - 2026-06-17

### Fixed
- **hotfix**: fix auth

## [1.40.6] - 2026-06-17

### Fixed
- **hotfix**: port rabbitMQ

## [1.40.5] - 2026-06-17

### Fixed
- **hotfix**: fix prod

## [1.40.4] - 2026-06-17

### Fixed
- **hotfix**: docker compose prod fix

## [1.40.3] - 2026-06-17

### Fixed
- **hotfix**: fix gitlabci

## [1.40.2] - 2026-06-17

### Fixed
- **hotfix**: gitlabci variables library service


## [1.40.1] - 2026-06-15

### Fixed
- **hotfix**: auth CORS for prod

## [1.40.0] - 2026-06-16

### Fixed
- **fix**: Notifications would not appear if user (receiving end) is connected earlier to the websocket then user b (sender) sends a chat.

## [1.39.0] - 2026-06-16

### Added
- **feat**: Display amount of posts a user had on their profile

## [1.38.2] - 2026-06-16

### Fixed
- **fix**: added rabbitMQ to prod

## [1.38.1] - 2026-06-16

### Changed
- **chore**: Better styled and more user friendly error handling for media uploading

## [1.38.0] - 2026-06-16

### Added
- **feat**: Added saved lists for user

## [1.37.2] - 2026-06-15

### Fixed
- **hotfix**: auth CORS for prod

## [1.37.1] - 2026-06-15

### Fixed
- **fix**: added queue
- 
## [1.37.0] - 2026-06-15

### Fixed
- **fix**: Users weren't able to create chats easily. Rerouting did not work. On refresh user has 1-infinite new chats with same name & users

### Changed
- **docs**: docs for Profile Connection, Bug Reports & Notifications

## [1.36.0] - 2026-06-15

### Added
- **feat**: Made feed page render left to right, then go to 2nd row

## [1.35.7] - 2026-06-15

## [1.35.6] - 2026-06-15

### Fixed
- **fix**: Removed @controller from chatcontroller
- **fix**: added logging on response from get

## [1.35.5] - 2026-06-15

### Security
- **security**: added unit and integration tests to the library-service

### Fixed
- **fix**: added logging to middleware
- **fix**: Added logging to chat-service get chats
- **fix**: Added temporary sanity check to chat-service

### Changed
- **docs**: Added complete documentation for media-service-elx, recognition-service and interaction-service

## [1.35.4] - 2026-06-12

### Fixed
- **fix**: Fix broken websockets for notification service

## [1.35.3] - 2026-06-12

### Fixed
- **fix**: Report modal being behind components

## [1.35.2] - 2026-06-12

### Fixed
- **hotfix**: missing env variables in docker compose prod

## [1.35.1] - 2026-06-12

### Fixed
- **fix**: In notification connection to chats, & in stead of ? for token

## [1.35.0] - 2026-06-12

### Fixed
- **fix**: added jwt to chat-service for more authorization and security

## [1.34.1] - 2026-06-12

### Fixed
- **fix**: fix post details not loading

## [1.34.0] - 2026-06-12

### Added
- **feat**: Changed and styled the post details page

## [1.33.3] - 2026-06-12

### Fixed
- **hotfix**: CORS issues in Chats & Graphql

## [1.33.2] - 2026-06-08

### Security
- **security**: CORS setup for hobbiton.

## [1.33.1] - 2026-06-12

### Fixed
- **fix**: Profile header showed your pfp instead of the user you're viewing if you changed yours recently

## [1.33.0] - 2026-06-10

### Changed
- **refactor**: Refactor sidebar to be responsive on mobile viewports
- **docs**: docs for chat service update

## [1.32.1] - 2026-06-10

### Fixed
- **fix**: User was allowed to kick user from chat, when already kicked from chat. Now user gets redirected to /chats when kicked from chat.

## [1.32.0] - 2026-06-09

### Added
- **feat**: Added link hobby to post

## [1.31.0] - 2026-06-09

### Added
- **feat**: Terms of Service modal on registration

## [1.30.2] - 2026-06-08

### Fixed
- **fix**: Pfp's are rendered correctly instead of always default or optimistic updated

## [1.30.1] - 2026-06-08

### Fixed
- **fix**: Fetch chats latest updated first && notifications not shown when already in chat

## [1.30.0] - 2026-06-08

### Added
- **feat**: Rewrite of the authentication service to c

## [1.29.0] - 2026-06-08

### Added
- **feat**: Notifications for chat services using websockets && changed chat-service to be spliced into multiple files for maintainability

## [1.28.1] - 2026-06-08

### Fixed
- **fix**: Larger files can now also be uploaded and assigned a contentFlag by ai

## [1.28.0] - 2026-06-08

### Added
- **feat**: Added generic posts to the feed

## [1.27.0] - 2026-06-08

### Added
- **feat**: Users can now update their profile picture


## [1.26.0] - 2026-06-06

### Added
- **feat**: General docker setup for auth in C

## [1.25.0] - 2026-06-07

### Added
- **feat**: User can now report bugs to the admins.

## [1.24.1] - 2026-06-07

### Fixed
- **fix**: register improvement
## [1.24.0] - 2026-06-05

### Changed
- **refactor**: Combine User and UserProfile

## [1.23.1] - 2026-06-05

### Fixed
- **fix**: Recognition service can now also handle webp and jpg images

## [1.23.0] - 2026-06-05

### Added
- **feat**: Admin can view all profiles in a grid

## [1.22.3] - 2026-06-05

### Fixed
- **fix**: docker exiting with code 0

## [1.22.2] - 2026-06-05

### Fixed
- **fix**: Docker not working on other devices because os musl tools

## [1.22.1] - 2026-06-04

### Fixed
- **fix**: Search items now caching individually

## [1.22.0] - 2026-06-03

### Added
- **feat**: jwt-admin
=======
## [1.21.0] - 2026-06-03

### Added
- **feat**: Added Movie and Exercise API's

## [1.20.0] - 2026-06-03

### Changed
- **refactor**: Gave the frontend styling to let users be able to change from light to darkmode

## [1.19.3] - 2026-06-03

### Fixed
- **hotfix**: Mock.env() call is removed as that isnt available in release

## [1.19.2] - 2026-06-03

### Fixed
- **fix**: Add RECOGNITION_MODEL_URL variable to exports of CI

## [1.19.1] - 2026-06-03

### Changed
- **chore**: Added unittests for media_service controllers and models

## [1.19.0] - 2026-06-03

### Added
- **feat**: Add or delete users from chats && styling of chats

## [1.18.3] - 2026-06-03

### Fixed
- **hotfix**: Added create if not exists to migrations (media_service)

## [1.18.2] - 2026-06-01

### Fixed
- **fix**: Rounded corners in FeedCard images

## [1.18.1] - 2026-06-01

### Fixed
- **fix**: Circle logo randomly spinning for no reason really

## [1.18.0] - 2026-06-01

### Added
- **feat**: Add author to feedcard, fix rounded corners on images

## [1.17.2] - 2026-06-01

### Changed
- **chore**: Increase frontend build speed by caching

## [1.17.1] - 2026-06-01

### Changed
- **chore**: Adjust CI/CD to only build changed services

## [1.17.0] - 2026-06-01

### Changed
- **refactor**: Delete old java library server and refactor the rust server to be called "library-service"

## [1.16.4] - 2026-06-01

### Added
- **feat**: Add border to changelog

## [1.16.3] - 2026-06-01

### Fixed
- **hotfix**: Add library url to middleware in docker compose prod

## [1.16.2] - 2026-06-01

### Fixed
- **hotfix**: Remove empty page.tsx

## [1.16.1] - 2026-06-01

### Changed
- **chore**: Updating docker prod

## [1.16.0] - 2026-06-01

### Added
- **feat**: Library service from Rust

## [1.15.0] - 2026-05-30

### Added

- **feat**: Admin approved label prevents the ai model from changing contentflag of reviewed files

## [1.14.1] - 2026-05-29

### Added

- **feat**: Clickable notifications to visit post

## [1.14.0] - 2026-05-29

### Added

- **feat**: Added usernames to chats in stead of UUIDS for security reasons

## [1.13.0] - 2026-05-29

### Added

- **feat**: Notifications on Likes & Comments

## [1.12.2] - 2026-05-29

### Fixed

- **hotfix**: Double variables with space in gitlab-ci.yml error in merge

## [1.12.1] - 2026-05-29

### Fixed

- **hotfix**: Added environment variables to the docker-compose-prod, chat_http_url & chat_ws-url

## [1.12.0] - 2026-05-29

### Changed

- **refactor**: ContentFlag assigning happens at upload and every 24h, instead of every 5mins

## [1.11.2] - 2026-05-29

### Fixed

- **hotfix**: Space in .gitlab-ci.yml caused error at chat_http_url and chat_ws_url

## [1.11.1] - 2026-05-29

### Fixed

- **hotfix**: added cicd variables to gitlab-ci.yml

## [1.11.0] - 2026-05-29

### Added

- **feat**: Added title to chats and added styling of chats

## [1.10.0] - 2026-05-29

### Added

- **feat**: Add PostAuthorCard to view author of posts

## [1.9.3] - 2026-05-28

### Fixed

- **fix**: Content labels are improved: stylewise, when they show, and processing power wise

## [1.9.2] - 2026-05-27

### Fixed

- **fix**: recognition model is send through prod docker

## [1.9.1] - 2026-05-27

### Fixed

- **hotfix**: remove breaking seeder from profile

## [1.9.0] - 2026-05-27

### Changed

- **refactor**: aesthetic rewrite of the home feed

## [1.8.3] - 2026-05-27

### Fixed

- **fix**: recognition model name is now a cicd

## [1.8.2] - 2026-05-27

### Added

- **feat**: Media slider on post detail actually shows relevant images

## [1.8.1] - 2026-05-27

### Fixed

- **hotfix**: PgAdmin port fix

## [1.8.0] - 2026-05-27

### Added

- **feat**: PgAdmin in Prod environment

## [1.7.1] - 2026-05-27

### Fixed

- **hotfix**: add missing interaction url

## [1.7.0] - 2026-05-27

### Changed

- **refactor**: interaction is now handled via middleware instead of frontend<->microservice

## [1.6.0] - 2026-05-27

### Added

- **feat**: Improve UI for media in post creation

## [1.5.0] - 2026-05-27

### Added

- **feat**: Settings overhaul: Add account settings, security settings & profile deletion (FRONTEND). Backend support will follow.

## [1.4.1] - 2026-05-27

### Changed

- **chore**: add docker login to ci/cd

## [1.4.0] - 2026-05-27

### Changed

- **refactor**: Separate auth and main logic to hide sidebar

## [1.3.3] - 2026-05-27

### Fixed

- **hotfix**: Tailwind and other vulnerabilities

## [1.3.2] - 2026-05-27

### Removed

- **remove**: Remove remnants of feed service

## [1.3.1] - 2026-05-26

### Fixed

- **hotfix**: Feedcard component restored after mysterious deletion

## [1.3.0] - 2026-05-26

### Changed

- **refactor**: Media-service has been replaced with media-service-elx with all existing functionalities

## [1.2.0] - 2026-05-26

### Added

- **feat**: Content flag is now assigned on an image classification model instead of fully random

## [1.1.3] - 2026-05-22

### Fixed

- **hotfix**: deployment remove tags for real

## [1.1.2] - 2026-05-22

### Fixed

- **hotfix**: fix deployment

## [1.1.1] - 2026-05-22

### Fixed

- **hotfix**: Fix deployment not deploying

## [1.1.0] - 2026-05-22

### Changed

- **chore**: Added forced changelogs on prs

## [1.0.0] - 2026-05-22

### Added

- Initial project setup with all microservices
