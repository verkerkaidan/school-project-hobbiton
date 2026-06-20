package com.profileservice.profileservice.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityServiceTest {

    private final SecurityService securityService = new SecurityService();

    @Test
    void getRole_ShouldReturnUserRole() {
        String role = securityService.getRole("any-user-id");
        assertEquals("USER_ROLE", role);
    }

    @Test
    void getRole_ShouldReturnUserRole_ForAnyInput() {
        assertEquals("USER_ROLE", securityService.getRole(null));
        assertEquals("USER_ROLE", securityService.getRole(""));
        assertEquals("USER_ROLE", securityService.getRole("some-uuid-1234"));
    }
}
