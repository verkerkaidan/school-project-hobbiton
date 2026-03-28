package com.profileservice.profileservice.controller;

import com.profileservice.profileservice.dao.ProfileDAO;
import com.profileservice.profileservice.dao.ProfileRepository;
import com.profileservice.profileservice.dto.ProfileDTO;
import com.profileservice.profileservice.models.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileRepository profileRepository;
    private final ProfileDAO profileDAO;

    ///  This endpoint ("api/profile/create") will help the user create their profile
    /// unprotected endpoint
    @PostMapping("/create")
    public ResponseEntity<Profile> createProfile(@RequestBody ProfileDTO profileDTO){
        UUID userId = UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if(profileRepository.findById(userId).isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
        }
        Profile profile = profileDAO.createProfile(userId, profileDTO);
        return ResponseEntity.ok(profile);
    }

    ///  This endpoint("api/profile/update") will enable changing aspects of the user's profile
    @PutMapping("/update")
    public ResponseEntity<Profile> updateProfile(@RequestBody ProfileDTO updateProfileDTO){
        UUID userId = UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if(profileRepository.findById(userId).isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User doesn't exists");
        }
        Profile profile = profileDAO.updateProfile(userId, updateProfileDTO);
        return ResponseEntity.ok(profile);
    }

    ///  This endpoint("api/profile/delete") will be able to delete a user's profile
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<String> deleteProfile(@PathVariable("userId") String userId){
        Optional<Profile> profile = profileRepository.findById(UUID.fromString(userId));
        if(profile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found");
        }
        profileRepository.delete(profile.get());
        return ResponseEntity.ok("Profile Succesfully deleted");
    }

    ///  This endpoint("api/profile/userId") will return a user's profile information
    @GetMapping("{userId}")
    public ResponseEntity<Profile> getProfileByID(@PathVariable("userId") String userId){
        Optional<Profile> profile = profileRepository.findById(UUID.fromString(userId));
        if(profile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found");
        }
        return ResponseEntity.ok(profile.get());
    }

}
