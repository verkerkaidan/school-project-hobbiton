package com.userconnectionservice.repository;

import com.userconnectionservice.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockRepository extends JpaRepository<Block, UUID> {

    // Find the block relationship between two users
    Optional<Block> findByBlockerAndBlocked(UUID blocker, UUID blocked);

    // Check if a block relationship exists
    boolean existsByBlockerAndBlocked(UUID blocker, UUID blocked);

    // Get all users that this user has blocked
    List<Block> findByBlocker(UUID blocker);

    // Get all users that have blocked this user
    List<Block> findByBlocked(UUID blocked);
}
