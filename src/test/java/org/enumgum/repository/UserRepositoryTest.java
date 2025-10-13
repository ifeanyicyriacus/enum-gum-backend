package org.enumgum.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.enumgum.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserRepositoryTest {
  @Autowired private UserRepository repo;

  @Test
  void saveAndFind() {
    User user = repo.save(User.builder().email("a@b.com").password("hash").build());
    assertThat(user).isNotNull();
    assertThat(repo.existsById(user.getId())).isTrue();
    assertThat(repo.findByEmail("a@b.com")).contains(user);
  }
}
