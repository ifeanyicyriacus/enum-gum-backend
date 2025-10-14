package org.enumgum.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.enumgum.domain.model.RefreshToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class RefreshTokenRepositoryTest {

  @Autowired private RefreshTokenRepository repo;

  @Test
  void saveAndFindByFamily() {
    UUID family = UUID.randomUUID();
    RefreshToken r =
        repo.save(
            RefreshToken.builder()
                .token("rt-456")
                .userId(UUID.randomUUID())
                .family(family)
                .expiresAt(Instant.now().plusSeconds(3600))
                .used(false)
                .build());

    assertThat(repo.findByFamily(family)).contains(r);
  }
}
