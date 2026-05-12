package com.example.springai.exception;

import com.example.springai.model.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centraliserad felhantering för hela applikationen.
 * Alla undantag fångas här och returneras som ett strukturerat
 * JSON-svar (ApiErrorResponse) istället för Springs default-felformat.
 */
@ControllerAdvice
public class GlobalExceptionHandler {


     // 503 – AI-tjänsten otillgänglig efter att alla @Retryable-försök är slut.
    @ExceptionHandler(AiRateLimitException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(AiRateLimitException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error(503, "Service Unavailable", ex.getMessage()));
    }

     // 502 – Kommunikationsfel mot AI-tjänsten (ej retry-bart).
    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleAiService(AiServiceException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(error(502, "Bad Gateway", ex.getMessage()));
    }

      //400 – Valideringsfel i request-bodyn (t.ex. @NotBlank-fält saknas).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .badRequest()
                .body(error(400, "Bad Request", details));
    }


     //500 – Oväntade fel som inte fångats av specifika handlers.

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(500, "Internal Server Error", "Ett oväntat fel inträffade."));
    }

    private ApiErrorResponse error(int status, String error, String message) {
        return new ApiErrorResponse(status, error, message, Instant.now());
    }
}
