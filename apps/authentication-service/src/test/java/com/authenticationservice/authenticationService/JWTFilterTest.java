package com.authenticationservice.authenticationService;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.authenticationservice.authenticationservice.config.JWTFilter;
import com.authenticationservice.authenticationservice.config.JWTUtil;
import com.authenticationservice.authenticationservice.services.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTFilterTest {

    @Mock private CustomUserDetailsService userService;
    @Mock
    private JWTUtil jwtUtil;
    @Mock private FilterChain filterChain;

    private JWTFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JWTFilter(userService, jwtUtil);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        // Clear security context between tests
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ---- No Authorization header ----

    @Test
    void shouldPassThroughWhenNoAuthHeader() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldPassThroughWhenAuthHeaderIsNull() throws Exception {
        // No header set at all
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userService);
    }

    @Test
    void shouldPassThroughWhenAuthHeaderDoesNotStartWithBearer() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userService);
    }

    // ---- Blank JWT after "Bearer " ----

    @Test
    void shouldReturn400WhenJwtIsBlank() throws Exception {
        request.addHeader("Authorization", "Bearer    ");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Invalid JWT Token in Bearer Header", response.getErrorMessage());
        verify(filterChain, never()).doFilter(any(), any());
    }

    // ---- Valid JWT ----

    @Test
    void shouldAuthenticateUserWithValidToken() throws Exception {
        UserDetails mockUser = mockUserDetails("user-123", "ROLE_USER");
        when(jwtUtil.validateTokenAndRetrieveSubject("valid.jwt.token"))
                .thenReturn(new JWTUtil.TokenClaims("user-123", List.of("ROLE_USER")));
        when(userService.loadUserByUsername("user-123")).thenReturn(mockUser);

        request.addHeader("Authorization", "Bearer valid.jwt.token");
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user-123", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void shouldSetCorrectAuthoritiesFromUserDetails() throws Exception {
        UserDetails mockUser = mockUserDetails("user-123", "ROLE_ADMIN");
        when(jwtUtil.validateTokenAndRetrieveSubject("valid.jwt.token"))
                .thenReturn(new JWTUtil.TokenClaims("user-123", List.of("ROLE_USER")));
        when(userService.loadUserByUsername("user-123")).thenReturn(mockUser);

        request.addHeader("Authorization", "Bearer valid.jwt.token");
        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void shouldReturn200OnValidToken() throws Exception {
        UserDetails mockUser = mockUserDetails("user-123", "ROLE_USER");
        when(jwtUtil.validateTokenAndRetrieveSubject("valid.jwt.token"))
                .thenReturn(new JWTUtil.TokenClaims("user-123", List.of("ROLE_USER")));
        when(userService.loadUserByUsername("user-123")).thenReturn(mockUser);

        request.addHeader("Authorization", "Bearer valid.jwt.token");
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    // ---- Invalid / expired JWT ----

    @Test
    void shouldReturn401WhenTokenIsInvalid() throws Exception {
        when(jwtUtil.validateTokenAndRetrieveSubject("bad.token"))
                .thenThrow(new JWTVerificationException("Invalid token"));

        request.addHeader("Authorization", "Bearer bad.token");
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldReturn401WhenTokenIsExpired() throws Exception {
        when(jwtUtil.validateTokenAndRetrieveSubject("expired.token"))
                .thenThrow(new TokenExpiredException("Token expired", Instant.now()));

        request.addHeader("Authorization", "Bearer expired.token");
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturn401WhenTokenSignatureIsInvalid() throws Exception {
        when(jwtUtil.validateTokenAndRetrieveSubject("tampered.token"))
                .thenThrow(new SignatureVerificationException(Algorithm.HMAC256("wrong")));

        request.addHeader("Authorization", "Bearer tampered.token");
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    // ---- SecurityContext already set ----

    @Test
    void shouldNotOverwriteExistingAuthentication() throws Exception {
        // Pre-set an existing authentication
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken("existing-user", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        UserDetails mockUser = mockUserDetails("new-user", "ROLE_USER");
        when(jwtUtil.validateTokenAndRetrieveSubject("valid.jwt.token"))
                .thenReturn(new JWTUtil.TokenClaims("user-123", List.of("ROLE_USER")));
        when(userService.loadUserByUsername("new-user")).thenReturn(mockUser);

        request.addHeader("Authorization", "Bearer valid.jwt.token");
        filter.doFilterInternal(request, response, filterChain);

        // Should still be the original auth
        assertEquals("existing-user",
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    // ---- helper ----

    private UserDetails mockUserDetails(String username, String role) {
        return User.builder()
                .username(username)
                .password("password")
                .authorities(new SimpleGrantedAuthority(role))
                .build();
    }
}