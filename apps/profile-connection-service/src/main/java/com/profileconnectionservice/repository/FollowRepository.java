package com.profileconnectionservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.profileconnectionservice.model.Follow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    // Find the follow relationship between two profiles
    @Query("SELECT f FROM Follow f WHERE f.initiator = :initiator AND f.target = :target")
    Optional<Follow> findByFollowerAndFollowing(UUID initiator, UUID target);

    // Check if a follow relationship exists
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Follow f WHERE f.initiator = :initiator AND f.target = :target")
    boolean existsByFollowerAndFollowing(UUID initiator, UUID target);

    // Get all profiles that this profile is following
    @Query("SELECT f FROM Follow f WHERE f.initiator = :initiator")
    List<Follow> findByFollower(UUID initiator);

    // Get all followers of a profile (profiles who follow this profile)
    @Query("SELECT f FROM Follow f WHERE f.target = :target")
    List<Follow> findByFollowing(UUID target);

    // Delete all follows where this profile is the follower
    @Modifying
    @Transactional
    @Query("DELETE FROM Follow f WHERE f.initiator = :initiator")
    int deleteByInitiator(UUID initiator);

    // Delete all follows where this profile is the target being followed
    @Modifying
    @Transactional
    @Query("DELETE FROM Follow f WHERE f.target = :target")
    int deleteByTarget(UUID target);
}
