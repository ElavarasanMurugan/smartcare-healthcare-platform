package com.smartcare.user.controller;

import com.smartcare.user.dto.UserRegistrationRequest;
import com.smartcare.user.dto.UserRequest;
import com.smartcare.user.dto.UserResponse;
import com.smartcare.user.exception.DuplicateEmailException;
import com.smartcare.user.exception.GlobalExceptionHandler;
import com.smartcare.user.exception.UserNotFoundException;
import com.smartcare.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_validRequest_returns201WithoutCredentials() throws Exception {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        UserResponse response = new UserResponse(1L, "Aarav Sharma", "aarav@example.com", "+91-9876543210", now, now);
        when(userService.createUser(any(UserRegistrationRequest.class))).thenReturn(response);

        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com",
                  "phoneNumber": "+91-9876543210",
                  "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Aarav Sharma"))
                .andExpect(jsonPath("$.email").value("aarav@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void createUser_missingPassword_returns400() throws Exception {
        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void createUser_emptyPassword_returns400() throws Exception {
        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void createUser_shortPassword_returns400() throws Exception {
        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com",
                  "password": "short"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void createUser_passwordExceeding72Bytes_returns400() throws Exception {
        String longPassword = "A".repeat(73);
        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com",
                  "password": "%s"
                }
                """.formatted(longPassword);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void createUser_emptyFullNameAndInvalidEmail_returns400() throws Exception {
        String body = """
                {
                  "fullName": "",
                  "email": "invalid-email",
                  "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/users"))
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void createUser_emailExceedingMaxLength_returns400() throws Exception {
        String longEmail = "a".repeat(245) + "@example.com";
        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "%s",
                  "password": "StrongPassword123!"
                }
                """.formatted(longEmail);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void createUser_malformedJson_returns400() throws Exception {
        String malformedBody = """
                {
                  "fullName": "Aarav Sharma",
                  "email":
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request body."));
    }

    @Test
    void createUser_numericFullName_returns400() throws Exception {
        String body = """
                {
                  "fullName": 12345,
                  "email": "aarav@example.com",
                  "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid field type in JSON request body."))
                .andExpect(jsonPath("$.path").value("/api/v1/users"));
    }

    @Test
    void createUser_booleanEmail_returns400() throws Exception {
        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": true,
                  "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid field type in JSON request body."));
    }

    @Test
    void createUser_arrayFullName_returns400() throws Exception {
        String body = """
                {
                  "fullName": ["Aarav", "Sharma"],
                  "email": "aarav@example.com",
                  "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid field type in JSON request body."));
    }

    @Test
    void createUser_objectPhoneNumber_returns400() throws Exception {
        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com",
                  "phoneNumber": {"number": "9876543210"},
                  "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid field type in JSON request body."));
    }

    @Test
    void createUser_duplicateEmail_returns409() throws Exception {
        when(userService.createUser(any(UserRegistrationRequest.class)))
                .thenThrow(new DuplicateEmailException("aarav@example.com"));

        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com",
                  "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void createUser_databaseUniqueConstraintViolation_returns409WithoutSqlLeak() throws Exception {
        when(userService.createUser(any(UserRegistrationRequest.class)))
                .thenThrow(new DataIntegrityViolationException("ERROR: duplicate key value violates unique constraint \"uk_users_email\""));

        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com",
                  "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("A user with this email already exists."));
    }

    @Test
    void getAllUsers_returns200WithoutCredentials() throws Exception {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserResponse(1L, "Aarav Sharma", "aarav@example.com", null, now, now)
        ));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("aarav@example.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    void getUserById_found_returns200WithoutCredentials() throws Exception {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        UserResponse response = new UserResponse(1L, "Aarav Sharma", "aarav@example.com", null, now, now);
        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Aarav Sharma"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void updateUser_validRequest_returns200() throws Exception {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        UserResponse response = new UserResponse(1L, "Updated Name", "updated@example.com", "9876543210", now, now);
        when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(response);

        String body = """
                {
                  "fullName": "Updated Name",
                  "email": "updated@example.com",
                  "phoneNumber": "9876543210"
                }
                """;

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void updateUser_numericFullName_returns400() throws Exception {
        String body = """
                {
                  "fullName": 999,
                  "email": "aarav@example.com"
                }
                """;

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid field type in JSON request body."));
    }

    @Test
    void updateUser_booleanEmail_returns400() throws Exception {
        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": false
                }
                """;

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid field type in JSON request body."));
    }

    @Test
    void updateUser_arrayFullName_returns400() throws Exception {
        String body = """
                {
                  "fullName": ["Aarav", "Sharma"],
                  "email": "aarav@example.com"
                }
                """;

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid field type in JSON request body."));
    }

    @Test
    void updateUser_objectPhoneNumber_returns400() throws Exception {
        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com",
                  "phoneNumber": {"number": "9876543210"}
                }
                """;

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid field type in JSON request body."));
    }

    @Test
    void updateUser_notFound_returns404() throws Exception {
        when(userService.updateUser(eq(99L), any(UserRequest.class)))
                .thenThrow(new UserNotFoundException(99L));

        String body = """
                {
                  "fullName": "Updated Name",
                  "email": "updated@example.com"
                }
                """;

        mockMvc.perform(put("/api/v1/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteUser_returns204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        doThrow(new UserNotFoundException(99L)).when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/v1/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unexpectedError_returns500WithGenericMessage() throws Exception {
        when(userService.getAllUsers()).thenThrow(new RuntimeException("Internal DB password leak"));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."));
    }
}
