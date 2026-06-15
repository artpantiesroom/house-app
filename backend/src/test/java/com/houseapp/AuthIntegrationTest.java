package com.houseapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseapp.entity.Role;
import com.houseapp.entity.User;
import com.houseapp.repository.RefreshTokenRepository;
import com.houseapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.database.path=./target/test-house-app.db",
    "app.upload.dir=./target/test-uploads",
    "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-123456",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class AuthIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void loginSucceeds() throws Exception {
    JsonNode response = login("admin@house.com", "Admin123!", false);

    assertThat(response.get("accessToken").asText()).isNotBlank();
    assertThat(response.get("refreshToken").asText()).isNotBlank();
    assertThat(response.get("accessTokenExpiresIn").asLong()).isEqualTo(900);
    assertThat(response.at("/user/role").asText()).isEqualTo("ADMIN");
  }

  @Test
  void loginWithWrongPasswordFails() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"admin@house.com","password":"Wrong123!","rememberMe":false}
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshRotatesTokenAndRejectsOldTokenReuse() throws Exception {
    JsonNode login = login("admin@house.com", "Admin123!", false);
    String oldRefreshToken = login.get("refreshToken").asText();

    JsonNode refreshed = postJson("/api/auth/refresh", """
        {"refreshToken":"%s"}
        """.formatted(oldRefreshToken), 200);

    assertThat(refreshed.get("accessToken").asText()).isNotBlank();
    assertThat(refreshed.get("refreshToken").asText()).isNotEqualTo(oldRefreshToken);

    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"refreshToken":"%s"}
                """.formatted(oldRefreshToken)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logoutRevokesTokenAndIsIdempotent() throws Exception {
    JsonNode login = login("admin@house.com", "Admin123!", false);
    String refreshToken = login.get("refreshToken").asText();

    logout(refreshToken);
    logout(refreshToken);

    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"refreshToken":"%s"}
                """.formatted(refreshToken)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void accessWithoutJwtIsRejected() throws Exception {
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void residentCannotAccessAdminEndpoint() throws Exception {
    JsonNode login = login("resident@house.com", "Resident123!", false);
    String accessToken = login.get("accessToken").asText();

    mockMvc.perform(get("/api/admin/auth-check").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isForbidden());
  }

  @Test
  @Transactional
  void userWithRequiredPasswordChangeIsBlockedUntilPasswordChanges() throws Exception {
    User user = new User();
    user.setName("Temporary Resident");
    user.setEmail("temporary.resident@example.com");
    user.setPasswordHash(passwordEncoder.encode("Temporary123!"));
    user.setRole(Role.RESIDENT);
    user.setPreferredLanguage("uk");
    user.setMustChangePassword(true);
    user.setEnabled(true);
    userRepository.saveAndFlush(user);

    JsonNode login = login("temporary.resident@example.com", "Temporary123!", false);
    String accessToken = login.get("accessToken").asText();

    mockMvc.perform(get("/api/resident/auth-check").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isForbidden());

    JsonNode changed = postJsonWithAccessToken("/api/auth/change-password", """
        {"currentPassword":"Temporary123!","newPassword":"Changed123!"}
        """, accessToken, 200);

    assertThat(changed.at("/user/mustChangePassword").asBoolean()).isFalse();
  }

  @Test
  void residentWithRequiredPasswordChangeCanReplaceTemporaryPasswordAndKeepSession() throws Exception {
    String email = "change.required.%d@example.com".formatted(System.nanoTime());
    createTemporaryResident(email, "Temporary123!");

    JsonNode login = login(email, "Temporary123!", false);
    assertThat(login.at("/user/mustChangePassword").asBoolean()).isTrue();
    String accessToken = login.get("accessToken").asText();
    String oldRefreshToken = login.get("refreshToken").asText();

    JsonNode changed = postJsonWithAccessToken("/api/auth/change-password", """
        {"currentPassword":"Temporary123!","newPassword":"Changed123!"}
        """, accessToken, 200);

    assertThat(changed.get("accessToken").asText()).isNotBlank();
    assertThat(changed.get("refreshToken").asText()).isNotBlank();
    assertThat(changed.get("refreshToken").asText()).isNotEqualTo(oldRefreshToken);
    assertThat(changed.at("/user/mustChangePassword").asBoolean()).isFalse();

    User saved = userRepository.findByEmail(email).orElseThrow();
    assertThat(saved.isMustChangePassword()).isFalse();
    assertThat(passwordEncoder.matches("Changed123!", saved.getPasswordHash())).isTrue();
    assertThat(passwordEncoder.matches("Temporary123!", saved.getPasswordHash())).isFalse();

    JsonNode refreshed = postJson("/api/auth/refresh", """
        {"refreshToken":"%s"}
        """.formatted(changed.get("refreshToken").asText()), 200);
    assertThat(refreshed.at("/user/mustChangePassword").asBoolean()).isFalse();

    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"refreshToken":"%s"}
                """.formatted(oldRefreshToken)))
        .andExpect(status().isUnauthorized());

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"%s","password":"Temporary123!","rememberMe":false}
                """.formatted(email)))
        .andExpect(status().isUnauthorized());

    JsonNode newPasswordLogin = login(email, "Changed123!", false);
    assertThat(newPasswordLogin.at("/user/mustChangePassword").asBoolean()).isFalse();
  }

  @Test
  void passwordChangeWithWrongCurrentPasswordReturnsValidationError() throws Exception {
    String email = "wrong.current.%d@example.com".formatted(System.nanoTime());
    createTemporaryResident(email, "Temporary123!");
    JsonNode login = login(email, "Temporary123!", false);

    JsonNode error = postJsonWithAccessToken("/api/auth/change-password", """
        {"currentPassword":"Wrong123!","newPassword":"Changed123!"}
        """, login.get("accessToken").asText(), 400);

    assertThat(error.get("error").asText()).isEqualTo("VALIDATION_ERROR");
    assertThat(error.get("message").asText()).isEqualTo("Current password is incorrect");
    assertThat(userRepository.findByEmail(email).orElseThrow().isMustChangePassword()).isTrue();
  }

  @Test
  void passwordValidationRejectsWeakPassword() throws Exception {
    JsonNode login = login("admin@house.com", "Admin123!", false);
    String accessToken = login.get("accessToken").asText();

    mockMvc.perform(post("/api/auth/change-password")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"currentPassword":"Admin123!","newPassword":"weak"}
                """))
        .andExpect(status().isBadRequest());
  }

  private JsonNode login(String email, String password, boolean rememberMe) throws Exception {
    return postJson("/api/auth/login", """
        {"email":"%s","password":"%s","rememberMe":%s}
        """.formatted(email, password, rememberMe), 200);
  }

  private JsonNode postJson(String path, String json, int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(post(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private JsonNode postJsonWithAccessToken(String path, String json, String accessToken, int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(post(path)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private void logout(String refreshToken) throws Exception {
    mockMvc.perform(post("/api/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"refreshToken":"%s"}
                """.formatted(refreshToken)))
        .andExpect(status().isNoContent());
  }

  private void createTemporaryResident(String email, String password) {
    User user = new User();
    user.setName("Temporary Resident");
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setRole(Role.RESIDENT);
    user.setPreferredLanguage("uk");
    user.setMustChangePassword(true);
    user.setEnabled(true);
    userRepository.saveAndFlush(user);
  }
}
