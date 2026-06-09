package com.houseapp.service;

import com.houseapp.entity.Role;
import com.houseapp.entity.User;
import com.houseapp.repository.UserRepository;
import java.util.Locale;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PrototypeDataSeeder implements ApplicationRunner {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public PrototypeDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedUser("Administrator", "admin@house.com", "Admin123!", Role.ADMIN);
    seedUser("Demo Resident", "resident@house.com", "Resident123!", Role.RESIDENT);
  }

  private void seedUser(String name, String email, String rawPassword, Role role) {
    String normalizedEmail = email.toLowerCase(Locale.ROOT);
    if (userRepository.existsByEmail(normalizedEmail)) {
      return;
    }
    User user = new User();
    user.setName(name);
    user.setEmail(normalizedEmail);
    user.setPasswordHash(passwordEncoder.encode(rawPassword));
    user.setRole(role);
    user.setPreferredLanguage("uk");
    user.setMustChangePassword(false);
    user.setEnabled(true);
    userRepository.save(user);
  }
}
