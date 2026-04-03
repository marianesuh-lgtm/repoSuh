package com.mrs.shakes.service;

import com.mrs.shakes.dto.OllamaChatRequest;
import com.mrs.shakes.dto.OllamaChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Service
public class OllamaChatService {

    private final WebClient ollamaWebClient;

    public OllamaChatService(@Qualifier("ollamaWebClient") WebClient ollamaWebClient) {
        this.ollamaWebClient = ollamaWebClient;
    }

    @Value("${ollama.default-model:gemma2}")
    private String defaultModel;

    /**
     * Ollama /api/chat 호출 후 어시스턴트 답변 텍스트 반환
     */
    public String ask(String question, String model) {
        String effectiveModel = (model != null && !model.isBlank()) ? model : defaultModel;

        OllamaChatRequest.Message userMessage = new OllamaChatRequest.Message("user", question);
        OllamaChatRequest request = OllamaChatRequest.builder()
                .model(effectiveModel)
                .messages(List.of(userMessage))
                .stream(false)
                .build();

        try {
            OllamaChatResponse response = ollamaWebClient
                    .post()
                    .uri("/api/chat")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaChatResponse.class)
                    .block();

            if (response != null && response.getMessage() != null) {
                return response.getMessage().getContent() != null
                        ? response.getMessage().getContent()
                        : "";
            }
            return "";
        } catch (WebClientResponseException e) {
            log.error("Ollama API error: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Ollama 요청 실패: " + e.getMessage());
        }
    }
}
