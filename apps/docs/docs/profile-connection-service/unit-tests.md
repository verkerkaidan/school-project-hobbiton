---
sidebar_position: 6
---

# Unit Tests

The Profile Connection Service has JUnit 5 tests covering both controller and service layers.

## What is tested

### Controller tests

- **BlockController** — toggling a block returns `true` when creating and `false` when removing, fetching blocked profiles returns the correct set, and user data deletion returns a summary with `deletedCount`, `auditLogId`, `timestamp`, and `message` including the zero-records case.
- **FollowController** — toggling a follow returns `true` when creating and `false` when removing, fetching followers and following lists returns the correct sets, and user data deletion returns a summary with the correct count including bidirectional records.
- **ReportController** — creating a report returns 201 with the persisted record, fetching by target/initiator/status returns the correct lists, fetching pending reports filters to `PENDING` status, updating report status returns the modified record, deleting a report returns 204, and user data deletion returns a summary with audit log details.

### Service tests

- **BlockService** — toggling a block creates a new block when none exists and removes follows in both directions between the profiles, removes the block when one already exists, and rejects self-blocks with `IllegalArgumentException`. `isBlocking` and `isBlockedBy` return the correct boolean. `getBlocked` returns the set of blocked profile IDs. `deleteAllUserData` sums deletions as both initiator and target.
- **FollowService** — toggling a follow creates when missing and deletes when existing, and rejects self-follows. `isFollowing` returns the correct boolean. `getFollowers` and `getFollowing` return the correct sets. `deleteAllUserData` sums both directions.
- **ReportService** — creating a report sets status to `PENDING` and rejects self-reports. Querying by target, initiator, status, and ID returns the expected results. Updating a non-existent report throws `IllegalArgumentException`. `deleteAllUserData` sums both directions.

The tests use Mockito mocks for repositories and Spring MockMvc for controller-layer HTTP assertions. No database is required.

## Not tested

- PostgreSQL itself, Hibernate schema generation, or the database seeder.
- Spring Security configuration beyond the permissive test override.
- Actuator health and Prometheus endpoints.
- End-to-end request flows through the API Gateway.
