package com.smartcare.user.service;

import com.smartcare.user.dto.UserRequest;
import com.smartcare.user.dto.UserResponse;
import com.smartcare.user.entity.User;
import com.smartcare.user.exception.DuplicateEmailException;
import com.smartcare.user.exception.UserNotFoundException;
import com.smartcare.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success_normalizesEmailAndReturnsResponse() {
        UserRequest request = new UserRequest("  Aarav Sharma  ", "  Aarav.Sharma@Example.COM ", " +91-9876543210 ");
        when(userRepository.existsByEmail("aarav.sharma@example.com")).thenReturn(false);

        Instant now = Instant.now();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            return user;
        });

        UserResponse response = userService.createUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedInput = userCaptor.getValue();

        assertEquals("Aarav Sharma", savedInput.getFullName());
        assertEquals("aarav.sharma@example.com", savedInput.getEmail());
        assertEquals("+91-9876543210", savedInput.getPhoneNumber());

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("aarav.sharma@example.com", response.email());
    }

    @Test
    void createUser_duplicateEmail_throwsDuplicateEmailException() {
        UserRequest request = new UserRequest("Aarav Sharma", "Aarav@Example.com", null);
        when(userRepository.existsByEmail("aarav@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_found_returnsUserResponse() {
        User user = new User("Priya Nair", "priya@example.com", "9999999999");
        user.setId(10L);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(10L);

        assertEquals(10L, response.id());
        assertEquals("Priya Nair", response.fullName());
        assertEquals("priya@example.com", response.email());
    }

    @Test
    void getUserById_notFound_throwsUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void getAllUsers_returnsMappedList() {
        User u1 = new User("User One", "one@example.com", null);
        u1.setId(1L);
        User u2 = new User("User Two", "two@example.com", "12345");
        u2.setId(2L);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponse> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("one@example.com", users.get(0).email());
        assertEquals("two@example.com", users.get(1).email());
    }

    @Test
    void updateUser_keepingSameEmail_succeeds() {
        User existing = new User("Old Name", "same@example.com", "111");
        existing.setId(5L);

        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserRequest updateRequest = new UserRequest("New Name", " SAME@Example.COM ", "222");
        UserResponse response = userService.updateUser(5L, updateRequest);

        assertEquals("New Name", response.fullName());
        assertEquals("same@example.com", response.email());
        assertEquals("222", response.phoneNumber());
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateUser_changingToExistingEmail_throwsDuplicateEmailException() {
        User existing = new User("Old Name", "old@example.com", "111");
        existing.setId(5L);

        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        UserRequest updateRequest = new UserRequest("Old Name", "taken@example.com", "111");

        assertThrows(DuplicateEmailException.class, () -> userService.updateUser(5L, updateRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_existingUser_deletesEntity() {
        User existing = new User("To Delete", "delete@example.com", null);
        existing.setId(7L);

        when(userRepository.findById(7L)).thenReturn(Optional.of(existing));

        userService.deleteUser(7L);

        verify(userRepository).delete(existing);
    }

    @Test
    void deleteUser_notFound_throwsUserNotFoundException() {
        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(77L));
        verify(userRepository, never()).delete(any());
    }
}
