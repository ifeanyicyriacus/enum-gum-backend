package org.enumgum.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthFilterTest {
  private JwtAuthFilter jwtAuthFilter;
  private TokenProvider mockTokenProvider;
  private FilterChain mockFilterChain;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  private final String VALID_JWT = "valid_jwt_string";
  private final String BEARER_TOKEN = "Bearer " + VALID_JWT;
  private final UUID userId = UUID.randomUUID();
  private final String email = "test@example.com";
  private final String role = "MEMBER";
  private final UUID orgId = UUID.randomUUID();
  private final String EXPIRED_JWT = "expired_jwt_string";
  private final String INVALID_JWT = "invalid_jwt_string";

  @BeforeEach
  void setup() {
    mockTokenProvider = mock(JwtTokenProvider.class);
    jwtAuthFilter = new JwtAuthFilter(mockTokenProvider);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    mockFilterChain = mock(FilterChain.class);
  }

  @Test
    void shouldExtractTokenFromBearerHeader() throws ServletException, IOException {
        request.addHeader("Authorization", BEARER_TOKEN);

      Jws<Claims> mockClaimsJws = mock(Jws.class);
      Claims mockClaims = mock(Claims.class);
      when(mockClaimsJws.getBody()).thenReturn(mockClaims);
      when(mockClaims.getSubject()).thenReturn(userId.toString());
      when(mockClaims.get("role", String.class)).thenReturn(role);
      // when(mockClaims.get("org", String.class)).thenReturn(orgId.toString());

      when(mockTokenProvider.validateToken(VALID_JWT)).thenReturn(true);
      when(mockTokenProvider.parseToken(VALID_JWT)).thenReturn(mockClaimsJws);
      jwtAuthFilter.doFilterInternal(request, response, mockFilterChain);
      verify(mockTokenProvider).validateToken(VALID_JWT);
  }

  @Test
    void shouldSetAuthenticationInContextWhenTokenIsValid() throws ServletException, IOException {
        request.addHeader("Authorization", BEARER_TOKEN);

        Jws<Claims> mockClaimsJws = mock(Jws.class);
        Claims mockClaims = mock(Claims.class);
        when(mockClaimsJws.getBody()).thenReturn(mockClaims);
        when(mockClaims.getSubject()).thenReturn(userId.toString());
        when(mockClaims.get("role", String.class)).thenReturn(role);

        when(mockTokenProvider.validateToken(VALID_JWT)).thenReturn(true);

      // When: The filter processes the request
      jwtAuthFilter.doFilterInternal(request, response, mockFilterChain);

      // Then: The authentication should be set in the SecurityContext
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      assertThat(authentication).isNotNull();
      assertThat(authentication.getName()).isEqualTo(userId.toString()); // Subject should be the name

      assertThat(authentication.getAuthorities())
              .containsExactly(new SimpleGrantedAuthority("ROLE_" + role));

    }

    @Test
    void shouldNotSetAuthenticationWhenTokenIsInvalid() throws ServletException, IOException {
        // Given: A request with an invalid Bearer token
        request.addHeader("Authorization", "Bearer " + INVALID_JWT);

        // Mock the TokenProvider to return false for validity
        when(mockTokenProvider.validateToken(INVALID_JWT)).thenReturn(false);

        // When: The filter processes the request
        jwtAuthFilter.doFilterInternal(request, response, mockFilterChain);

        // Then: The SecurityContext should remain empty
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
    }

    @Test
    void shouldNotSetAuthenticationWhenNoBearerToken() throws ServletException, IOException {
        // Given: A request without an Authorization header
        // request.addHeader("Authorization", BEARER_TOKEN); // Don't add header

        // When: The filter processes the request
        jwtAuthFilter.doFilterInternal(request, response, mockFilterChain);

        // Then: The SecurityContext should remain empty
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        // And the filter chain should continue
        verify(mockFilterChain).doFilter(request, response);
    }

    @Test
    void shouldContinueFilterChain() throws ServletException, IOException {
        // Given: A request (with or without token)
        request.addHeader("Authorization", BEARER_TOKEN); // Add token, validity doesn't matter for this test
        when(mockTokenProvider.validateToken(VALID_JWT)).thenReturn(true); // Mock validity

        // Mock claims
        Jws<Claims> mockClaimsJws = mock(Jws.class);
        Claims mockClaims = mock(Claims.class);
        when(mockClaimsJws.getBody()).thenReturn(mockClaims);
        when(mockClaims.getSubject()).thenReturn(userId.toString());
        when(mockClaims.get("role", String.class)).thenReturn(role);

        // When: The filter processes the request
        jwtAuthFilter.doFilterInternal(request, response, mockFilterChain);

        // Then: The filter chain should continue
        verify(mockFilterChain).doFilter(request, response);
    }





}
