package org.enumgum.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SignupRequestTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void shouldValidateValidSignupRequest() {
    SignupRequest request = new SignupRequest("test@example", "password123");
    Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);
    assertThat(violations).hasSize(0);
  }

  @Test
  void shouldRejectInvalidEmail() {
    SignupRequest request = new SignupRequest("invalid-email", "password123");
    Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Email should be valid");
  }

  @Test
  void shouldRejectShortPassword() {
    SignupRequest request = new SignupRequest("test@example.com", "short1");
    Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("at least 8 characters");
  }
}
