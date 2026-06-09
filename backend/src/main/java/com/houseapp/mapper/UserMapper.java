package com.houseapp.mapper;

import com.houseapp.dto.response.UserResponse;
import com.houseapp.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole(),
        user.isMustChangePassword(),
        user.getPreferredLanguage(),
        user.getLastLoginAt()
    );
  }
}
