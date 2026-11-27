package com.smartcare.user.service;

import com.smartcare.user.config.JwtProperties;
import com.smartcare.user.dto.LoginRequest;
import com.smartcare.user.dto.LoginResponse;
import com.smartcare.user.entity.User;
import com.smartcare.user.exception.InvalidCredentialsException;
import com.smartcare.user.repository.UserRepository;
import com.smartcare.user.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String TEST_SECRET = "test-only-jwt-secret-key-with-at-least-32-bytes!";

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        JwtProperties properties = new JwtProperties();
        properties.setSecret(TEST_SECRET);
        properties.setExpirationMs(3_600_000L);
        jwtService = new JwtService(properties);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void login_validCredentials_returnsTokenAndSafeUserSummary() {
        String hash = passwordEncoder.encode("SmartCare@123");
        User user = new User("Arun Kumar", "arun.phase3@example.com", "+91-9876543210", hash);
        user.setId(15L);

        when(userRepository.findByEmail("arun.phase3@example.com")).thenReturn(Optional.of(user));

        LoginResponse response = authService.login(new LoginRequest("arun.phase3@example.com", "SmartCare@123"));

        assertNotNull(response.accessToken());
        assertTrue(jwtService.isTokenValid(response.accessToken()));
        assertEquals(15L, jwtService.extractUserId(response.accessToken()));
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        assertEquals(15L, response.user().id());
        assertEquals("Arun Kumar", response.user().fullName());
        assertEquals("arun.phase3@example.com", response.user().email());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_uppercaseEmail_normalizesAndSucceeds() {
        String hash = passwordEncoder.encode("SmartCare@123");
        User user = new User("Arun Kumar", "arun.phase3@example.com", null, hash);
        user.setId(15L);

        when(userRepository.findByEmail("arun.phase3@example.com")).thenReturn(Optional.of(user));

        LoginResponse response = authService.login(new LoginRequest("ARUN.PHASE3@EXAMPLE.COM", "SmartCare@123"));

        assertNotNull(response.accessToken());
        assertEquals("arun.phase3@example.com", response.user().email());
    }

    @Test
    void login_incorrectPassword_throwsInvalidCredentialsException() {
        String hash = passwordEncoder.encode("SmartCare@123");
        User user = new User("Arun Kumar", "arun.phase3@example.com", null, hash);
        user.setId(15L);

        when(userRepository.findByEmail("arun.phase3@example.com")).thenReturn(Optional.of(user));

        InvalidCredentialsException ex = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("arun.phase3@example.com", "WrongPassword!"))
        );
        assertEquals("Invalid email or password.", ex.getMessage());
    }

    @Test
    void login_unregisteredEmail_throwsGenericInvalidCredentialsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        InvalidCredentialsException ex = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("unknown@example.com", "SmartCare@123"))
        );
        assertEquals("Invalid email or password.", ex.getMessage());
    }
}
