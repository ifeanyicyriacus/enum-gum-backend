package org.enumgum.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class RefreshRotateTest {

  @Test
  void shouldRotateFamily() {
    UUID family = UUID.randomUUID();
    RefreshToken rt =
        RefreshToken.builder()
            .token("rt-123")
            .userId(UUID.randomUUID())
            .family(family)
            .expiresAt(Instant.now().plusSeconds(3600))
            .used(false)
            .build();
    assertThat(rt.getFamily()).isEqualTo(family);
  }
}
