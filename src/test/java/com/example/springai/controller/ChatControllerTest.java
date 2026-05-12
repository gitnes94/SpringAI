package com.example.springai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.example.springai.model.ChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatControllerTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().port(9090))
            .build();

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        wireMock.resetAll();
    }

    // ── Testfall 1: Lyckat anrop ─────────────────────────────────────────────

    @Test
    @DisplayName("Ska returnera AI-svar vid lyckat anrop")
    void shouldReturnSuccessfulChatResponse() throws Exception {
        wireMock.stubFor(WireMock.post(urlEqualTo("/chat/completions"))
                .willReturn(okJson("""
                        {
                            "choices": [
                                {"message": {"role": "assistant", "content": "En for-loop: for (int i = 0; i < 10; i++) {}"}}
                            ]
                        }
                        """)));

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRequest("coder", "Hur skriver jag en for-loop?", "session-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("En for-loop: for (int i = 0; i < 10; i++) {}"))
                .andExpect(jsonPath("$.sessionId").value("session-1"));
    }

    // ── Testfall 2: Retry vid 429 → lyckas på andra försöket ─────────────────

    @Test
    @DisplayName("Ska försöka igen efter 429 och returnera lyckat svar")
    void shouldRetryOnRateLimitAndSucceed() throws Exception {
        wireMock.stubFor(WireMock.post(urlEqualTo("/chat/completions"))
                .inScenario("rate-limit-retry")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(429))
                .willSetStateTo("retrying"));

        wireMock.stubFor(WireMock.post(urlEqualTo("/chat/completions"))
                .inScenario("rate-limit-retry")
                .whenScenarioStateIs("retrying")
                .willReturn(okJson("""
                        {
                            "choices": [
                                {"message": {"role": "assistant", "content": "Svar efter retry!"}}
                            ]
                        }
                        """)));

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRequest("helper", "Hej!", "session-retry"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Svar efter retry!"));

        wireMock.verify(2, postRequestedFor(urlEqualTo("/chat/completions")));
    }

    // ── Testfall 3: Alla retries misslyckas → 503 ────────────────────────────

    @Test
    @DisplayName("Ska returnera 503 när AI-tjänsten alltid svarar med 429")
    void shouldReturn503WhenAllRetriesFail() throws Exception {
        wireMock.stubFor(WireMock.post(urlEqualTo("/chat/completions"))
                .willReturn(aResponse().withStatus(429)));

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRequest("helper", "Hej!", "session-fail"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.timestamp").exists());

        // Verifierar att minst 3 anrop gjordes (retry fungerar)
        wireMock.verify(moreThanOrExactly(3), postRequestedFor(urlEqualTo("/chat/completions")));
    }

    // ── Testfall 4: Retry vid 503 ────────────────────────────────────────────

    @Test
    @DisplayName("Ska försöka igen efter 503")
    void shouldRetryOn503() throws Exception {
        wireMock.stubFor(WireMock.post(urlEqualTo("/chat/completions"))
                .inScenario("503-retry")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("recovered"));

        wireMock.stubFor(WireMock.post(urlEqualTo("/chat/completions"))
                .inScenario("503-retry")
                .whenScenarioStateIs("recovered")
                .willReturn(okJson("""
                        {
                            "choices": [
                                {"message": {"role": "assistant", "content": "Tillbaka efter 503!"}}
                            ]
                        }
                        """)));

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRequest("pirate", "Arrr!", "session-503"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Tillbaka efter 503!"));
    }

    // ── Testfall 5: Valideringsfel – personality tom ──────────────────────────

    @Test
    @DisplayName("Ska returnera 400 när personality är tom")
    void shouldReturn400WhenPersonalityIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personality": "", "message": "Hej?", "sessionId": "s1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // ── Testfall 6: Valideringsfel – message tom ──────────────────────────────

    @Test
    @DisplayName("Ska returnera 400 när message är tom")
    void shouldReturn400WhenMessageIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personality": "coder", "message": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── Testfall 7: Sessionsminne ─────────────────────────────────────────────

    @Test
    @DisplayName("Ska returnera samma sessionId och hantera uppföljningsfrågor")
    void shouldIncludeHistoryInSubsequentRequests() throws Exception {
        wireMock.stubFor(WireMock.post(urlEqualTo("/chat/completions"))
                .willReturn(okJson("""
                        {
                            "choices": [
                                {"message": {"role": "assistant", "content": "Polymorfism betyder..."}}
                            ]
                        }
                        """)));

        String sessionId = "session-memory-test";

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRequest("coder", "Vad är polymorfism?", sessionId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId));

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRequest("coder", "Kan du ge ett kodexempel?", sessionId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId));

        wireMock.verify(2, postRequestedFor(urlEqualTo("/chat/completions")));
    }

    // ── Testfall 8: Auto-genererat sessions-ID ───────────────────────────────

    @Test
    @DisplayName("Ska skapa sessions-ID automatiskt när inget anges")
    void shouldAutoGenerateSessionIdWhenNotProvided() throws Exception {
        wireMock.stubFor(WireMock.post(urlEqualTo("/chat/completions"))
                .willReturn(okJson("""
                        {
                            "choices": [
                                {"message": {"role": "assistant", "content": "Hej!"}}
                            ]
                        }
                        """)));

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRequest("helper", "Hej!", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").isNotEmpty());
    }
}