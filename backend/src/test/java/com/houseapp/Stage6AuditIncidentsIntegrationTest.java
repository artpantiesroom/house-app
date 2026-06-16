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
import java.time.LocalDate;
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
class Stage6AuditIncidentsIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void adminCanListAndReadAuditLogs() throws Exception {
    JsonNode logs = getJson("/api/admin/audit-logs", adminToken(), 200);

    assertThat(logs.size()).isGreaterThanOrEqualTo(15);
    long id = logs.get(0).get("id").asLong();
    JsonNode log = getJson("/api/admin/audit-logs/" + id, adminToken(), 200);

    assertThat(log.get("id").asLong()).isEqualTo(id);
    assertThat(log.has("summary")).isTrue();
  }

  @Test
  void residentAndUnauthenticatedCannotAccessAuditLogs() throws Exception {
    mockMvc.perform(get("/api/admin/audit-logs")
            .header("Authorization", "Bearer " + residentToken()))
        .andExpect(status().isForbidden());

    mockMvc.perform(get("/api/admin/audit-logs"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void auditLogsDoNotExposePublicMutationEndpoints() throws Exception {
    mockMvc.perform(post("/api/admin/audit-logs")
            .header("Authorization", "Bearer " + adminToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isMethodNotAllowed());

    mockMvc.perform(put("/api/admin/audit-logs/1")
            .header("Authorization", "Bearer " + adminToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isMethodNotAllowed());

    mockMvc.perform(delete("/api/admin/audit-logs/1")
            .header("Authorization", "Bearer " + adminToken()))
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  void creatingPaymentMaintenanceAndIncidentCreatesAuditLog() throws Exception {
    JsonNode payment = postJson("/api/admin/payments", paymentJson(), adminToken(), 201);
    JsonNode request = postJson("/api/resident/maintenance-requests", maintenanceJson(), residentToken(), 201);
    JsonNode incident = postJson("/api/admin/security-incidents", incidentJson("OPEN"), adminToken(), 201);

    assertThat(hasAudit("PAYMENT_CREATED", payment.get("id").asLong())).isTrue();
    assertThat(hasAudit("MAINTENANCE_CREATED", request.get("id").asLong())).isTrue();
    assertThat(hasAudit("SECURITY_INCIDENT_CREATED", incident.get("id").asLong())).isTrue();
  }

  @Test
  void failedLoginCreatesAuditLog() throws Exception {
    String email = "missing-%s@example.com".formatted(UUID.randomUUID());
    postJson("/api/auth/login", """
        {"email":"%s","password":"Wrong123!","rememberMe":false}
        """.formatted(email), null, 401);

    JsonNode logs = getJson("/api/admin/audit-logs?action=LOGIN_FAILED&search=" + email, adminToken(), 200);

    assertThat(logs.size()).isGreaterThanOrEqualTo(1);
  }

  @Test
  void adminCanListCreateUpdateResolveAndSoftDeleteIncidents() throws Exception {
    JsonNode created = postJson("/api/admin/security-incidents", incidentJson("OPEN"), adminToken(), 201);

    JsonNode list = getJson("/api/admin/security-incidents?severity=HIGH&search=Stage6", adminToken(), 200);
    assertThat(list.size()).isGreaterThanOrEqualTo(1);

    JsonNode updated = putJson("/api/admin/security-incidents/" + created.get("id").asLong(), """
        {
          "title":"Stage6 edited incident",
          "description":"Updated Stage 6 incident description.",
          "severity":"CRITICAL",
          "status":"INVESTIGATING",
          "category":"SYSTEM",
          "resolutionNotes":null
        }
        """, adminToken(), 200);
    assertThat(updated.get("severity").asText()).isEqualTo("CRITICAL");

    JsonNode resolved = patchJson("/api/admin/security-incidents/" + created.get("id").asLong() + "/status", """
        {"status":"RESOLVED","resolutionNotes":"Resolved during integration test."}
        """, adminToken(), 200);
    assertThat(resolved.get("status").asText()).isEqualTo("RESOLVED");
    assertThat(resolved.get("resolvedAt").isNull()).isFalse();

    deleteJson("/api/admin/security-incidents/" + created.get("id").asLong(), adminToken(), 204);
    JsonNode falsePositive = getJson("/api/admin/security-incidents/" + created.get("id").asLong(), adminToken(), 200);
    assertThat(falsePositive.get("status").asText()).isEqualTo("FALSE_POSITIVE");
    assertThat(falsePositive.get("resolvedAt").isNull()).isFalse();
  }

  @Test
  void residentAndUnauthenticatedCannotAccessIncidents() throws Exception {
    mockMvc.perform(get("/api/admin/security-incidents")
            .header("Authorization", "Bearer " + residentToken()))
        .andExpect(status().isForbidden());

    mockMvc.perform(get("/api/admin/security-incidents"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void invalidIncidentEnumIsRejectedCleanly() throws Exception {
    postJson("/api/admin/security-incidents", """
        {
          "title":"Invalid enum",
          "description":"Invalid enum test",
          "severity":"SEVERE",
          "status":"OPEN",
          "category":"OTHER"
        }
        """, adminToken(), 400);
  }

  private boolean hasAudit(String action, long entityId) throws Exception {
    JsonNode logs = getJson("/api/admin/audit-logs?action=" + action + "&entityId=" + entityId, adminToken(), 200);
    return logs.size() > 0;
  }

  private String paymentJson() {
    return """
        {
          "residentProfileId":1,
          "type":"UTILITIES",
          "status":"PENDING",
          "amountMinor":123456,
          "currency":"UAH",
          "periodYear":2026,
          "periodMonth":9,
          "titleUk":"Stage6 audit payment %s",
          "titleEn":"Stage6 audit payment",
          "descriptionUk":"Stage 6 audit payment test",
          "descriptionEn":"Stage 6 audit payment test",
          "dueDate":"%s"
        }
        """.formatted(UUID.randomUUID(), LocalDate.of(2026, 9, 20));
  }

  private String maintenanceJson() {
    return """
        {
          "title":"Stage6 maintenance %s",
          "description":"Maintenance request for Stage 6 audit verification.",
          "category":"OTHER"
        }
        """.formatted(UUID.randomUUID());
  }

  private String incidentJson(String status) {
    return """
        {
          "title":"Stage6 incident %s",
          "description":"Security incident created by Stage 6 integration test.",
          "severity":"HIGH",
          "status":"%s",
          "category":"AUTHENTICATION",
          "resolutionNotes":null
        }
        """.formatted(UUID.randomUUID(), status);
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

  private JsonNode patchJson(String path, String json, String accessToken, int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(patch(path)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
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
