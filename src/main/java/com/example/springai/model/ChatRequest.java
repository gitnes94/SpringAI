package com.example.springai.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Förfrågan till AI-chatten")
public record ChatRequest(

        @NotBlank(message = "Personality får inte vara tom")
        @Schema(
                description = "Personlighet som styr systemprompten",
                example = "coder",
                allowableValues = {"helper", "pirate", "coder"}
        )
        String personality,

        @NotBlank(message = "Message får inte vara tom")
        @Schema(description = "Användarens fråga", example = "Hur skriver jag en for-loop i Java?")
        String message,

        @Schema(
                description = "Sessions-ID för att hålla konversationshistorik. " +
                        "Utelämnas det skapas en ny session automatiskt.",
                example = "user-123-abc",
                nullable = true
        )
        String sessionId

) {}