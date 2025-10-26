package org.enumgum.controller.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@Import(JwtSecurityMockConfig.class) // Adjust import name if necessary
// @WebMvcTest(controllers = AuthController.class) // Explicitly specify the controller under test
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
      SecurityAutoConfiguration.class,
      SecurityFilterAutoConfiguration.class
    })
public class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper; // For JSON serialization in tests

  @MockitoBean // Mock the service dependency
  private AuthService authService;

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
    // Given: A valid signup request and the service should handle it successfully
    SignupResponse expectedResponse = new SignupResponse("Verification email sent successfully");
    when(authService.signup(any(SignupRequest.class))).thenReturn(expectedResponse);

    String body = objectMapper.writeValueAsString(validSignupRequest);
    // When: Performing the signup request
    mockMvc
        .perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
        // Then: Expect 201 Created and the correct response body
        .andExpect(status().isCreated()) // 201 for successful creation
        .andExpect(jsonPath("$.message").value("Verification email sent successfully"));

    // Verify: AuthService.signup was called once
    verify(authService, times(1)).signup(any(SignupRequest.class)); // Optional: Verify interaction
  }

  // Sub-step 11.4: Red - Write Duplicate Verified Email Test
  @Test
  @WithAnonymousUser
  void shouldReturn409WhenEmailAlreadyExistsAndVerified() throws Exception {
    // Given: A signup request with an email that already exists and is verified
    String body = objectMapper.writeValueAsString(validSignupRequest);

    // Mock the service to throw BusinessException for EMAIL_IN_USE
    // We need to throw the actual exception that the GlobalExceptionHandler will catch
    // Assuming BusinessException is mapped correctly in GlobalExceptionHandler
    //        when(authService.signup(any(SignupRequest.class)))
    //                .thenThrow(new RuntimeException("Email already in use")); // Simplified for
    // now, should use BusinessException

    // Better:
    when(authService.signup(any(SignupRequest.class)))
        .thenThrow(
            new BusinessException(ErrorCode.EMAIL_IN_USE, "Email already in use and verified."));

    // When: Performing the signup request
    mockMvc
        .perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
        // Then: Expect 409 Conflict and the correct error code in the response body
        .andExpect(status().isConflict()) // 409 for conflict
        .andExpect(
            jsonPath("$.code")
                .value("EMAIL_IN_USE")); // Assuming GlobalExceptionHandler maps BusinessException
    // correctly

    // Verify: AuthService.signup was called
    verify(authService, times(1)).signup(any(SignupRequest.class)); // Optional
  }

  @Test
  @WithAnonymousUser
  void shouldResendVerificationWhenEmailExistsButNotVerified() throws Exception {
    // Given: A signup request with an email that already exists but is NOT verified
    String body = objectMapper.writeValueAsString(validSignupRequest);

    // Mock the service to return a response indicating resend
    SignupResponse expectedResponse =
        new SignupResponse("Verification email resent. Please check your inbox.");
    when(authService.signup(any(SignupRequest.class))).thenReturn(expectedResponse);

    // When: Performing the signup request
    mockMvc
        .perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
        // Then: Expect 201 Created and the correct response message
        .andExpect(status().isCreated()) // 201 for successful action (resend)
        .andExpect(
            jsonPath("$.message").value("Verification email resent. Please check your inbox."));

    // Verify: AuthService.signup was called
    verify(authService, times(1)).signup(any(SignupRequest.class)); // Optional
  }
}
