package com.profileservice.profileservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import com.profileservice.profileservice.models.Profile;
import com.profileservice.profileservice.dao.ProfileRepository;

import java.util.UUID;

@Configuration
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public void run(String... args) throws Exception {

        // UUIDs must match profile-connection-service UUIDs
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

        // Create 10 profiles with mock data
        Profile profile1 = new Profile();
        profile1.setUserId(profile1Id);
        profile1.setUsername("alice_reads");
        profile1.setName("Alice Cooper");
        profile1.setBiography("Bookworm and hiking enthusiast. Always up for trail adventures and a good novel.");
        profile1.setMediaPointer("/alice.jpg");
        profile1.setLink("https://aliceblogs.com");
        profile1.setLocation("California, USA");

        Profile profile2 = new Profile();
        profile2.setUserId(profile2Id);
        profile2.setUsername("bob_fitness");
        profile2.setName("Bob Smith");
        profile2.setBiography(
                "Certified fitness trainer. Helping others achieve their health goals, one workout at a time.");
        profile2.setMediaPointer("/bob.jpg");
        profile2.setLink("https://bobfitness.com");
        profile2.setLocation("New York, USA");

        Profile profile3 = new Profile();
        profile3.setUserId(profile3Id);
        profile3.setUsername("charlie_films");
        profile3.setName("Charlie Brown");
        profile3.setBiography("Indie filmmaker and storyteller. Passionate about capturing authentic moments.");
        profile3.setMediaPointer("/charlie.jpg");
        profile3.setLink("https://charliefilms.com");
        profile3.setLocation("Los Angeles, USA");

        Profile profile4 = new Profile();
        profile4.setUserId(profile4Id);
        profile4.setUsername("diana_writes");
        profile4.setName("Diana Prince");
        profile4.setBiography("Author and creative writing enthusiast. Exploring worlds through words.");
        profile4.setMediaPointer("/diana.jpg");
        profile4.setLink("https://dianabooks.com");
        profile4.setLocation("Seattle, USA");

        Profile profile5 = new Profile();
        profile5.setUserId(profile5Id);
        profile5.setUsername("eve_design");
        profile5.setName("Eve Johnson");
        profile5.setBiography("UI/UX Designer crafting beautiful digital experiences.");
        profile5.setMediaPointer("/eve.jpg");
        profile5.setLink("https://evedesign.com");
        profile5.setLocation("Austin, USA");

        Profile profile6 = new Profile();
        profile6.setUserId(profile6Id);
        profile6.setUsername("frank_music");
        profile6.setName("Frank Wilson");
        profile6.setBiography("Music producer and sound engineer. Beats and melodies are my passion.");
        profile6.setMediaPointer("/frank.jpg");
        profile6.setLink("https://frankmusic.com");
        profile6.setLocation("Miami, USA");

        Profile profile7 = new Profile();
        profile7.setUserId(profile7Id);
        profile7.setUsername("grace_yoga");
        profile7.setName("Grace Lee");
        profile7.setBiography("Yoga instructor and wellness coach. Finding balance in mind, body, and spirit.");
        profile7.setMediaPointer("/grace.jpg");
        profile7.setLink("https://graceyoga.com");
        profile7.setLocation("Portland, USA");

        Profile profile8 = new Profile();
        profile8.setUserId(profile8Id);
        profile8.setUsername("henry_science");
        profile8.setName("Henry Davis");
        profile8.setBiography("Science communicator and educator. Making complex ideas simple and fun.");
        profile8.setMediaPointer("/henry.jpg");
        profile8.setLink("https://henryscience.com");
        profile8.setLocation("Boston, USA");

        Profile profile9 = new Profile();
        profile9.setUserId(profile9Id);
        profile9.setUsername("ivy_fashion");
        profile9.setName("Ivy Martinez");
        profile9.setBiography("Fashion designer and style curator. Creative expression through clothing.");
        profile9.setMediaPointer("/ivy.jpg");
        profile9.setLink("https://ivyfashion.com");
        profile9.setLocation("Paris, France");

        Profile profile10 = new Profile();
        profile10.setUserId(profile10Id);
        profile10.setUsername("jack_travel");
        profile10.setName("Jack Taylor");
        profile10.setBiography("Travel blogger and adventurer. Exploring hidden gems around the world.");
        profile10.setMediaPointer("/jack.jpg");
        profile10.setLink("https://jacktravel.com");
        profile10.setLocation("Tokyo, Japan");

        // Add currentUser (dionaidan)
        UUID dionaidanId = UUID.fromString("0c3066a9-99ad-41e3-a936-2ca10d18985e");
        Profile dionaidan = new Profile();
        dionaidan.setUserId(dionaidanId);
        dionaidan.setUsername("dionaidan");
        dionaidan.setName("Dion Aidan");
        dionaidan.setBiography("Passionate about hobbies, cinema, and literature. Let's share our interests!");
        dionaidan.setMediaPointer("/jpeg.png");
        dionaidan.setLink("https://dionaidan.com");
        dionaidan.setLocation("New York, USA");

        // Save all profiles
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
        profileRepository.save(dionaidan);

        System.out.println("Profile database seeded with 11 mock profiles (10 + dionaidan)");
    }
}
