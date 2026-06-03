# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

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
