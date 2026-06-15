package com.apigateway.apigateway.models;

public class CreateProfileRequest {

    private String username;
    private String name;
    private String biography;
    private String profilePicture;

    public CreateProfileRequest() {
    }

    public CreateProfileRequest(String username, String name, String biography) {
        this.username = username;
        this.name = name;
        this.biography = biography;
    }

    public CreateProfileRequest(String username, String name, String biography, String profilePicture) {
        this.username = username;
        this.name = name;
        this.biography = biography;
        this.profilePicture = profilePicture;
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

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }
}
