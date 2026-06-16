package com.houseapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.database.path=./target/test-house-app.db",
    "app.upload.dir=./target/test-uploads",
    "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-123456",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class Stage7AAvatarIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void residentCanUploadPngAvatarAndProfileContainsSafeUrl() throws Exception {
    JsonNode profile = uploadResidentAvatar(pngFile("avatar.png"), 200);

    assertThat(profile.get("avatarUrl").asText()).startsWith("/api/files/avatars/");
    assertThat(profile.get("avatarUrl").asText()).doesNotContain("/home/");
    assertThat(profile.get("avatarPath").asText()).endsWith(".png");
  }

  @Test
  void residentCanUploadJpegAndWebpAvatar() throws Exception {
    JsonNode jpeg = uploadResidentAvatar(jpegFile("avatar.jpg"), 200);
    assertThat(jpeg.get("avatarPath").asText()).endsWith(".jpg");

    JsonNode webp = uploadResidentAvatar(webpFile("avatar.webp"), 200);
    assertThat(webp.get("avatarPath").asText()).endsWith(".webp");
  }

  @Test
  void residentCanDeleteOwnAvatarEvenIfFileIsMissing() throws Exception {
    JsonNode uploaded = uploadResidentAvatar(pngFile("avatar.png"), 200);
    Files.deleteIfExists(Path.of("./target/test-uploads/avatars").toAbsolutePath().resolve(uploaded.get("avatarPath").asText()));

    JsonNode deleted = deleteJson("/api/resident/profile/avatar", residentToken(), 200);

    assertThat(deleted.get("avatarPath").isNull()).isTrue();
    assertThat(deleted.get("avatarUrl").isNull()).isTrue();
  }

  @Test
  void residentUploadRejectsUnsupportedAndEmptyFiles() throws Exception {
    uploadResidentAvatar(new MockMultipartFile("file", "avatar.gif", "image/gif", new byte[] {1, 2, 3}), 400);
    uploadResidentAvatar(new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]), 400);
  }

  @Test
  void adminCanUploadAndDeleteResidentAvatar() throws Exception {
    long residentId = firstResidentId();
    JsonNode uploaded = uploadAdminAvatar(residentId, pngFile("admin-avatar.png"), 200);

    assertThat(uploaded.get("id").asLong()).isEqualTo(residentId);
    assertThat(uploaded.get("avatarUrl").asText()).startsWith("/api/files/avatars/");

    JsonNode deleted = deleteJson("/api/admin/residents/" + residentId + "/avatar", adminToken(), 200);
    assertThat(deleted.get("avatarPath").isNull()).isTrue();
  }

  @Test
  void residentAndUnauthenticatedCannotAccessAdminAvatarEndpoint() throws Exception {
    long residentId = firstResidentId();
    mockMvc.perform(multipart("/api/admin/residents/" + residentId + "/avatar")
            .file(pngFile("avatar.png"))
            .header("Authorization", "Bearer " + residentToken()))
        .andExpect(status().isForbidden());

    mockMvc.perform(multipart("/api/admin/residents/" + residentId + "/avatar")
            .file(pngFile("avatar.png")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void avatarFileServingIsAuthenticatedAndRejectsTraversal() throws Exception {
    JsonNode uploaded = uploadResidentAvatar(pngFile("avatar.png"), 200);
    String avatarUrl = uploaded.get("avatarUrl").asText();

    mockMvc.perform(get(avatarUrl).header("Authorization", "Bearer " + residentToken()))
        .andExpect(status().isOk());

    mockMvc.perform(get(avatarUrl))
        .andExpect(status().isUnauthorized());

    mockMvc.perform(get("/api/files/avatars/..%2Fapplication.properties")
            .header("Authorization", "Bearer " + adminToken()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void replacingAvatarDeletesOldFileAndCreatesAuditLog() throws Exception {
    JsonNode first = uploadResidentAvatar(pngFile("first.png"), 200);
    String oldFilename = first.get("avatarPath").asText();
    Path oldPath = Path.of("./target/test-uploads/avatars").toAbsolutePath().resolve(oldFilename);
    assertThat(Files.exists(oldPath)).isTrue();

    JsonNode second = uploadResidentAvatar(jpegFile("second.jpg"), 200);

    assertThat(second.get("avatarPath").asText()).isNotEqualTo(oldFilename);
    assertThat(Files.exists(oldPath)).isFalse();
    assertThat(hasAudit("AVATAR_UPLOADED", second.get("id").asLong())).isTrue();
  }

  private JsonNode uploadResidentAvatar(MockMultipartFile file, int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(multipart("/api/resident/profile/avatar")
            .file(file)
            .header("Authorization", "Bearer " + residentToken()))
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return readBody(result);
  }

  private JsonNode uploadAdminAvatar(long id, MockMultipartFile file, int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(multipart("/api/admin/residents/" + id + "/avatar")
            .file(file)
            .header("Authorization", "Bearer " + adminToken()))
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return readBody(result);
  }

  private boolean hasAudit(String action, long entityId) throws Exception {
    JsonNode logs = getJson("/api/admin/audit-logs?action=" + action + "&entityId=" + entityId, adminToken(), 200);
    return logs.size() > 0;
  }

  private long firstResidentId() throws Exception {
    return getJson("/api/admin/residents", adminToken(), 200).get(0).get("id").asLong();
  }

  private MockMultipartFile pngFile(String filename) {
    return new MockMultipartFile("file", filename, "image/png", new byte[] {
        (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00
    });
  }

  private MockMultipartFile jpegFile(String filename) {
    return new MockMultipartFile("file", filename, "image/jpeg", new byte[] {
        (byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00
    });
  }

  private MockMultipartFile webpFile(String filename) {
    return new MockMultipartFile("file", filename, "image/webp", new byte[] {
        0x52, 0x49, 0x46, 0x46, 0x24, 0x00, 0x00, 0x00, 0x57, 0x45, 0x42, 0x50
    });
  }

  private String adminToken() throws Exception {
    return login("admin@house.com", "Admin123!");
  }

  private String residentToken() throws Exception {
    return login("resident@house.com", "Resident123!");
  }

  private String login(String email, String password) throws Exception {
    return postJson("/api/auth/login", """
        {"email":"%s","password":"%s","rememberMe":false}
        """.formatted(email, password), null, 200).get("accessToken").asText();
  }

  private JsonNode getJson(String path, String accessToken, int expectedStatus) throws Exception {
    var builder = get(path);
    if (accessToken != null) {
      builder.header("Authorization", "Bearer " + accessToken);
    }
    MvcResult result = mockMvc.perform(builder)
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return readBody(result);
  }

  private JsonNode postJson(String path, String json, String accessToken, int expectedStatus) throws Exception {
    var builder = post(path).contentType(MediaType.APPLICATION_JSON).content(json);
    if (accessToken != null) {
      builder.header("Authorization", "Bearer " + accessToken);
    }
    MvcResult result = mockMvc.perform(builder)
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return readBody(result);
  }

  private JsonNode deleteJson(String path, String accessToken, int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(delete(path)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return readBody(result);
  }

  private JsonNode readBody(MvcResult result) throws Exception {
    String body = result.getResponse().getContentAsString();
    if (body == null || body.isBlank()) {
      return objectMapper.createObjectNode();
    }
    return objectMapper.readTree(body);
  }
}
