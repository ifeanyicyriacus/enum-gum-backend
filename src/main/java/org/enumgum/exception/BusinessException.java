package org.enumgum.exception;

import java.util.List;
import org.enumgum.domain.error.ErrorCode;
import org.enumgum.dto.ErrorEnvelope;

public class BusinessException extends RuntimeException {
  private final ErrorCode code;
  private final List<ErrorEnvelope.ErrorDetail> details;

  public BusinessException(ErrorCode code, String message) {
    super(message);
    this.code = code;
    this.details = List.of();
  }

  public BusinessException(
      ErrorCode code, String message, List<ErrorEnvelope.ErrorDetail> details) {
    super(message);
    this.code = code;
    this.details = details;
  }

  public ErrorCode getCode() {
    return code;
  }

  public List<ErrorEnvelope.ErrorDetail> getDetails() {
    return details;
  }
}
