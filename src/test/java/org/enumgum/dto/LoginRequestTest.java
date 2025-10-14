package org.enumgum.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginRequestTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
      //try with resource
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void shouldValidateValidLoginRequest() {
    LoginRequest request = new LoginRequest("test@example", "password123");
    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
    assertThat(violations).isEmpty();
  }

  @Test
  void shouldRejectInvalidEmail() {
    LoginRequest request = new LoginRequest("invalid-email", "password123");
    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Email should be valid");
  }

  @Test
  void shouldRejectBlankEmail() {
    LoginRequest request = new LoginRequest("", "password123");
    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Email is required");
  }

  @Test
  void shouldRejectBlankPassword() {
    LoginRequest request = new LoginRequest("test@example.com", "");
    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
    assertThat(violations).hasSize(2);
    assertThat(violations.iterator().next().getMessage())
        .isEqualTo("Password must be at least 8 characters");
  }
}
