package com.profileservice.profileservice.dao;

import com.profileservice.profileservice.dto.ProfileDTO;
import com.profileservice.profileservice.models.Profile;
import com.profileservice.profileservice.service.CredentialValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

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

        if(!profileDTO.username.isEmpty()) {
            profile.setUsername(profileDTO.username);
        }
        if(!profileDTO.name.isEmpty()) {
            profile.setName(profileDTO.name);
        }
        if(!profileDTO.biography.isEmpty()) {
            // check if this is not too long?
            profile.setBiography(profileDTO.biography);
        }
        if(!profileDTO.imageUrl.isEmpty()) {
            String image_url = CredentialValidator.validateImageUrl(profileDTO.imageUrl);
            profile.setImageUrl(image_url);
        }
        if(!profileDTO.link.isEmpty()) {
            profile.setLink(profileDTO.link);
        }
        if(!profileDTO.location.isEmpty()) {
            profile.setLocation(profileDTO.location);
        }

        return profile;
    }

    public Profile createProfile(UUID userId, ProfileDTO createProfileDTO) {
        String image_url = CredentialValidator.validateImageUrl(createProfileDTO.imageUrl);
        String location = CredentialValidator.validateLocation(createProfileDTO.location);
        String username= CredentialValidator.validateUsername(createProfileDTO.username);

        Profile profile = new Profile(
                userId,
                username,
                createProfileDTO.name,
                createProfileDTO.biography,
                image_url,
                createProfileDTO.link,
                location
        );
        profileRepository.save(profile);
        return profile;
    }
}
