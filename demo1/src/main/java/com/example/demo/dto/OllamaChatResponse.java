package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ollama /api/chat 응답 바디
 */
@Data
@NoArgsConstructor
public class OllamaChatResponse {

    private Message message;

    private Boolean done;

    @Data
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
