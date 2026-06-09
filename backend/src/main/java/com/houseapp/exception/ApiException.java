package com.houseapp.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
  private final HttpStatus status;
  private final String errorCode;
  private final String safeMessage;

  public ApiException(HttpStatus status, String errorCode, String safeMessage) {
    super(safeMessage);
    this.status = status;
    this.errorCode = errorCode;
    this.safeMessage = safeMessage;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getSafeMessage() {
    return safeMessage;
  }
}
