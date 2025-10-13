package org.enumgum.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserTest {

  @Test
  void shouldCreateUserWithValidProperties() {
    // Given
    String email = "test@example.com";
    String password = "hashed_password";

    // When
    User user = User.builder().email(email).password(password).build();

    // Then
    assertThat(user.getEmail()).isEqualTo(email);
    assertThat(user.getPassword()).isEqualTo(password);
    assertThat(user.getVerified()).isFalse();
    assertThat(user.getCreatedAt()).isNull();
    assertThat(user.getUpdatedAt()).isNull();
  }
}
