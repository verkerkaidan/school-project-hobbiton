package com.profileconnectionservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.profileconnectionservice.model.Block;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockRepository extends JpaRepository<Block, UUID> {

    // Find the block relationship between two profiles
    @Query("SELECT b FROM Block b WHERE b.initiator = :initiator AND b.target = :target")
    Optional<Block> findByBlockerAndBlocked(UUID initiator, UUID target);

    // Check if a block relationship exists
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Block b WHERE b.initiator = :initiator AND b.target = :target")
    boolean existsByBlockerAndBlocked(UUID initiator, UUID target);

    // Get all profiles that this profile has blocked
    @Query("SELECT b FROM Block b WHERE b.initiator = :initiator")
    List<Block> findByBlocker(UUID initiator);

    // Get all profiles that have blocked this profile
    @Query("SELECT b FROM Block b WHERE b.target = :target")
    List<Block> findByBlocked(UUID target);

    // Delete all blocks where this profile is the blocker
    @Modifying
    @Transactional
    @Query("DELETE FROM Block b WHERE b.initiator = :initiator")
    int deleteByInitiator(UUID initiator);

    // Delete all blocks where this profile is the blocked target
    @Modifying
    @Transactional
    @Query("DELETE FROM Block b WHERE b.target = :target")
    int deleteByTarget(UUID target);
}
