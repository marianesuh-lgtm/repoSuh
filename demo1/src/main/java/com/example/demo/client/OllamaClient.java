package com.example.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.demo.service.StoryService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OllamaClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public OllamaClient(RestTemplate restTemplate,
                        @Value("${ollama.base-url:http://suh.local:11434}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Ollama에 텍스트 생성 요청 (간단한 chat/completions 스타일)
     * @param prompt 입력 프롬프트
     * @param model 사용 모델 (예: "llama3.1", "gemma2")
     * @return 생성된 텍스트
     */
    public String generate(String prompt, int maxLength) {
        String url = baseUrl + "/api/generate";
        String model = "my-eeve";
    	log.info("generate  prompt:: {}", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = """
            {
              "model": "%s",
              "prompt": "%s",
              "stream": false
            }
             """.formatted(model, prompt);
         //  """.formatted(model, prompt.replace("\"", "\\\""));
        
       	log.info("generate  requestBody:: {}", requestBody);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        String response = restTemplate.postForObject(url, request, String.class);

       	log.info("generate  response:: {}", response);
        
        // 실제로는 JSON 파싱 필요 (Jackson 등 사용 추천)
        // 여기서는 간단히 response에서 "response" 필드 추출 가정
        return extractResponseFromJson(response);
    }

    // JSON에서 텍스트 추출 (간단 예시, 실제로는 ObjectMapper 사용)
    private String extractResponseFromJson(String json) {
        // 실제 구현 시 Jackson이나 Gson 사용
        // 예: {"response": "안녕하세요"} → "안녕하세요" 추출
       	log.info("extractResponseFromJson  json:: {}", json);
        int start = json.indexOf("\"response\":\"") + 12;
        int end = json.lastIndexOf("\"}");
        return json.substring(start, end).replace("\\n", "\n");
    }
}