package com.profileservice.profileservice.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CredentialValidatorTest {

    @Test
    void validateUsername_ShouldReturnInput() {
        String username = "john_doe";
        assertEquals(username, CredentialValidator.validateUsername(username));
    }

    @Test
    void validateUsername_ShouldReturnNull_WhenInputIsNull() {
        assertNull(CredentialValidator.validateUsername(null));
    }

    @Test
    void validateUsername_ShouldReturnEmpty_WhenInputIsEmpty() {
        assertEquals("", CredentialValidator.validateUsername(""));
    }

    @Test
    void validateMediaPointer_ShouldReturnInput() {
        String pointer = "http://example.com/profile.jpg";
        assertEquals(pointer, CredentialValidator.validateMediaPointer(pointer));
    }

    @Test
    void validateMediaPointer_ShouldReturnNull_WhenInputIsNull() {
        assertNull(CredentialValidator.validateMediaPointer(null));
    }

    @Test
    void validateLocation_ShouldReturnInput() {
        String location = "New York, USA";
        assertEquals(location, CredentialValidator.validateLocation(location));
    }

    @Test
    void validateLocation_ShouldReturnNull_WhenInputIsNull() {
        assertNull(CredentialValidator.validateLocation(null));
    }
}
