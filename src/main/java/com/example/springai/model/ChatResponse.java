package com.example.springai.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Svar från AI-chatten")
public record ChatResponse(

        @Schema(description = "AI:ns svar på meddelandet")
        String reply,

        @Schema(
                description = "Sessions-ID – spara detta för att fortsätta konversationen",
                example = "user-123-abc"
        )
        String sessionId

) {}