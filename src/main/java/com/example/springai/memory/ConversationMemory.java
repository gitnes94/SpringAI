package com.example.springai.memory;

import com.example.springai.model.ConversationMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory lagring av konversationshistorik per sessions-ID.
 *
 * ConcurrentHashMap används för trådsäkerhet vid parallella anrop.
 * Historiken begränsas till MAX_HISTORY meddelanden per session för
 * att hålla nere context-storleken mot AI-tjänsten.
 *
 * OBS: Historiken försvinner vid omstart. För persistent historik
 * skulle man byta ut denna mot en databas-implementation.
 */
@Component
public class ConversationMemory {

    private static final int MAX_HISTORY = 20;

    private final Map<String, List<ConversationMessage>> sessions = new ConcurrentHashMap<>();

    /**
     * Hämtar konversationshistoriken för en session.
     * Returnerar en tom lista om sessionen inte finns.
     */
    public List<ConversationMessage> getHistory(String sessionId) {
        return Collections.unmodifiableList(
                sessions.getOrDefault(sessionId, List.of())
        );
    }

    /**
     * Lägger till ett meddelande i en sessions historik.
     * Om historiken överstiger MAX_HISTORY tas de äldsta bort.
     */
    public void addMessage(String sessionId, ConversationMessage message) {
        List<ConversationMessage> history = sessions.computeIfAbsent(
                sessionId, k -> new ArrayList<>()
        );
        history.add(message);

        if (history.size() > MAX_HISTORY) {
            sessions.put(sessionId,
                    new ArrayList<>(history.subList(history.size() - MAX_HISTORY, history.size())));
        }
    }

    /**
     * Rensar konversationshistoriken för en specifik session.
     */
    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }
}