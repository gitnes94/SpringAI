package com.example.springai.service;

import com.example.springai.config.AiProperties;
import com.example.springai.exception.AiRateLimitException;
import com.example.springai.exception.AiServiceException;
import com.example.springai.memory.ConversationMemory;
import com.example.springai.model.ChatRequest;
import com.example.springai.model.ChatResponse;
import com.example.springai.model.ConversationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final RestClient restClient;
    private final AiProperties properties;
    private final PersonalityService personalityService;
    private final ConversationMemory memory;

    public ChatService(RestClient restClient,
                       AiProperties properties,
                       PersonalityService personalityService,
                       ConversationMemory memory) {
        this.restClient = restClient;
        this.properties = properties;
        this.personalityService = personalityService;
        this.memory = memory;
    }

    @Retryable(
            retryFor = AiRateLimitException.class,
            maxAttemptsExpression = "${ai.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${ai.retry.delay:1000}",
                    multiplierExpression = "${ai.retry.multiplier:2.0}"
            )
    )
    public ChatResponse chat(ChatRequest request) {
        String sessionId = resolveSessionId(request.sessionId());

        log.debug("Hanterar anrop – personality={}, sessionId={}", request.personality(), sessionId);

        String systemPrompt = personalityService.getSystemPrompt(request.personality());
        List<ConversationMessage> history = memory.getHistory(sessionId);

        List<OpenAiMessage> messages = new ArrayList<>();
        messages.add(new OpenAiMessage("system", systemPrompt));
        history.forEach(m -> messages.add(new OpenAiMessage(m.role(), m.content())));
        messages.add(new OpenAiMessage("user", request.message()));

        OpenAiRequest aiRequest = new OpenAiRequest(properties.model(), messages);

        OpenAiResponse response = restClient.post()
                .uri("/chat/completions")
                .body(aiRequest)
                .retrieve()
                .body(OpenAiResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new AiServiceException("Tomt svar från AI-tjänsten.");
        }

        String reply = response.choices().getFirst().message().content();
        log.debug("Svar mottaget, sessionId={}", sessionId);

        memory.addMessage(sessionId, new ConversationMessage("user", request.message()));
        memory.addMessage(sessionId, new ConversationMessage("assistant", reply));

        return new ChatResponse(reply, sessionId);
    }

    private String resolveSessionId(String sessionId) {
        return (sessionId != null && !sessionId.isBlank())
                ? sessionId
                : UUID.randomUUID().toString();
    }

    record OpenAiMessage(String role, String content) {}
    record OpenAiRequest(String model, List<OpenAiMessage> messages) {}
    record Choice(OpenAiMessage message) {}
    record OpenAiResponse(List<Choice> choices) {}
}