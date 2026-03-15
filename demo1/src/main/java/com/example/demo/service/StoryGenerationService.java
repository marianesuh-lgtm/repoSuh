package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoryGenerationService {

    private final PromptCompositeService promptCompositeService;
    private final RestTemplate restTemplate = new RestTemplate(); // 간단한 호출을 위해 사용

    public String askToOllama(String charId, String userTopic) {
        String systemPrompt = promptCompositeService.buildOllamaSystemPrompt(charId);
        String url = "http://suh.local:11434/api/generate";

        // Ollama API 요청 데이터 구성 (JSON 형태)
        Map<String, Object> request = new HashMap<>();
        request.put("model", "llama3"); // 사용 중인 모델명
        request.put("prompt", systemPrompt + "\n주제: " + userTopic);
        request.put("stream", false);

        // API 호출
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
        return (String) response.get("response");
    }
}