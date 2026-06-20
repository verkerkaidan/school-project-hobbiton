package com.profileservice.profileservice.config;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.profileservice.profileservice.service.SecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTFilterTest {

    private final JWTUtil jwtTokenUtil = new JWTUtil() {
        @Override
        public String validateTokenAndRetrieveId(String token) {
            if ("valid-token".equals(token)) return "user-id-123";
            if ("malformed-token".equals(token))
                throw new JWTDecodeException("Malformed JWT token");
            return null;
        }
    };

    private final SecurityService securityService = new SecurityService() {
        @Override
        public String getRole(String userId) {
            return "USER_ROLE";
        }
    };

    private JWTFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtFilter = new JWTFilter(jwtTokenUtil, securityService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthentication_WhenValidTokenProvided() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.addHeader("Authorization", "Bearer valid-token");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user-id-123",
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void shouldNotSetAuthentication_WhenNoAuthHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotProcess_WhenAuthHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.addHeader("Authorization", "Basic some-token");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldReturnBadRequest_WhenTokenIsBlank() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.addHeader("Authorization", "Bearer ");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturnBadRequest_WhenTokenIsMalformed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.addHeader("Authorization", "Bearer malformed-token");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldContinueChain_WhenAuthHeaderIsEmpty() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.addHeader("Authorization", "");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
