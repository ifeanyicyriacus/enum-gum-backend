package org.enumgum.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class VerificationTokenTest {

  @Test
  void shouldRejectExpired() {
    Instant past = Instant.now().minusSeconds(1);
    VerificationToken token =
        VerificationToken.builder()
            .token("abc")
            .email("a@b.com")
            .expiresAt(past)
            .used(false)
            .build();
    assertThat(token.isExpired()).isTrue();
  }
}
