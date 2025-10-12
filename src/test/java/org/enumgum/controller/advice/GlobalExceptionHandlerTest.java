package org.enumgum.controller.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.servlet.http.HttpServletRequest;
import org.enumgum.domain.error.ErrorCode;
import org.enumgum.dto.ErrorEnvelope;
import org.enumgum.dto.ErrorEnvelope.ErrorDetail;
import org.enumgum.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  void shouldHandleBusinessException() {
    BusinessException ex = new BusinessException(ErrorCode.EMAIL_IN_USE, "Email already in use");
    HttpServletRequest request = new MockHttpServletRequest();

    ResponseEntity<ErrorEnvelope> response = handler.handleBusinessException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody().code()).isEqualTo(ErrorCode.EMAIL_IN_USE.name());
    assertThat(response.getBody().message()).isEqualTo("Email already in use");
    assertThat(response.getBody().details()).isEmpty();
    assertThat(response.getBody().traceId()).isNotBlank();
  }

  @Test
  void shouldHandleValidationException() {
    FieldError fieldError = new FieldError("SignupRequest", "email", "Invalid email format");
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    ErrorEnvelope.ErrorDetail detail = new ErrorDetail("email", null, "Invalid email format");

    //        when - to be handled later
  }

  @Test
  void shouldHandleGenericException() {
    Exception ex = new Exception("Unexpected Error");
    HttpServletRequest request = new MockHttpServletRequest();

    ResponseEntity<ErrorEnvelope> response = handler.handleGenericException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody().code()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name());
    assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    assertThat(response.getBody().details()).isEmpty();
    assertThat(response.getBody().traceId()).isNotBlank();
  }
}
