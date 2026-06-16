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
class Stage5PaymentsIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void residentCanListOwnPayments() throws Exception {
    JsonNode payments = getJson("/api/resident/payments", residentToken(), 200);

    assertThat(payments.size()).isGreaterThanOrEqualTo(3);
    for (JsonNode payment : payments) {
      assertThat(payment.has("residentEmail")).isFalse();
      assertThat(payment.get("amountMinor").asLong()).isGreaterThan(0);
    }
  }

  @Test
  void residentCanReadOwnPayment() throws Exception {
    JsonNode payments = getJson("/api/resident/payments", residentToken(), 200);
    long id = payments.get(0).get("id").asLong();

    JsonNode payment = getJson("/api/resident/payments/" + id, residentToken(), 200);

    assertThat(payment.get("id").asLong()).isEqualTo(id);
  }

  @Test
  void residentCannotReadAnotherResidentPayment() throws Exception {
    JsonNode adminPayments = getJson("/api/admin/payments", adminToken(), 200);
    long otherPaymentId = 0;
    for (JsonNode payment : adminPayments) {
      if (!"resident@house.com".equals(payment.get("residentEmail").asText())) {
        otherPaymentId = payment.get("id").asLong();
        break;
      }
    }
    assertThat(otherPaymentId).isGreaterThan(0);

    getJson("/api/resident/payments/" + otherPaymentId, residentToken(), 404);
  }

  @Test
  void residentCannotAccessAdminPaymentsEndpoints() throws Exception {
    mockMvc.perform(get("/api/admin/payments")
            .header("Authorization", "Bearer " + residentToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCanListAllPaymentsAndFilter() throws Exception {
    JsonNode created = createPayment("PENDING", "UTILITIES");

    JsonNode all = getJson("/api/admin/payments", adminToken(), 200);
    JsonNode filtered = getJson("/api/admin/payments?status=PENDING&type=UTILITIES&periodYear=2026&periodMonth=8", adminToken(), 200);

    assertThat(all.size()).isGreaterThanOrEqualTo(12);
    assertThat(containsId(filtered, created.get("id").asLong())).isTrue();
  }

  @Test
  void adminCanCreatePaymentForResidentAndApartmentIsDerived() throws Exception {
    JsonNode created = createPayment("PENDING", "RENT");

    assertThat(created.get("residentEmail").asText()).isEqualTo("resident@house.com");
    assertThat(created.get("apartmentNumber").asText()).isEqualTo("A-101");
    assertThat(created.get("amountMinor").asLong()).isEqualTo(123456L);
  }

  @Test
  void adminCanUpdatePayment() throws Exception {
    JsonNode created = createPayment("PENDING", "OTHER");

    JsonNode updated = putJson("/api/admin/payments/" + created.get("id").asLong(), paymentJson("OVERDUE", "MAINTENANCE", 222222L), adminToken(), 200);

    assertThat(updated.get("status").asText()).isEqualTo("OVERDUE");
    assertThat(updated.get("type").asText()).isEqualTo("MAINTENANCE");
    assertThat(updated.get("amountMinor").asLong()).isEqualTo(222222L);
  }

  @Test
  void adminCanSetStatusPaidAndPaidAtIsSet() throws Exception {
    JsonNode created = createPayment("PENDING", "SECURITY");

    JsonNode paid = patchJson("/api/admin/payments/" + created.get("id").asLong() + "/status", """
        {"status":"PAID"}
        """, adminToken(), 200);

    assertThat(paid.get("status").asText()).isEqualTo("PAID");
    assertThat(paid.get("paidAt").isNull()).isFalse();
  }

  @Test
  void adminCanCancelPayment() throws Exception {
    JsonNode created = createPayment("PENDING", "PARKING");

    deleteJson("/api/admin/payments/" + created.get("id").asLong(), adminToken(), 204);
    JsonNode cancelled = getJson("/api/admin/payments/" + created.get("id").asLong(), adminToken(), 200);

    assertThat(cancelled.get("status").asText()).isEqualTo("CANCELLED");
  }

  @Test
  void amountMinorValidationRejectsZeroOrNegative() throws Exception {
    postJson("/api/admin/payments", paymentJson("PENDING", "UTILITIES", 0L), adminToken(), 400);
    postJson("/api/admin/payments", paymentJson("PENDING", "UTILITIES", -100L), adminToken(), 400);
  }

  @Test
  void residentCannotCreatePayment() throws Exception {
    mockMvc.perform(post("/api/admin/payments")
            .header("Authorization", "Bearer " + residentToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(paymentJson("PENDING", "UTILITIES", 10000L)))
        .andExpect(status().isForbidden());
  }

  @Test
  void unauthenticatedRequestIsRejected() throws Exception {
    mockMvc.perform(get("/api/resident/payments"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void wrongRoleReturnsForbiddenForResidentEndpoint() throws Exception {
    mockMvc.perform(get("/api/resident/payments")
            .header("Authorization", "Bearer " + adminToken()))
        .andExpect(status().isForbidden());
  }

  private JsonNode createPayment(String status, String type) throws Exception {
    return postJson("/api/admin/payments", paymentJson(status, type, 123456L), adminToken(), 201);
  }

  private String paymentJson(String status, String type, Long amountMinor) {
    return """
        {
          "residentProfileId":1,
          "type":"%s",
          "status":"%s",
          "amountMinor":%d,
          "currency":"UAH",
          "periodYear":2026,
          "periodMonth":8,
          "titleUk":"Stage5 payment %s",
          "titleEn":"Stage5 payment",
          "descriptionUk":"Stage 5 payment test",
          "descriptionEn":"Stage 5 payment test",
          "dueDate":"%s"
        }
        """.formatted(type, status, amountMinor, UUID.randomUUID(), LocalDate.of(2026, 8, 20));
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
