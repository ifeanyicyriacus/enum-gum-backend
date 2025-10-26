package org.enumgum.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TokenProviderTest {
  private TokenProvider tokenProvider;
  private final String testSecret =
      "1234567890123456789012345678901234567890123456789012345678901234";

  private final String email = "a@b.com";
  private final String role = "ADMIN";
  private final UUID orgId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();
  private final UUID familyId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    // We'll create the TokenProvider instance and inject the secret via reflection for testing
    tokenProvider = new JwtTokenProvider();
    ReflectionTestUtils.setField(tokenProvider, "secret", testSecret);
  }

  @Test
  void shouldGenerateValidAccessToken() {
    // When
    String token = tokenProvider.generateAccessToken(userId, email, orgId, role);
    // Then
    assertThat(token).isNotBlank();
    // Basic JWT structure check (header.payload.signature)
    String[] parts = token.split("\\.");
    assertThat(parts).hasSize(3); // Should have 3 parts separated by dots

    // Validate token manually using JJWT (simulate what validateToken might do)
    Jws<Claims> claimsJws = tokenProvider.parseToken(token);
    Claims claims = claimsJws.getBody();

    assertThat(claims.getSubject()).isEqualTo(userId.toString());
    assertThat(claims.get("email")).isEqualTo(email);
    assertThat(claims.get("org")).isEqualTo(orgId.toString());
    assertThat(claims.get("role")).isEqualTo(role);
    assertThat(claims.getExpiration()).isAfter(new Date());
    assertTrue(tokenProvider.validateToken(token));
  }

  @Test
  void shouldGenerateValidRefreshToken() {
    // When
    String token = tokenProvider.generateRefreshToken(userId, familyId);

    assertThat(token).isNotBlank();
    String[] parts = token.split("\\.");
    assertThat(parts).hasSize(3);

    // Validate token manually using JJWT
    Jws<Claims> claimsJws = tokenProvider.parseToken(token);
    Claims claims = claimsJws.getBody();

    assertThat(claims.getSubject()).isEqualTo(userId.toString());
    assertThat(claims.get("family")).isEqualTo(familyId.toString());
    assertThat(claims.getExpiration()).isAfter(new Date());
    assertTrue(tokenProvider.validateToken(token));
  }

  @Test
  void shouldValidateOnlyValidToken() {
    // Given: Generate a valid token
    String token = tokenProvider.generateAccessToken(userId, email, orgId, role);
    String fakeToken = token.substring(0, token.length() - 1) + "a";

    // When: Validate the token
    assert tokenProvider.validateToken(token);
    assert !tokenProvider.validateToken(fakeToken);
  }

  @Test
  void shouldInValidateExpiredToken() {
    // Given: Create an expired token manually for testing
    Instant now = Instant.now();
    Instant expiredInstant = now.minusSeconds(1); // Expired 1 second ago
    Date issuedAt = Date.from(now);
    Date expiresAt = Date.from(expiredInstant);

    String expiredToken =
        tokenProvider.createTokenWithSpecificTimes(userId, issuedAt, expiresAt, testSecret);
    assertFalse(tokenProvider.validateToken(expiredToken));
  }

  @Test
  void shouldRotateRefreshToken() {
    UUID family = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    ReflectionTestUtils.setField(tokenProvider, "secret", testSecret);

    // original provider
    String old = tokenProvider.generateRefreshToken(userId, family);
    String neo = tokenProvider.rotateRefreshToken(old);

    assertThat(neo).isNotEqualTo(old); // different string
    assertThat(tokenProvider.validateToken(neo)).isTrue();
    assertThat(tokenProvider.validateToken(old)).isTrue(); // old still valid (soft rotation)
  }
}
