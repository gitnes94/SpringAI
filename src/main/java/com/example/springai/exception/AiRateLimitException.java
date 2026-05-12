package com.example.springai.exception;

/**
 * Kastas när AI-tjänsten svarar med 429 (Too Many Requests) eller 503 (Service Unavailable).
 *
 * Denna klass är konfigurerad som includes= i @Retryable på ChatService,
 * vilket innebär att Spring Framework 7 automatiskt gör återförsök med
 * exponentiell backoff när detta undantag kastas.
 *
 * Om alla återförsök misslyckas fångas undantaget av GlobalExceptionHandler
 * och returneras som 503 Service Unavailable till klienten.
 */
public class AiRateLimitException extends AiServiceException {

    public AiRateLimitException(String message) {
        super(message);
    }
}
