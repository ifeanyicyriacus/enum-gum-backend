package org.enumgum.controller.web.authcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.enumgum.controller.web.AuthController;
import org.enumgum.domain.error.ErrorCode;
import org.enumgum.dto.SignupRequest;
import org.enumgum.dto.SignupResponse;
import org.enumgum.exception.BusinessException;
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
public class SignUpTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper; // For JSON serialization in tests

  @MockitoBean private AuthService authService;
  @MockitoBean private TokenProvider mockTokenProvider;

  private SignupRequest validSignupRequest;

  @BeforeEach
  void setUp() {
    validSignupRequest = new SignupRequest("newuser@example.com", "ValidPass123");
  }

  @Test
  @WithAnonymousUser
  void shouldReturn201OnValidSignup() throws Exception {
    String body = objectMapper.writeValueAsString(validSignupRequest);

    mockMvc
        .perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
  }

  @Test
  @WithAnonymousUser // Explicitly set an unauthenticated context for this test
  void shouldSignupUserSuccessfully() throws Exception {
    SignupResponse expectedResponse = new SignupResponse("Verification email sent successfully");
    when(authService.signup(any(SignupRequest.class))).thenReturn(expectedResponse);

    String body = objectMapper.writeValueAsString(validSignupRequest);
    mockMvc
        .perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("Verification email sent successfully"));

    verify(authService, times(1)).signup(any(SignupRequest.class));
  }

  @Test
  @WithAnonymousUser
  void shouldReturn409WhenEmailAlreadyExistsAndVerified() throws Exception {
    String body = objectMapper.writeValueAsString(validSignupRequest);

    when(authService.signup(any(SignupRequest.class)))
        .thenThrow(new BusinessException(ErrorCode.EMAIL_IN_USE, "Email already in use and verified."));

    mockMvc
        .perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.code").value("EMAIL_IN_USE"));

    verify(authService, times(1)).signup(any(SignupRequest.class));
  }

  @Test
  @WithAnonymousUser
  void shouldResendVerificationWhenEmailExistsButNotVerified() throws Exception {
    String body = objectMapper.writeValueAsString(validSignupRequest);

    SignupResponse expectedResponse =
        new SignupResponse("Verification email resent. Please check your inbox.");
    when(authService.signup(any(SignupRequest.class))).thenReturn(expectedResponse);

    mockMvc
        .perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(
            jsonPath("$.message").value("Verification email resent. Please check your inbox."));

    verify(authService, times(1)).signup(any(SignupRequest.class));
  }
}
