package com.smartcare.user.service;

import com.smartcare.user.dto.LoginRequest;
import com.smartcare.user.dto.LoginResponse;
import com.smartcare.user.entity.User;
import com.smartcare.user.exception.InvalidCredentialsException;
import com.smartcare.user.repository.UserRepository;
import com.smartcare.user.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Service layer responsible for authenticating users and issuing JWT access tokens.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        String storedHash = user.getPasswordHash();
        if (storedHash == null || !passwordEncoder.matches(request.password(), storedHash)) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateToken(user);
        long expiresInSeconds = jwtService.getExpirationMs() / 1000;

        return new LoginResponse(
                accessToken,
                "Bearer",
                expiresInSeconds,
                LoginResponse.UserSummary.fromEntity(user)
        );
    }
}
