package org.enumgum.dto;

import java.util.List;

// import java.util.UUID;

public record ErrorEnvelope(
    String code, String message, List<ErrorDetail> details, String traceId) {
  //    public ErrorEnvelope {
  //        if (details == null) {
  //            details = List.of();
  //        }
  //        if (traceId == null) {
  //            traceId = UUID.randomUUID().toString();
  //        }
  //    }

  public record ErrorDetail(String field, Object rejected, String message) {}
}
