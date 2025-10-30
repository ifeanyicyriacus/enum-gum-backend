package org.enumgum.controller.web.authcontroller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.enumgum.controller.web.AuthController;
import org.enumgum.security.JwtSecurityMockConfig;
import org.enumgum.security.TokenProvider;
import org.enumgum.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
public class VerifyEmailTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AuthService authService;
  @MockitoBean private TokenProvider tokenProvider;

  @Test
  @WithAnonymousUser
  void shouldVerifyEmailSuccessfully() throws Exception {

    String verificationToken = "valid_verification_token_xyz";

    doNothing()
        .when(authService)
        .verifyEmail(
            verificationToken); // Assuming verifyEmail is void or returns specific response

    mockMvc
        .perform(
            post("/api/auth/verify-email")
                .param(
                    "token", verificationToken)) // Pass token as a query parameter or request body
        // Then: Expect 200 OK or 204 No Content (depending on PRD preference for public endpoints)
        .andExpect(status().isOk());

    verify(authService, times(1)).verifyEmail(verificationToken);
  }
}
