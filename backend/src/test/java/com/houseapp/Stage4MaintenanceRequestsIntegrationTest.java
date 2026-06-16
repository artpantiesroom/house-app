package com.houseapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class Stage4MaintenanceRequestsIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void residentCanCreateMaintenanceRequest() throws Exception {
    JsonNode created = createResidentRequest(residentToken(), "PLUMBING");

    assertThat(created.get("title").asText()).startsWith("Stage4");
    assertThat(created.get("category").asText()).isEqualTo("PLUMBING");
    assertThat(created.get("status").asText()).isEqualTo("NEW");
    assertThat(created.get("priority").asText()).isEqualTo("NORMAL");
  }

  @Test
  void createdRequestUsesAuthenticatedResidentProfileAndApartment() throws Exception {
    JsonNode created = createResidentRequest(residentToken(), "ELEVATOR");
    JsonNode adminView = getJson("/api/admin/maintenance-requests/" + created.get("id").asLong(), adminToken(), 200);

    assertThat(adminView.get("residentEmail").asText()).isEqualTo("resident@house.com");
    assertThat(adminView.get("apartmentNumber").asText()).isEqualTo("A-101");
  }

  @Test
  void residentCanListOwnRequests() throws Exception {
    JsonNode created = createResidentRequest(residentToken(), "INTERNET");
    JsonNode list = getJson("/api/resident/maintenance-requests", residentToken(), 200);

    assertThat(containsId(list, created.get("id").asLong())).isTrue();
  }

  @Test
  void residentCannotReadAnotherResidentRequest() throws Exception {
    JsonNode adminList = getJson("/api/admin/maintenance-requests", adminToken(), 200);
    long otherRequestId = 0;
    for (JsonNode request : adminList) {
      if (!"resident@house.com".equals(request.get("residentEmail").asText())) {
        otherRequestId = request.get("id").asLong();
        break;
      }
    }
    assertThat(otherRequestId).isGreaterThan(0);

    getJson("/api/resident/maintenance-requests/" + otherRequestId, residentToken(), 404);
  }

  @Test
  void residentCannotSetStatusPriorityOrInternalNotesDuringCreate() throws Exception {
    JsonNode created = postJson("/api/resident/maintenance-requests", """
        {
          "title":"Stage4 guarded fields %s",
          "description":"Resident request should ignore admin fields.",
          "category":"PLUMBING",
          "status":"RESOLVED",
          "priority":"URGENT",
          "internalNotes":"hidden"
        }
        """.formatted(UUID.randomUUID()), residentToken(), 201);
    JsonNode adminView = getJson("/api/admin/maintenance-requests/" + created.get("id").asLong(), adminToken(), 200);

    assertThat(created.get("status").asText()).isEqualTo("NEW");
    assertThat(created.get("priority").asText()).isEqualTo("NORMAL");
    assertThat(adminView.get("internalNotes").isNull()).isTrue();
  }

  @Test
  void adminCanListAllRequestsAndFilter() throws Exception {
    JsonNode created = createResidentRequest(residentToken(), "PLUMBING");
    patchJson("/api/admin/maintenance-requests/" + created.get("id").asLong(), """
        {"status":"IN_PROGRESS","priority":"URGENT"}
        """, adminToken(), 200);

    JsonNode all = getJson("/api/admin/maintenance-requests", adminToken(), 200);
    JsonNode filtered = getJson("/api/admin/maintenance-requests?status=IN_PROGRESS&category=PLUMBING&priority=URGENT", adminToken(), 200);

    assertThat(all.size()).isGreaterThanOrEqualTo(8);
    assertThat(containsId(filtered, created.get("id").asLong())).isTrue();
  }

  @Test
  void adminCanUpdateStatusPriorityAdminResponseAndInternalNotes() throws Exception {
    JsonNode created = createResidentRequest(residentToken(), "HEATING");

    JsonNode updated = patchJson("/api/admin/maintenance-requests/" + created.get("id").asLong(), """
        {
          "status":"WAITING_RESIDENT",
          "priority":"HIGH",
          "adminResponse":"Please confirm access time.",
          "internalNotes":"Call before arrival."
        }
        """, adminToken(), 200);

    assertThat(updated.get("status").asText()).isEqualTo("WAITING_RESIDENT");
    assertThat(updated.get("priority").asText()).isEqualTo("HIGH");
    assertThat(updated.get("adminResponse").asText()).isEqualTo("Please confirm access time.");
    assertThat(updated.get("internalNotes").asText()).isEqualTo("Call before arrival.");
  }

  @Test
  void residentResponseDoesNotExposeInternalNotes() throws Exception {
    JsonNode created = createResidentRequest(residentToken(), "SECURITY");
    patchJson("/api/admin/maintenance-requests/" + created.get("id").asLong(), """
        {"adminResponse":"Visible response","internalNotes":"Admin only"}
        """, adminToken(), 200);

    JsonNode residentView = getJson("/api/resident/maintenance-requests/" + created.get("id").asLong(), residentToken(), 200);

    assertThat(residentView.has("internalNotes")).isFalse();
    assertThat(residentView.get("adminResponse").asText()).isEqualTo("Visible response");
  }

  @Test
  void statusResolvedSetsResolvedAt() throws Exception {
    JsonNode created = createResidentRequest(residentToken(), "CLEANING");

    JsonNode updated = patchJson("/api/admin/maintenance-requests/" + created.get("id").asLong(), """
        {"status":"RESOLVED"}
        """, adminToken(), 200);

    assertThat(updated.get("resolvedAt").isNull()).isFalse();
  }

  @Test
  void unauthenticatedRequestIsRejected() throws Exception {
    mockMvc.perform(get("/api/resident/maintenance-requests"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void residentCannotAccessAdminMaintenanceEndpoints() throws Exception {
    mockMvc.perform(get("/api/admin/maintenance-requests")
            .header("Authorization", "Bearer " + residentToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCannotAccessResidentMaintenanceEndpoints() throws Exception {
    mockMvc.perform(get("/api/resident/maintenance-requests")
            .header("Authorization", "Bearer " + adminToken()))
        .andExpect(status().isForbidden());
  }

  private JsonNode createResidentRequest(String token, String category) throws Exception {
    return postJson("/api/resident/maintenance-requests", """
        {
          "title":"Stage4 %s",
          "description":"Stage 4 integration test request.",
          "category":"%s"
        }
        """.formatted(UUID.randomUUID(), category), token, 201);
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

  private JsonNode patchJson(String path, String json, String accessToken, int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(patch(path)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return readBody(result);
  }

  private boolean containsId(JsonNode array, long id) {
    for (JsonNode item : array) {
      if (item.get("id").asLong() == id) {
        return true;
      }
    }
    return false;
  }

  private JsonNode readBody(MvcResult result) throws Exception {
    String body = result.getResponse().getContentAsString();
    if (body == null || body.isBlank()) {
      return objectMapper.createObjectNode();
    }
    return objectMapper.readTree(body);
  }
}
