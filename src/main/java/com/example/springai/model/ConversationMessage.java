package com.example.springai.model;

/**
 * Representerar ett enskilt meddelande i konversationshistoriken.
 *
 * @param role    "user" eller "assistant"
 * @param content Meddelandets textinnehåll
 */
public record ConversationMessage(String role, String content) {}