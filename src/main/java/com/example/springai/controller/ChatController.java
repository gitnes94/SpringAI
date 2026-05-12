package com.example.springai.controller;

import com.example.springai.model.ApiErrorResponse;
import com.example.springai.model.ChatRequest;
import com.example.springai.model.ChatResponse;
import com.example.springai.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Chat", description = "AI-chatt med stöd för personligheter och konversationsminne")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    @Operation(
            summary = "Skicka ett meddelande till AI:n",
            description = """
                    Skickar ett meddelande till en LLM med vald personlighet.
                    Om sessionId anges inkluderas tidigare konversationshistorik,
                    vilket möjliggör uppföljningsfrågor.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lyckat svar från AI:n",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ogiltigt request (saknade fält)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "AI-tjänsten otillgänglig efter retries",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Kommunikationsfel mot AI-tjänsten",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(request));
    }
}