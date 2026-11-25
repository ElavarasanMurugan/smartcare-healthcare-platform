package com.smartcare.user.controller;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
    void createUser_validRequest_returns201() throws Exception {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        UserResponse response = new UserResponse(1L, "Aarav Sharma", "aarav@example.com", "9876543210", now, now);
        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com",
                  "phoneNumber": "9876543210"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Aarav Sharma"))
                .andExpect(jsonPath("$.email").value("aarav@example.com"));
    }

    @Test
    void createUser_invalidRequest_returns400() throws Exception {
        String body = """
                {
                  "fullName": "",
                  "email": "invalid-email"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void createUser_duplicateEmail_returns409() throws Exception {
        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new DuplicateEmailException("aarav@example.com"));

        String body = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getAllUsers_returns200() throws Exception {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserResponse(1L, "Aarav Sharma", "aarav@example.com", null, now, now)
        ));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("aarav@example.com"));
    }

    @Test
    void getUserById_found_returns200() throws Exception {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        UserResponse response = new UserResponse(1L, "Aarav Sharma", "aarav@example.com", null, now, now);
        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Aarav Sharma"));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateUser_validRequest_returns200() throws Exception {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        UserResponse response = new UserResponse(1L, "Updated Name", "updated@example.com", "12345", now, now);
        when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(response);

        String body = """
                {
                  "fullName": "Updated Name",
                  "email": "updated@example.com",
                  "phoneNumber": "12345"
                }
                """;

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void deleteUser_returns204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
    }
}
