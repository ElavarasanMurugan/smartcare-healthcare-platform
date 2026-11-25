package com.smartcare.user.service;

import com.smartcare.user.dto.UserRequest;
import com.smartcare.user.dto.UserResponse;
import com.smartcare.user.entity.User;
import com.smartcare.user.exception.DuplicateEmailException;
import com.smartcare.user.exception.UserNotFoundException;
import com.smartcare.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Service layer containing business logic for managing SmartCare users.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        User user = request.toEntity(normalizedEmail);
        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User existingUser = findUserOrThrow(id);
        String normalizedEmail = normalizeEmail(request.email());

        // Allow keeping the same email, but reject an email already owned by another user.
        if (!normalizedEmail.equals(existingUser.getEmail())
                && userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        existingUser.setFullName(request.fullName().trim());
        existingUser.setEmail(normalizedEmail);
        existingUser.setPhoneNumber(
                request.phoneNumber() != null ? request.phoneNumber().trim() : null
        );

        User updatedUser = userRepository.save(existingUser);
        return UserResponse.fromEntity(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User existingUser = findUserOrThrow(id);
        userRepository.delete(existingUser);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
