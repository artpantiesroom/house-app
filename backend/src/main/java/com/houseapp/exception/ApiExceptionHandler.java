package com.houseapp.exception;

import com.houseapp.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
    Map<String, String> fieldErrors = new LinkedHashMap<>();
    exception.getBindingResult().getFieldErrors().forEach(error ->
        fieldErrors.put(error.getField(), error.getDefaultMessage()));
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", request, fieldErrors);
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> api(ApiException exception, HttpServletRequest request) {
    return build(exception.getStatus(), exception.getErrorCode(), exception.getSafeMessage(), request, Map.of());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> typeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
    String name = exception.getName();
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid value for " + name, request, Map.of(name, "Invalid value"));
  }

  @ExceptionHandler({BadCredentialsException.class})
  public ResponseEntity<ErrorResponse> badCredentials(RuntimeException exception, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid credentials", request, Map.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> accessDenied(AccessDeniedException exception, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied", request, Map.of());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> unexpected(Exception exception, HttpServletRequest request) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error", request, Map.of());
  }

  private ResponseEntity<ErrorResponse> build(
      HttpStatus status,
      String error,
      String message,
      HttpServletRequest request,
      Map<String, String> fieldErrors
  ) {
    return ResponseEntity.status(status).body(new ErrorResponse(
        Instant.now(),
        status.value(),
        error,
        message,
        request.getRequestURI(),
        fieldErrors
    ));
  }
}
