package com.houseapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseapp.entity.Role;
import com.houseapp.repository.UserRepository;
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
class Stage2ResidentsApartmentsIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Test
  void adminCanListApartments() throws Exception {
    JsonNode apartments = getJson("/api/admin/apartments", adminToken(), 200);

    assertThat(apartments).hasSizeGreaterThanOrEqualTo(5);
  }

  @Test
  void adminCanCreateApartment() throws Exception {
    JsonNode created = createApartment(shortApartmentNumber("T"), 20);

    assertThat(created.get("apartmentNumber").asText()).startsWith("T-");
    assertThat(created.get("status").asText()).isEqualTo("VACANT");
  }

  @Test
  void duplicateApartmentNumberReturnsConflict() throws Exception {
    String number = shortApartmentNumber("D");
    createApartment(number, 20);

    postJson("/api/admin/apartments", apartmentJson(number, 21), adminToken(), 409);
  }

  @Test
  void adminCanListResidents() throws Exception {
    JsonNode residents = getJson("/api/admin/residents", adminToken(), 200);

    assertThat(residents).hasSizeGreaterThanOrEqualTo(5);
    assertThat(residents.get(0).has("mustChangePassword")).isTrue();
  }

  @Test
  void adminCanCreateResidentWithTemporaryPassword() throws Exception {
    String email = "new." + UUID.randomUUID() + "@example.com";

    JsonNode created = postJson("/api/admin/residents", """
        {
          "name":"New Resident",
          "email":"%s",
          "temporaryPassword":"Temporary123!",
          "phone":"+380501110000"
        }
        """.formatted(email), adminToken(), 201);

    assertThat(created.get("email").asText()).isEqualTo(email);
    assertThat(created.get("mustChangePassword").asBoolean()).isTrue();
    assertThat(userRepository.findByEmail(email).orElseThrow().getRole()).isEqualTo(Role.RESIDENT);
  }

  @Test
  void weakTemporaryPasswordIsRejected() throws Exception {
    String email = "weak." + UUID.randomUUID() + "@example.com";

    postJson("/api/admin/residents", """
        {"name":"Weak Resident","email":"%s","temporaryPassword":"weak"}
        """.formatted(email), adminToken(), 400);
  }

  @Test
  void residentCannotAccessAdminResidentEndpoints() throws Exception {
    mockMvc.perform(get("/api/admin/residents")
            .header("Authorization", "Bearer " + residentToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void residentCannotReadAnotherResidentProfileThroughAdminApi() throws Exception {
    JsonNode residents = getJson("/api/admin/residents", adminToken(), 200);
    Long otherProfileId = residents.get(0).get("id").asLong();

    mockMvc.perform(get("/api/admin/residents/" + otherProfileId)
            .header("Authorization", "Bearer " + residentToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void residentCanReadOwnProfile() throws Exception {
    JsonNode profile = getJson("/api/resident/profile", residentToken(), 200);

    assertThat(profile.get("email").asText()).isEqualTo("resident@house.com");
    assertThat(profile.has("notes")).isFalse();
  }

  @Test
  void residentCanUpdateOwnAllowedProfileFields() throws Exception {
    JsonNode profile = putJson("/api/resident/profile", """
        {
          "phone":"+380501119999",
          "emergencyContactName":"Emergency Contact",
          "emergencyContactPhone":"+380501118888",
          "preferredLanguage":"en"
        }
        """, residentToken(), 200);

    assertThat(profile.get("phone").asText()).isEqualTo("+380501119999");
    assertThat(profile.get("preferredLanguage").asText()).isEqualTo("en");
  }

  @Test
  void residentCannotUpdateRestrictedFields() throws Exception {
    putJson("/api/resident/profile", """
        {"phone":"+380501110101","email":"changed@example.com","apartmentId":1,"notes":"hidden"}
        """, residentToken(), 400);
  }

  @Test
  void occupiedApartmentCannotBeAssignedToAnotherResident() throws Exception {
    JsonNode apartments = getJson("/api/admin/apartments", adminToken(), 200);
    long occupiedApartmentId = findApartmentId(apartments, "101");
    String email = "occupied." + UUID.randomUUID() + "@example.com";

    postJson("/api/admin/residents", """
        {
          "name":"Occupied Test",
          "email":"%s",
          "temporaryPassword":"Temporary123!",
          "apartmentId":%d
        }
        """.formatted(email, occupiedApartmentId), adminToken(), 409);
  }

  private JsonNode createApartment(String apartmentNumber, int floor) throws Exception {
    return postJson("/api/admin/apartments", apartmentJson(apartmentNumber, floor), adminToken(), 201);
  }

  private String apartmentJson(String apartmentNumber, int floor) {
    return """
        {
          "buildingSection":"T",
          "floor":%d,
          "apartmentNumber":"%s",
          "areaSqM":42.50,
          "rooms":2,
          "status":"VACANT"
        }
        """.formatted(floor, apartmentNumber);
  }

  private String shortApartmentNumber(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
  }

  private long findApartmentId(JsonNode apartments, String number) {
    for (JsonNode apartment : apartments) {
      if (number.equals(apartment.get("apartmentNumber").asText())) {
        return apartment.get("id").asLong();
      }
    }
    throw new AssertionError("Apartment not found: " + number);
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
    MvcResult result = mockMvc.perform(get(path).header("Authorization", "Bearer " + accessToken))
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

  private JsonNode readBody(MvcResult result) throws Exception {
    String body = result.getResponse().getContentAsString();
    return body == null || body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
  }
}
