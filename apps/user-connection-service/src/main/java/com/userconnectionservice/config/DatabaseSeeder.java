package com.userconnectionservice.config;

import com.userconnectionservice.model.Block;
import com.userconnectionservice.model.Follow;
import com.userconnectionservice.model.User;
import com.userconnectionservice.repository.BlockRepository;
import com.userconnectionservice.repository.FollowRepository;
import com.userconnectionservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import java.util.UUID;

@Configuration
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only seed if there are no users
        if (userRepository.count() > 0) {
            return;
        }

        // Create mock users
        UUID user1Id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID user2Id = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
        UUID user3Id = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        UUID user4Id = UUID.fromString("d62d0b05-9dad-11d1-80b4-00c04fd430c8");
        UUID user5Id = UUID.fromString("a1b2c3d4-e5f6-47d8-9e0f-1a2b3c4d5e6f");
        UUID user6Id = UUID.fromString("b2c3d4e5-f6a7-48e9-af10-2b3c4d5e6f7f");

        User user1 = new User();
        user1.setId(user1Id);

        User user2 = new User();
        user2.setId(user2Id);

        User user3 = new User();
        user3.setId(user3Id);

        User user4 = new User();
        user4.setId(user4Id);

        User user5 = new User();
        user5.setId(user5Id);

        User user6 = new User();
        user6.setId(user6Id);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);
        userRepository.save(user5);
        userRepository.save(user6);

        // Create mock follows - building an interesting follow graph
        // User1 follows: User2, User3, User5
        followRepository.save(new Follow(user1Id, user2Id));
        followRepository.save(new Follow(user1Id, user3Id));
        followRepository.save(new Follow(user1Id, user5Id));

        // User2 follows: User3, User4
        followRepository.save(new Follow(user2Id, user3Id));
        followRepository.save(new Follow(user2Id, user4Id));

        // User3 follows: User4, User6
        followRepository.save(new Follow(user3Id, user4Id));
        followRepository.save(new Follow(user3Id, user6Id));

        // User4 follows: User1, User5
        followRepository.save(new Follow(user4Id, user1Id));
        followRepository.save(new Follow(user4Id, user5Id));

        // User5 follows: User2
        followRepository.save(new Follow(user5Id, user2Id));

        // User6 follows: User1
        followRepository.save(new Follow(user6Id, user1Id));

        // Create mock blocks
        Block block1 = new Block();
        block1.setBlocker(user2Id);
        block1.setBlocked(user4Id);

        Block block2 = new Block();
        block2.setBlocker(user3Id);
        block2.setBlocked(user1Id);

        Block block3 = new Block();
        block3.setBlocker(user5Id);
        block3.setBlocked(user6Id);

        blockRepository.save(block1);
        blockRepository.save(block2);
        blockRepository.save(block3);

        System.out.println("Database seeded with mock data");
        System.out.println("Users: 6");
        System.out.println("Follows: 10");
        System.out.println("Blocks: 3");
    }
}
