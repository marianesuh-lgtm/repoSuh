package com.mrs.shakes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Ollama /api/chat 요청 바디
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OllamaChatRequest {

    private String model;

    private List<Message> messages;

    @JsonProperty("stream")
    private boolean stream;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
