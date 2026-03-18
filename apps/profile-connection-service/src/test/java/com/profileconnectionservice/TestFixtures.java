package com.profileconnectionservice;

import java.util.UUID;

import com.profileconnectionservice.model.Block;
import com.profileconnectionservice.model.Follow;
import com.profileconnectionservice.model.Report;

/**
 * Shared test fixtures for all unit and integration tests.
 * Provides reusable test data and factory methods.
 */
public class TestFixtures {

    // Test Profile IDs
    public static final UUID PROFILE_ID_1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    public static final UUID PROFILE_ID_2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    public static final UUID PROFILE_ID_3 = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    public static final UUID PROFILE_ID_4 = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");

    // Test Report IDs
    public static final UUID REPORT_ID_1 = UUID.fromString("660e8400-e29b-41d4-a716-446655440000");
    public static final UUID REPORT_ID_2 = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");

    // Test Block IDs
    public static final UUID BLOCK_ID_1 = UUID.fromString("770e8400-e29b-41d4-a716-446655440000");
    public static final UUID BLOCK_ID_2 = UUID.fromString("770e8400-e29b-41d4-a716-446655440001");

    // Test Follow IDs
    public static final UUID FOLLOW_ID_1 = UUID.fromString("880e8400-e29b-41d4-a716-446655440000");
    public static final UUID FOLLOW_ID_2 = UUID.fromString("880e8400-e29b-41d4-a716-446655440001");

    /**
     * Creates a Block entity with the given blocker and blocked profile IDs.
     */
    public static Block createBlock(UUID blocker, UUID blocked) {
        Block block = new Block(blocker, blocked);
        block.setId(UUID.randomUUID());
        return block;
    }

    /**
     * Creates a Follow entity with the given follower and following profile IDs.
     */
    public static Follow createFollow(UUID follower, UUID following) {
        Follow follow = new Follow(follower, following);
        follow.setId(UUID.randomUUID());
        return follow;
    }

    /**
     * Creates a Report entity with the given initiator, target, reason, and
     * description.
     */
    public static Report createReport(UUID initiator, UUID target, Report.ReportReason reason, String description) {
        Report report = new Report(initiator, target, reason, description);
        report.setId(UUID.randomUUID());
        report.setStatus(Report.ReportStatus.PENDING);
        return report;
    }

    /**
     * Creates a Report entity with pending status.
     */
    public static Report createPendingReport(UUID initiator, UUID target) {
        return createReport(initiator, target, Report.ReportReason.HARASSMENT, "Test report");
    }
}
