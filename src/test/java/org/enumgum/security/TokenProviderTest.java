package org.enumgum.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.enumgum.config.JwtSecretConfig;
import org.junit.jupiter.api.Test;

class TokenProviderTest {
  private final JwtSecretConfig cfg =
      new JwtSecretConfig(
          "1234567890123456789012345678901234567890123456789012345678901234"); // 256-bit hex
  private final ZoneId zone = ZoneId.of("UTC");
  private final Clock clock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), zone);
  private final TokenProvider provider = new JwtTokenProvider(cfg, clock);

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

  @Test
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
  }
}
