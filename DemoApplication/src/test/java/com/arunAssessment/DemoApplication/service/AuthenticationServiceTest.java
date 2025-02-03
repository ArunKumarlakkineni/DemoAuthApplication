package com.arunAssessment.DemoApplication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext(); // Clear context before each test
    }

    @Test
    void testAuthenticate_Success() {
        String email = "test@example.com";
        String password = "password123";

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);

        when(authenticationManager.authenticate(authToken)).thenReturn(authentication);

        Authentication result = authenticationService.authenticate(email, password);

        assertNotNull(result);
        assertEquals(authentication, result);
        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());

        verify(authenticationManager, times(1)).authenticate(authToken);
    }

    @Test
    void testAuthenticate_Failure() {
        String email = "wrong@example.com";
        String password = "wrongPassword";

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);

        when(authenticationManager.authenticate(authToken)).thenThrow(new RuntimeException("Authentication failed"));

        Exception exception = assertThrows(RuntimeException.class, () ->
                authenticationService.authenticate(email, password)
        );

        assertEquals("Authentication failed", exception.getMessage());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(authenticationManager, times(1)).authenticate(authToken);

        SecurityContextHolder.clearContext(); // Ensure context is cleared after the test
    }
}