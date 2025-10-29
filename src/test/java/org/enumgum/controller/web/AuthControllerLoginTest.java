package org.enumgum.controller.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.enumgum.domain.error.ErrorCode;
import org.enumgum.domain.model.RefreshToken;
import org.enumgum.dto.LoginRequest;
import org.enumgum.dto.LogoutRequest;
import org.enumgum.dto.RefreshRequest;
import org.enumgum.dto.TokenResponse;
import org.enumgum.entity.User;
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

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Import(JwtSecurityMockConfig.class)
@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        })
public class AuthControllerLoginTest {


    @Autowired private MockMvc              mockMvc;
    @Autowired private ObjectMapper     objectMapper;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private PasswordEncoder passwordEncoder;

    @MockitoBean private AuthService authService;
    @MockitoBean private TokenProvider tokenProvider;

    @MockitoBean
    RefreshTokenRepository refreshTokenRepository;

    private LoginRequest validLoginRequest;
    private LoginRequest  invalidLoginRequest;
    private LoginRequest  unverifiedEmailLoginRequest;
    private LogoutRequest validLogoutRequest;
    private RefreshRequest validRefreshRequest;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest("newuser@example.com", "ValidPass123");
        invalidLoginRequest = new LoginRequest("nonexistent@example.com", "WrongPass123");
        unverifiedEmailLoginRequest = new LoginRequest("unverified@example.com", "ValidPass123");

        validLogoutRequest = new LogoutRequest("");
        validRefreshRequest = new RefreshRequest("");
    }


    @Test
    @WithAnonymousUser
    void shouldReturn200WithTokensOnValidLogin() throws Exception {
        TokenResponse expectedResponse = new TokenResponse("access_token_abc", "refresh_token_xyz", "bearer", 3600);
        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        // When: Performing the login request
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                // Then: Expect 200 OK and the correct response body structure
                .andExpect(status().isOk()) // 200 for successful login
                .andExpect(jsonPath("$.accessToken").value("access_token_abc"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token_xyz"))
                .andExpect(jsonPath("$.tokenType").value("bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        // Verify: AuthService.login was called once (Optional assertion)
         verify(authService, times(1)).login(any(LoginRequest.class));
    }


    @Test
    @WithAnonymousUser
    void shouldReturn401WhenLoginFails() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                 .thenThrow(new BusinessException(ErrorCode.AUTHENTICATION_ERROR, "Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
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

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unverifiedEmailLoginRequest)))
                .andExpect(status().isUnauthorized()) // 401 for unauthorized
                .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));

         verify(authService, times(1)).login(any(LoginRequest.class));
    }


    @Test
    void shouldReturn401WhenEmailNotVerified() throws Exception {
        String email = "unver@enumgum.com";
        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode("password"))
                .verified(false)
                .build());

        String body = objectMapper.writeValueAsString(new LoginRequest(email, "password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("EMAIL_NOT_VERIFIED"));

        verify(authService, times(1)).login(any(LoginRequest.class));

    }

    @Test
    void shouldReturn200WithNewTokensOnValidRefresh() throws Exception {
        // given: a verified user with a refresh token
        User user = userRepository.save(User.builder()
                .email("refresh@enumgum.com")
                .password(passwordEncoder.encode("pass"))
                .verified(true)
                .build());
        String oldRefresh = tokenProvider.generateRefreshToken(user.getId(), UUID.randomUUID());

        String body = objectMapper.writeValueAsString(new RefreshRequest(oldRefresh));

        // when / then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString());
    }

    @Test
    void shouldClearContextOnLogout() throws Exception {
        // given: a logged-in user (we simulate by setting a refresh token row)
        User user = userRepository.save(User.builder()
                .email("logout@enumgum.com")
                .password(passwordEncoder.encode("pass"))
                .verified(true)
                .build());
        String refresh = tokenProvider.generateRefreshToken(user.getId(), UUID.randomUUID());
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refresh)
                .userId(user.getId())
                .family(UUID.randomUUID())
                .expiresAt(Instant.now().plusSeconds(3600))
                .used(false)
                .build());

        String body = objectMapper.writeValueAsString(new LogoutRequest(refresh));

        // when / then
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        assertThat(refreshTokenRepository.findByToken(refresh)).isEmpty();
    }
}