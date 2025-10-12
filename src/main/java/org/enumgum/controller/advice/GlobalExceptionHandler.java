package org.enumgum.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.enumgum.domain.error.ErrorCode;
import org.enumgum.dto.ErrorEnvelope;
import org.enumgum.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorEnvelope> handleBusinessException(
      BusinessException ex, HttpServletRequest request) {

    ErrorEnvelope error =
        new ErrorEnvelope(
            ex.getCode().name(), ex.getMessage(), ex.getDetails(), getTraceId(request));

    HttpStatus status = getHttpStatusFromErrorCode(ex.getCode());
    return ResponseEntity.status(status).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorEnvelope> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    List<ErrorEnvelope.ErrorDetail> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::mapFieldErrorToErrorDetail)
            .collect(Collectors.toList());

    ErrorEnvelope error =
        new ErrorEnvelope(
            ErrorCode.VALIDATION_ERROR.name(), "Validation failed", details, getTraceId(request));

    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorEnvelope> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {

    ErrorEnvelope error =
        new ErrorEnvelope(
            ErrorCode.VALIDATION_ERROR.name(),
            "Invalid JSON format",
            List.of(),
            getTraceId(request));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorEnvelope> handleMediaTypeNotSupportedException(
      HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

    ErrorEnvelope error =
        new ErrorEnvelope(
            ErrorCode.VALIDATION_ERROR.name(),
            "Unsupported media type",
            List.of(),
            getTraceId(request));

    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorEnvelope> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    ErrorEnvelope.ErrorDetail detail =
        new ErrorEnvelope.ErrorDetail(
            ex.getName(), ex.getValue(), "Invalid value for " + ex.getName());

    ErrorEnvelope error =
        new ErrorEnvelope(
            ErrorCode.VALIDATION_ERROR.name(),
            "Invalid request parameter",
            List.of(detail),
            getTraceId(request));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorEnvelope> handleGenericException(
      Exception ex, HttpServletRequest request) {

    ErrorEnvelope error =
        new ErrorEnvelope(
            ErrorCode.INTERNAL_SERVER_ERROR.name(),
            "An unexpected error occurred",
            List.of(),
            getTraceId(request));

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  private HttpStatus getHttpStatusFromErrorCode(ErrorCode errorCode) {
    switch (errorCode) {
      case VALIDATION_ERROR:
        return HttpStatus.UNPROCESSABLE_ENTITY;
      case AUTHENTICATION_ERROR:
      case TOKEN_EXPIRED:
      case TOKEN_INVALID:
        return HttpStatus.UNAUTHORIZED;
      case INSUFFICIENT_PRIVILEGE:
      case ACCESS_DENIED:
        return HttpStatus.FORBIDDEN;
      case EMAIL_IN_USE:
      case RESOURCE_CONFLICT:
        return HttpStatus.CONFLICT;
      case RATE_LIMITED:
        return HttpStatus.TOO_MANY_REQUESTS;
      default:
        return HttpStatus.BAD_REQUEST;
    }
  }

  private ErrorEnvelope.ErrorDetail mapFieldErrorToErrorDetail(FieldError fieldError) {
    return new ErrorEnvelope.ErrorDetail(
        fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage());
  }

  private String getTraceId(HttpServletRequest request) {
    String traceId = request.getHeader("X-Trace-Id");
    return traceId != null ? traceId : UUID.randomUUID().toString();
  }
}
