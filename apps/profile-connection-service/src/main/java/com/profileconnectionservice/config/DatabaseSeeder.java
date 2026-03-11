package com.profileconnectionservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import com.profileconnectionservice.model.Block;
import com.profileconnectionservice.model.Follow;
import com.profileconnectionservice.model.Profile;
import com.profileconnectionservice.repository.BlockRepository;
import com.profileconnectionservice.repository.FollowRepository;
import com.profileconnectionservice.repository.ProfileRepository;

import java.util.UUID;

@Configuration
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only seed if there are no profiles
        if (profileRepository.count() > 0) {
            return;
        }

        // Create mock profiles
        UUID profile1Id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID profile2Id = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
        UUID profile3Id = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        UUID profile4Id = UUID.fromString("d62d0b05-9dad-11d1-80b4-00c04fd430c8");
        UUID profile5Id = UUID.fromString("a1b2c3d4-e5f6-47d8-9e0f-1a2b3c4d5e6f");
        UUID profile6Id = UUID.fromString("b2c3d4e5-f6a7-48e9-af10-2b3c4d5e6f7f");
        UUID profile7Id = UUID.fromString("c3d4e5f6-a7b8-49fa-b011-3c4d5e6f7a8d");
        UUID profile8Id = UUID.fromString("d4e5f6a7-b8c9-4a0b-b112-4d5e6f7a8b9e");
        UUID profile9Id = UUID.fromString("e5f6a7b8-c9da-4b1c-b213-5e6f7a8b9caf");
        UUID profile10Id = UUID.fromString("f6a7b8c9-daeb-4c2d-b314-6f7a8b9cada0");

        Profile profile1 = new Profile();
        profile1.setId(profile1Id);

        Profile profile2 = new Profile();
        profile2.setId(profile2Id);

        Profile profile3 = new Profile();
        profile3.setId(profile3Id);

        Profile profile4 = new Profile();
        profile4.setId(profile4Id);

        Profile profile5 = new Profile();
        profile5.setId(profile5Id);

        Profile profile6 = new Profile();
        profile6.setId(profile6Id);

        Profile profile7 = new Profile();
        profile7.setId(profile7Id);

        Profile profile8 = new Profile();
        profile8.setId(profile8Id);

        Profile profile9 = new Profile();
        profile9.setId(profile9Id);

        Profile profile10 = new Profile();
        profile10.setId(profile10Id);

        profileRepository.save(profile1);
        profileRepository.save(profile2);
        profileRepository.save(profile3);
        profileRepository.save(profile4);
        profileRepository.save(profile5);
        profileRepository.save(profile6);
        profileRepository.save(profile7);
        profileRepository.save(profile8);
        profileRepository.save(profile9);
        profileRepository.save(profile10);

        // Create mock follows - building an interesting follow graph
        // Profile1 follows: Profile2, Profile3, Profile5
        followRepository.save(new Follow(profile1Id, profile2Id));
        followRepository.save(new Follow(profile1Id, profile3Id));
        followRepository.save(new Follow(profile1Id, profile5Id));

        // Profile2 follows: Profile3, Profile4
        followRepository.save(new Follow(profile2Id, profile3Id));
        followRepository.save(new Follow(profile2Id, profile4Id));

        // Profile3 follows: Profile4, Profile6
        followRepository.save(new Follow(profile3Id, profile4Id));
        followRepository.save(new Follow(profile3Id, profile6Id));

        // Profile4 follows: Profile1, Profile5
        followRepository.save(new Follow(profile4Id, profile1Id));
        followRepository.save(new Follow(profile4Id, profile5Id));

        // Profile5 follows: Profile2
        followRepository.save(new Follow(profile5Id, profile2Id));

        // Profile6 follows: Profile1
        followRepository.save(new Follow(profile6Id, profile1Id));

        // Profile7 follows: Profile2, Profile5
        followRepository.save(new Follow(profile7Id, profile2Id));
        followRepository.save(new Follow(profile7Id, profile5Id));

        // Profile8 follows: Profile3, Profile9
        followRepository.save(new Follow(profile8Id, profile3Id));
        followRepository.save(new Follow(profile8Id, profile9Id));

        // Profile9 follows: Profile1, Profile10
        followRepository.save(new Follow(profile9Id, profile1Id));
        followRepository.save(new Follow(profile9Id, profile10Id));

        // Profile10 follows: Profile5, Profile8
        followRepository.save(new Follow(profile10Id, profile5Id));
        followRepository.save(new Follow(profile10Id, profile8Id));

        // Create mock blocks
        // Profile2 blocks Profile4
        blockRepository.save(new Block(profile2Id, profile4Id));

        // Profile3 blocks Profile1
        blockRepository.save(new Block(profile3Id, profile1Id));

        // Profile5 blocks Profile6
        blockRepository.save(new Block(profile5Id, profile6Id));

        // Profile4 blocks Profile6
        blockRepository.save(new Block(profile4Id, profile6Id));

        // Profile6 blocks Profile1 and Profile2
        blockRepository.save(new Block(profile6Id, profile1Id));
        blockRepository.save(new Block(profile6Id, profile2Id));

        // Profile7 blocks Profile3
        blockRepository.save(new Block(profile7Id, profile3Id));

        // Profile9 blocks Profile4
        blockRepository.save(new Block(profile9Id, profile4Id));

        // Profile10 blocks Profile2
        blockRepository.save(new Block(profile10Id, profile2Id));

        System.out.println("Database seeded with mock data");
        System.out.println("Profiles: 10");
        System.out.println("Follows: 17");
        System.out.println("Blocks: 9");
    }
}
