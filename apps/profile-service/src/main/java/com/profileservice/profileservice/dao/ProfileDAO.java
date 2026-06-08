package com.profileservice.profileservice.dao;

import com.profileservice.profileservice.dto.ProfileDTO;
import com.profileservice.profileservice.models.Profile;
import com.profileservice.profileservice.service.CredentialValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class ProfileDAO {
    private final ProfileRepository profileRepository;

    public Profile updateProfile(UUID userId, ProfileDTO profileDTO) {
        Optional<Profile> optionalProfile = profileRepository.findById(userId);
        if(optionalProfile.isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "USER NOT FOUND"
            );
        }
        Profile profile = optionalProfile.get();

        if(!(profileDTO.username == null)) {
            profile.setUsername(profileDTO.username);
        }
        if(!(profileDTO.name == null)) {
            profile.setName(profileDTO.name);
        }
        if(!(profileDTO.biography == null)) {
            // check if this is not too long?
            profile.setBiography(profileDTO.biography);
        }
        if(!(profileDTO.mediaPointer == null)){
            String mediaPointer = CredentialValidator.validateMediaPointer(profileDTO.mediaPointer);
            profile.setMediaPointer(mediaPointer);
        }
        if(!(profileDTO.link == null)) {
            profile.setLink(profileDTO.link);
        }
        if(!(profileDTO.location == null)) {
            profile.setLocation(profileDTO.location);
        }
        profile.setLastEdited(OffsetDateTime.now());
        profileRepository.save(profile);
        return profile;
    }

    public Profile createProfile(UUID userId, ProfileDTO createProfileDTO) {
        String mediaPointer = CredentialValidator.validateMediaPointer(createProfileDTO.mediaPointer);
        String location = CredentialValidator.validateLocation(createProfileDTO.location);
        String username= CredentialValidator.validateUsername(createProfileDTO.username);

        Profile profile = new Profile(
                userId,
                username,
                createProfileDTO.name,
                createProfileDTO.biography,
                mediaPointer,
                createProfileDTO.link,
                location
        );
        profileRepository.save(profile);
        return profile;
    }
}
