package org.enumgum.controller.web.authcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.enumgum.controller.web.AuthController;
import org.enumgum.domain.error.ErrorCode;
import org.enumgum.dto.LoginRequest;
import org.enumgum.dto.TokenResponse;
import org.enumgum.exception.BusinessException;
import org.enumgum.repository.RefreshTokenRepository;
import org.enumgum.repository.UserRepository;
import org.enumgum.security.JwtSecurityMockConfig;
import org.enumgum.security.TokenProvider;
import org.enumgum.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(JwtSecurityMockConfig.class)
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
      SecurityAutoConfiguration.class,
      SecurityFilterAutoConfiguration.class
    })
public class LoginTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private AuthService authService;
  @MockitoBean private TokenProvider tokenProvider;

  private LoginRequest validLoginRequest;
  private LoginRequest invalidLoginRequest;
  private LoginRequest unverifiedEmailLoginRequest;

  @BeforeEach
  void setUp() {
    validLoginRequest = new LoginRequest("newuser@example.com", "ValidPass123");
    invalidLoginRequest = new LoginRequest("nonexistent@example.com", "WrongPass123");
    unverifiedEmailLoginRequest = new LoginRequest("unverified@example.com", "ValidPass123");
  }

  @Test
  @WithAnonymousUser
  void shouldReturn200WithTokensOnValidLogin() throws Exception {
    TokenResponse expectedResponse =
        new TokenResponse("access_token_abc", "refresh_token_xyz", "bearer", 3600);
    when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access_token_abc"))
        .andExpect(jsonPath("$.refreshToken").value("refresh_token_xyz"))
        .andExpect(jsonPath("$.tokenType").value("bearer"))
        .andExpect(jsonPath("$.expiresIn").value(3600));

    verify(authService, times(1)).login(any(LoginRequest.class));
  }

  @Test
  @WithAnonymousUser
  void shouldReturn401WhenLoginFails() throws Exception {
    when(authService.login(any(LoginRequest.class)))
        .thenThrow(new BusinessException(ErrorCode.AUTHENTICATION_ERROR, "Invalid credentials"));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoginRequest)))
        .andExpect(status().isUnauthorized()) // 401 for unauthorized
        .andExpect(jsonPath("$.code").value("AUTHENTICATION_ERROR"));

    verify(authService, times(1)).login(any(LoginRequest.class));
  }

  @Test
  @WithAnonymousUser
  void shouldReturn401WhenUserNotVerified() throws Exception {
    when(authService.login(any(LoginRequest.class)))
        .thenThrow(new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED, "Email not verified"));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unverifiedEmailLoginRequest)))
        .andExpect(status().isUnauthorized()) // 401 for unauthorized
        .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));

    verify(authService, times(1)).login(any(LoginRequest.class));
  }
}
