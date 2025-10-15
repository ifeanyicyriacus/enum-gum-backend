package org.enumgum.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.enumgum.domain.model.RefreshToken;
import org.enumgum.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class RefreshTokenRepositoryTest {

  @Autowired private RefreshTokenRepository repo;
  @Autowired private UserRepository userRepository;

  @Test
  void saveAndFindByFamily() {
    UUID family = UUID.randomUUID();

    userRepository.save(User.builder().email("a@b.com").password("password").build());
    User user = userRepository.findByEmail("a@b.com").orElseThrow();
    UUID userId = user.getId();

    RefreshToken r =
        repo.save(
            RefreshToken.builder()
                .token("rt-456")
                .userId(userId)
                .family(family)
                .expiresAt(Instant.now().plusSeconds(3600))
                .used(false)
                .build());

    assertThat(repo.findByFamily(family)).contains(r);
  }
}
