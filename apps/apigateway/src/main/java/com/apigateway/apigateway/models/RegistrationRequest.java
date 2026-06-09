package com.apigateway.apigateway.models;

public class RegistrationRequest {

    // Auth Service fields
    private String email;
    private String password;

    // Profile Service fields
    private String username;
    private String name; // display name
    private String bio;
    private String profilePictureUrl;
    private Boolean acceptedTOS;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Boolean getAcceptedTOS() {
        return acceptedTOS;
    }

    public void setAcceptedTOS(Boolean acceptedTOS) {
        this.acceptedTOS = acceptedTOS;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public AuthenticationRequest toAuthenticationRequest() {
        return new AuthenticationRequest(email, password);
    }

    public CreateProfileRequest toProfileRequest() {
        // CreateProfileRequest(username, name, biography)
        return new CreateProfileRequest(username, name, bio, acceptedTOS);
    }
}
