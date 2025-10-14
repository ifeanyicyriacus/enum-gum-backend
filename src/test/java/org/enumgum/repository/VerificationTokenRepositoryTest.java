package org.enumgum.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.enumgum.domain.model.VerificationToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class VerificationTokenRepositoryTest {
  @Autowired private VerificationTokenRepository repo;

  @Test
  void saveAndFind() {
    VerificationToken v =
        repo.save(
            VerificationToken.builder()
                .token("token")
                .email("a@b.com")
                .expiresAt(Instant.now().plusSeconds(3600))
                .used(false)
                .build());

    assertThat(repo.findByToken("token")).contains(v);
  }
}
