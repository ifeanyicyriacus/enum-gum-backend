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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(JwtSecurityMockConfig.class)
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
      SecurityAutoConfiguration.class,
      SecurityFilterAutoConfiguration.class
    })
public class LogoutTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AuthService authService;
  @MockitoBean private TokenProvider tokenProvider;

  @Test
  void shouldLogoutUserSuccessfully() throws Exception {
    String refreshToken = "refresh_token_xyz";
    String authHeader = "Bearer " + refreshToken;

    doNothing().when(authService).logout(refreshToken);

    mockMvc
        .perform(post("/api/auth/logout").header("Authorization", authHeader))
        .andExpect(status().isNoContent());

    verify(authService, times(1)).logout(refreshToken);
  }

  //  @Test
  //  void shouldClearContextOnLogout() throws Exception {
  //    // given: a logged-in user (we simulate by setting a refresh token row)
  //    User user =
  //        userRepository.save(
  //            User.builder()
  //                .email("logout@enumgum.com")
  //                .password(passwordEncoder.encode("pass"))
  //                .verified(true)
  //                .build());
  //    String refresh = tokenProvider.generateRefreshToken(user.getId(), UUID.randomUUID());
  //    refreshTokenRepository.save(
  //        RefreshToken.builder()
  //            .token(refresh)
  //            .userId(user.getId())
  //            .family(UUID.randomUUID())
  //            .expiresAt(Instant.now().plusSeconds(3600))
  //            .used(false)
  //            .build());
  //
  //    String body = objectMapper.writeValueAsString(new LogoutRequest(refresh));
  //
  //    // when / then
  //    mockMvc
  //        .perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content(body))
  //        .andExpect(status().isNoContent());
  //
  //    assertThat(refreshTokenRepository.findByToken(refresh)).isEmpty();
  //  }
}
