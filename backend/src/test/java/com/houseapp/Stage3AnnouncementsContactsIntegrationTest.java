package com.houseapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class Stage3AnnouncementsContactsIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void adminCanCreateDraftAnnouncement() throws Exception {
    JsonNode created = createAnnouncement("DRAFT");

    assertThat(created.get("status").asText()).isEqualTo("DRAFT");
    assertThat(created.get("publishedAt").isNull()).isTrue();
  }

  @Test
  void adminCanPublishAnnouncement() throws Exception {
    JsonNode created = createAnnouncement("DRAFT");

    JsonNode published = patchJson("/api/admin/announcements/" + created.get("id").asLong() + "/publish", adminToken(), 200);

    assertThat(published.get("status").asText()).isEqualTo("PUBLISHED");
    assertThat(published.get("publishedAt").isNull()).isFalse();
  }

  @Test
  void adminCanArchiveAnnouncement() throws Exception {
    JsonNode created = createAnnouncement("PUBLISHED");

    JsonNode archived = patchJson("/api/admin/announcements/" + created.get("id").asLong() + "/archive", adminToken(), 200);

    assertThat(archived.get("status").asText()).isEqualTo("ARCHIVED");
  }

  @Test
  void adminCanListAllAnnouncementStatuses() throws Exception {
    createAnnouncement("DRAFT");
    createAnnouncement("PUBLISHED");
    createAnnouncement("ARCHIVED");

    JsonNode announcements = getJson("/api/admin/announcements", adminToken(), 200);

    assertThat(containsStatus(announcements, "DRAFT")).isTrue();
    assertThat(containsStatus(announcements, "PUBLISHED")).isTrue();
    assertThat(containsStatus(announcements, "ARCHIVED")).isTrue();
  }

  @Test
  void residentCanListPublishedAnnouncementsOnly() throws Exception {
    createAnnouncement("DRAFT");
    createAnnouncement("ARCHIVED");
    JsonNode published = createAnnouncement("PUBLISHED");

    JsonNode announcements = getJson("/api/resident/announcements", residentToken(), 200);

    assertThat(containsId(announcements, published.get("id").asLong())).isTrue();
    for (JsonNode announcement : announcements) {
      assertThat(announcement.get("status").asText()).isEqualTo("PUBLISHED");
    }
  }

  @Test
  void residentCannotOpenDraftOrArchivedAnnouncement() throws Exception {
    JsonNode draft = createAnnouncement("DRAFT");
    JsonNode archived = createAnnouncement("ARCHIVED");

    getJson("/api/resident/announcements/" + draft.get("id").asLong(), residentToken(), 404);
    getJson("/api/resident/announcements/" + archived.get("id").asLong(), residentToken(), 404);
  }

  @Test
  void residentCannotAccessAdminAnnouncementEndpoints() throws Exception {
    mockMvc.perform(get("/api/admin/announcements")
            .header("Authorization", "Bearer " + residentToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void unauthenticatedAnnouncementRequestIsRejected() throws Exception {
    mockMvc.perform(get("/api/resident/announcements"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void adminCanCreateContact() throws Exception {
    JsonNode created = createContact(true);

    assertThat(created.get("nameUk").asText()).startsWith("Stage Contact ");
    assertThat(created.get("active").asBoolean()).isTrue();
  }

  @Test
  void adminCanUpdateContact() throws Exception {
    JsonNode created = createContact(true);

    JsonNode updated = putJson("/api/admin/contacts/" + created.get("id").asLong(), contactJson(true, "updated@example.com"), adminToken(), 200);

    assertThat(updated.get("email").asText()).isEqualTo("updated@example.com");
  }

  @Test
  void adminCanSoftDeleteContact() throws Exception {
    JsonNode created = createContact(true);

    deleteJson("/api/admin/contacts/" + created.get("id").asLong(), adminToken(), 204);
    JsonNode updated = getJson("/api/admin/contacts/" + created.get("id").asLong(), adminToken(), 200);

    assertThat(updated.get("active").asBoolean()).isFalse();
  }

  @Test
  void residentCanListActiveContactsOnly() throws Exception {
    JsonNode active = createContact(true);
    JsonNode inactive = createContact(false);

    JsonNode contacts = getJson("/api/resident/contacts", residentToken(), 200);

    assertThat(containsId(contacts, active.get("id").asLong())).isTrue();
    assertThat(containsId(contacts, inactive.get("id").asLong())).isFalse();
  }

  @Test
  void residentCannotAccessAdminContactEndpoints() throws Exception {
    mockMvc.perform(get("/api/admin/contacts")
            .header("Authorization", "Bearer " + residentToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void contactRequiresPhoneOrEmail() throws Exception {
    postJson("/api/admin/contacts", """
        {
          "nameUk":"No Channel",
          "roleUk":"Support",
          "sortOrder":1,
          "active":true
        }
        """, adminToken(), 400);
  }

  private JsonNode createAnnouncement(String status) throws Exception {
    return postJson("/api/admin/announcements", announcementJson(status), adminToken(), 201);
  }

  private String announcementJson(String status) {
    String title = "Stage 3 " + status + " " + UUID.randomUUID();
    String expiresAt = Instant.now().plus(7, ChronoUnit.DAYS).toString();
    return """
        {
          "titleUk":"%s",
          "titleEn":"%s",
          "bodyUk":"Тестове оголошення Stage 3",
          "bodyEn":"Stage 3 test announcement",
          "category":"GENERAL",
          "priority":"HIGH",
          "status":"%s",
          "expiresAt":"%s"
        }
        """.formatted(title, title, status, expiresAt);
  }

  private JsonNode createContact(boolean active) throws Exception {
    return postJson("/api/admin/contacts", contactJson(active, "stage3." + UUID.randomUUID() + "@example.com"), adminToken(), 201);
  }

  private String contactJson(boolean active, String email) {
    return """
        {
          "nameUk":"Stage Contact %s",
          "nameEn":"Contact",
          "roleUk":"Support",
          "roleEn":"Support",
          "departmentUk":"Building",
          "departmentEn":"Building",
          "phone":"+380501112299",
          "email":"%s",
          "availabilityUk":"Mon-Fri",
          "availabilityEn":"Mon-Fri",
          "sortOrder":5,
          "active":%s
        }
        """.formatted(UUID.randomUUID(), email, active);
  }

  private boolean containsStatus(JsonNode array, String status) {
    for (JsonNode item : array) {
      if (status.equals(item.get("status").asText())) {
        return true;
      }
    }
    return false;
  }

  private boolean containsId(JsonNode array, long id) {
    for (JsonNode item : array) {
      if (item.get("id").asLong() == id) {
        return true;
      }
    }
    return false;
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

  private JsonNode putJson(String path, String json, String accessToken, int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(put(path)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return readBody(result);
  }

  private JsonNode patchJson(String path, String accessToken, int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(patch(path)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return readBody(result);
  }

  private void deleteJson(String path, String accessToken, int expectedStatus) throws Exception {
    mockMvc.perform(delete(path)
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().is(expectedStatus));
  }

  private JsonNode readBody(MvcResult result) throws Exception {
    String body = result.getResponse().getContentAsString();
    if (body == null || body.isBlank()) {
      return objectMapper.createObjectNode();
    }
    return objectMapper.readTree(body);
  }
}
