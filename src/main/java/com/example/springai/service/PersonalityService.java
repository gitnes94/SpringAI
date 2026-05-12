package com.example.springai.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Mappar personlighetsidentifierare till specifika systemprompts.
 *
 * Lägg till nya personligheter som entries i SYSTEM_PROMPTS.
 * Faller tillbaka på "helper" om en okänd personlighet anges.
 */
@Service
public class PersonalityService {

    private static final String DEFAULT_PERSONALITY = "helper";

    private static final Map<String, String> SYSTEM_PROMPTS = Map.of(
            "helper",
            "Du är en hjälpsam, vänlig assistent. Svara tydligt och kortfattat på svenska " +
                    "eller på det språk användaren skriver på.",

            "pirate",
            "You are a salty old pirate! You must speak entirely in pirate dialect at all times. " +
                    "Use words like 'Arrr', 'matey', 'ye', 'shiver me timbers', and 'landlubber'. " +
                    "Never break character, even when answering technical questions.",

            "coder",
            "You are an expert software engineer with deep knowledge of Java, Spring Boot, and " +
                    "software architecture. Provide precise, well-commented code examples. Always explain " +
                    "the 'why' behind your solutions. Use best practices and mention relevant design " +
                    "patterns when applicable."
    );

    /**
     * Returnerar systemprompten för angiven personlighet.
     * Faller tillbaka på DEFAULT_PERSONALITY om personligheten inte känns igen.
     */
    public String getSystemPrompt(String personality) {
        return SYSTEM_PROMPTS.getOrDefault(
                personality.toLowerCase().trim(),
                SYSTEM_PROMPTS.get(DEFAULT_PERSONALITY)
        );
    }
}