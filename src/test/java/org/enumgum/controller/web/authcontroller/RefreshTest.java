package org.enumgum.controller.web.authcontroller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.enumgum.controller.web.AuthController;
import org.enumgum.domain.error.ErrorCode;
import org.enumgum.dto.TokenResponse;
import org.enumgum.exception.BusinessException;
import org.enumgum.security.JwtSecurityMockConfig;
import org.enumgum.security.TokenProvider;
import org.enumgum.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(JwtSecurityMockConfig.class)
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
      SecurityAutoConfiguration.class,
      SecurityFilterAutoConfiguration.class
    })
public class RefreshTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AuthService authService;
  @MockitoBean private TokenProvider tokenProvider;

  @Test
  void shouldRefreshTokenSuccessfully() throws Exception {
    String oldRefreshToken = "old_refresh_token_xyz";
    String authHeader = "Bearer " + oldRefreshToken;

    TokenResponse expectedResponse =
        new TokenResponse("new_access_token_abc", "new_refresh_token_123", "bearer", 3600);
    when(authService.refresh(oldRefreshToken)).thenReturn(expectedResponse);

    // When: Performing the refresh request
    mockMvc
        .perform(
            post("/api/auth/refresh")
                .header(
                    "Authorization", authHeader)) // Add Authorization header with old refresh token
        // Then: Expect 200 OK and the new token response
        .andExpect(status().isOk()) // 200 for successful refresh
        .andExpect(jsonPath("$.accessToken").value("new_access_token_abc"))
        .andExpect(jsonPath("$.refreshToken").value("new_refresh_token_123"))
        .andExpect(jsonPath("$.tokenType").value("bearer"))
        .andExpect(jsonPath("$.expiresIn").value(3600));

    // Verify: AuthService.refresh was called with the correct token
    verify(authService, times(1)).refresh(oldRefreshToken);
  }

  @Test
  void shouldReturn401WhenRefreshTokenIsReused() throws Exception {
    // Given: A refresh token that has already been used (marked as used in DB)
    String usedRefreshToken = "used_refresh_token_xyz";
    String authHeader = "Bearer " + usedRefreshToken;

    when(authService.refresh(usedRefreshToken))
        .thenThrow(
            new BusinessException(
                ErrorCode.TOKEN_REUSE_DETECTED, "Token reuse detected. Family revoked."));

    mockMvc
        .perform(post("/api/auth/refresh").header("Authorization", authHeader))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("TOKEN_REUSE_DETECTED"));

    verify(authService, times(1)).refresh(usedRefreshToken);
  }
}
