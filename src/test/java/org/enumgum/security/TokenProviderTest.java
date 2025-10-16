package org.enumgum.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import java.time.Instant;
import java.util.Date;
import org.enumgum.entity.User;
import org.enumgum.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
class TokenProviderTest {
  private TokenProvider2 tokenProvider;
  private String testSecret = "1234567890123456789012345678901234567890123456789012345678901234";
  private User testUser;

  @Autowired private UserRepository userRepo;

  @BeforeEach
  void setUp() {
    // We'll create the TokenProvider instance and inject the secret via reflection for testing
    tokenProvider = new TokenProvider2();
    ReflectionTestUtils.setField(tokenProvider, "secret", testSecret);

    testUser =
        User.builder().email("test@example.com").password("password").verified(false).build();
    testUser = userRepo.save(testUser);
    assertThat(testUser).isNotNull();
  }

  @Test
  void shouldGenerateValidAccessToken() {
    // When
    String token = tokenProvider.generateAccessToken(testUser);
      System.out.println("Token: " + token);
    // Then
    assertThat(token).isNotBlank();
    // Basic JWT structure check (header.payload.signature)
    String[] parts = token.split("\\.");
    assertThat(parts).hasSize(3); // Should have 3 parts separated by dots

    // Validate token manually using JJWT (simulate what validateToken might do)
    Jws<Claims> claimsJws = tokenProvider.parseToken(token, testSecret);
    Claims claims = claimsJws.getBody();

    assertThat(claims.getSubject()).isEqualTo(testUser.getId().toString());
    assertThat(claims.get("email")).isEqualTo(testUser.getEmail());
    // Check that the token has an expiration date in the future
    assertThat(claims.getExpiration()).isAfter(new Date());
    // Access tokens might not have a 'family' claim by default, check if added later
    // assertThat(claims.get("family")).isNull(); // Or assert specific family if implemented
  }

  @Test
  void shouldGenerateValidRefreshToken() {
    // When
    String token = tokenProvider.generateRefreshToken(testUser);
      System.out.println("Token: " + token);
    // Then
    assertThat(token).isNotBlank();
    String[] parts = token.split("\\.");
    assertThat(parts).hasSize(3);

    // Validate token manually using JJWT
    Jws<Claims> claimsJws = tokenProvider.parseToken(token, testSecret);
    Claims claims = claimsJws.getBody();

    assertThat(claims.getSubject()).isEqualTo(testUser.getId().toString());
    assertThat(claims.get("email")).isEqualTo(testUser.getEmail());
    // Refresh tokens should have a longer expiry time
    assertThat(claims.getExpiration()).isAfter(new Date());
    // Check if family ID or other refresh-specific claims are added later
    // For now, just ensure basic structure and claims are present
  }

  @Test
  void shouldValidateValidTokenAndReturnClaims() {
    // Given: Generate a valid token
    String token = tokenProvider.generateAccessToken(testUser);

    // When: Validate the token
    Claims claims = tokenProvider.validateToken(token);

    // Then: Assert the claims are correct
    assertThat(claims.getSubject()).isEqualTo(testUser.getId().toString());
    assertThat(claims.get("email")).isEqualTo(testUser.getEmail());
    assertThat(claims.getExpiration()).isAfter(new Date());
  }

  @Test
  void shouldThrowExceptionWhenValidatingExpiredToken() {
    // Given: Create an expired token manually for testing
    Instant now = Instant.now();
    Instant expiredInstant = now.minusSeconds(1); // Expired 1 second ago
    Date issuedAt = Date.from(now);
    Date expiresAt = Date.from(expiredInstant);

    String expiredToken =
        tokenProvider.createTokenWithSpecificTimes(testUser, issuedAt, expiresAt, testSecret);

    // When & Then: Validating the expired token should throw ExpiredJwtException
    assertThatThrownBy(() -> tokenProvider.validateToken(expiredToken))
        .isInstanceOf(ExpiredJwtException.class);
  }

  //  private final JwtSecretConfig cfg =
  //      new JwtSecretConfig(
  //          "1234567890123456789012345678901234567890123456789012345678901234"); // 256-bit hex
  //  private final ZoneId zone = ZoneId.of("UTC");
  //  private final Clock clock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), zone);
  //  private final TokenProvider provider = new JwtTokenProvider(cfg, clock);

  //
  //
  //    @Test
  //    void shouldGenerateValidAccessToken() {
  //        UUID userId = UUID.randomUUID();
  //        UUID orgId = UUID.randomUUID();
  //        String token = provider.generateAccessToken(userId, "a@b.com", orgId, "ADMIN");
  //
  //        assertThat(token).isNotBlank();
  //        assertThat(provider.validateToken(token)).isTrue();
  //    }
  //
  //    @Test
  //    void shouldRejectInvalidAccessToken() {
  //        assertThat(provider.validateToken("garbage")).isFalse();
  //    }
  //
  //    @Test
  //    void shouldGenerateValidRefreshTokenWithFamily() {
  //        UUID family = UUID.randomUUID();
  //        String token = provider.generateRefreshToken(UUID.randomUUID(), family);
  //        assertThat(token).isNotBlank();
  //        assertThat(provider.validateToken(token)).isTrue();
  //    }

  //  @Test
  //  void shouldRotateRefresh() {
  //    UUID family = UUID.randomUUID();
  //    UUID userId = UUID.randomUUID();
  //
  //    // original provider
  //    JwtTokenProvider provider = new JwtTokenProvider(cfg, clock);
  //    String old = provider.generateRefreshToken(userId, family);
  //    System.out.println("OLD = " + old);
  //    // advance clock 1 ms
  //    Clock advanced = Clock.offset(clock, Duration.ofMillis(1));
  //    JwtTokenProvider advancedProvider = new JwtTokenProvider(cfg, advanced);
  //
  //    String neo = advancedProvider.rotateRefreshToken(old);
  //    System.out.println("NEW = " + neo);
  //    assertThat(advancedProvider.validateToken(neo)).isTrue();
  //    assertThat(neo).isNotBlank().isNotEqualTo(old);
  //  }

  //    @Test
  //    void shouldFailRotateOnGarbage() {
  //        assertThatThrownBy(() -> provider.rotateRefreshToken("garbage"))
  //                .isInstanceOf(IllegalArgumentException.class);
  //    }

  /*@Test
  void tokenRoundTrip() {
    UUID uid = UUID.randomUUID();
    UUID oid = UUID.randomUUID();

    String access = provider.generateAccessToken(uid, "a@b.com", oid, "ADMIN");
    assertThat(provider.validateToken(access)).isTrue();

    assertThat(provider.validateToken(access)).isTrue();

    UUID family = UUID.randomUUID();
    String refresh = provider.generateRefreshToken(uid, family);
    assertThat(provider.validateToken(refresh)).isTrue();

    Clock advanced = Clock.offset(clock, Duration.ofMillis(1));
    TokenProvider provider2 = new JwtTokenProvider(cfg, advanced);

    String rotated = provider2.rotateRefreshToken(refresh);

    assertThat(rotated).isNotEqualTo(refresh);
    assertThat(provider2.validateToken(rotated)).isTrue();
    assertThat(provider.validateToken(rotated)).isFalse();
    assertThat(provider2.validateToken(refresh)).isFalse();
  }*/
}
