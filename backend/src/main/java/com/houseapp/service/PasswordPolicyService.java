package com.houseapp.service;

import com.houseapp.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {
  public void validate(String password) {
    if (password == null
        || password.length() < 8
        || !password.matches(".*[A-Z].*")
        || !password.matches(".*[a-z].*")
        || !password.matches(".*\\d.*")
        || !password.matches(".*[^A-Za-z0-9].*")) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "VALIDATION_ERROR",
          "Password must contain at least 8 characters, uppercase, lowercase, digit, and special character"
      );
    }
  }
}
