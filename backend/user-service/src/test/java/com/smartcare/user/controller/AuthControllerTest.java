package com.smartcare.user.controller;

import com.smartcare.user.config.SecurityConfig;
import com.smartcare.user.dto.LoginRequest;
import com.smartcare.user.dto.LoginResponse;
import com.smartcare.user.exception.GlobalExceptionHandler;
import com.smartcare.user.exception.InvalidCredentialsException;
import com.smartcare.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_validCredentials_returns200WithJwtAndNoPassword() throws Exception {
        LoginResponse response = new LoginResponse(
                "eyJhbGciOiJIUzI1NiJ9.payload.signature",
                "Bearer",
                3600L,
                new LoginResponse.UserSummary(1L, "Arun Kumar", "arun.phase3@example.com")
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        String body = """
                {
                  "email": "arun.phase3@example.com",
                  "password": "SmartCare@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("eyJhbGciOiJIUzI1NiJ9.payload.signature"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.fullName").value("Arun Kumar"))
                .andExpect(jsonPath("$.user.email").value("arun.phase3@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void login_invalidCredentials_returns401WithGenericMessage() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        String body = """
                {
                  "email": "arun.phase3@example.com",
                  "password": "WrongPassword!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password."))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
    }

    @Test
    void login_missingEmail_returns400() throws Exception {
        String body = """
                {
                  "password": "SmartCare@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void login_missingPassword_returns400() throws Exception {
        String body = """
                {
                  "email": "arun.phase3@example.com"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void login_emptyPassword_returns400() throws Exception {
        String body = """
                {
                  "email": "arun.phase3@example.com",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void login_malformedEmail_returns400() throws Exception {
        String body = """
                {
                  "email": "not-an-email",
                  "password": "SmartCare@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }
}
