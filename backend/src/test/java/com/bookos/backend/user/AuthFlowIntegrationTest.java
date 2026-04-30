package com.bookos.backend.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userCanRegisterAndLoadCurrentUser() throws Exception {
        String registerBody = """
                {
                  "email": "reader@bookos.local",
                  "password": "Password123!",
                  "displayName": "Reader One"
                }
                """;

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.email").value("reader@bookos.local"))
                .andExpect(jsonPath("$.data.user.onboardingCompleted").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(registerResponse);
        String token = json.path("data").path("token").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("reader@bookos.local"))
                .andExpect(jsonPath("$.data.displayName").value("Reader One"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(false));
    }

    @Test
    void userCanSaveOnboardingPreferences() throws Exception {
        String registerBody = """
                {
                  "email": "onboarding@bookos.local",
                  "password": "Password123!",
                  "displayName": "Onboarding Reader"
                }
                """;

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(registerResponse);
        String token = json.path("data").path("token").asText();

        String onboardingBody = """
                {
                  "onboardingCompleted": true,
                  "primaryUseCase": "TRACK_READING",
                  "startingMode": "READER",
                  "preferredDashboardMode": "READER"
                }
                """;

        mockMvc.perform(put("/api/users/me/onboarding")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.data.primaryUseCase").value("TRACK_READING"))
                .andExpect(jsonPath("$.data.startingMode").value("READER"))
                .andExpect(jsonPath("$.data.preferredDashboardMode").value("READER"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.data.primaryUseCase").value("TRACK_READING"))
                .andExpect(jsonPath("$.data.startingMode").value("READER"))
                .andExpect(jsonPath("$.data.preferredDashboardMode").value("READER"));
    }
}
