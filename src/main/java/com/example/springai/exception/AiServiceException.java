package com.example.springai.exception;

/**
 * Basklass för alla AI-tjänst-relaterade undantag.
 * Fångas av GlobalExceptionHandler och returneras som 502 Bad Gateway.
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}