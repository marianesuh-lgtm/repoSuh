package com.mrs.shakes.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mrs.shakes.config.ShakesProperties;
import com.mrs.shakes.service.StoryTempService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OllamaClient {

    private final RestTemplate restTemplate;
    private final ShakesProperties properties; // 생성자 주입
    //private final String OLLAMA_URL  ;

//    public OllamaClient(RestTemplate restTemplate,
//                        @Value("${ollama.base-url:http://suhmac.local:11434}") String baseUrl) {
//        this.restTemplate = restTemplate;
//        this.baseUrl = baseUrl;
//    }

    /**
     * Ollama에 텍스트 생성 요청 (간단한 chat/completions 스타일)
     * @param prompt 입력 프롬프트
     * @param model 사용 모델 (예: "llama3.1", "gemma2")
     * @return 생성된 텍스트
     */
    public String generateOllama(String prompt, int maxLength) {
    	log.info("generate  prompt:: {}", prompt);
    	String url = properties.getOllama().getBaseUrl() + "/api/generate";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = """
            {
              "model": "%s",
              "prompt": "%s",
              "stream": false
            }
             """.formatted(properties.getOllama().getModel(), prompt);
         //  """.formatted(model, prompt.replace("\"", "\\\""));
        
       	log.info("generate  requestBody:: {}", requestBody);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        String response = restTemplate.postForObject(url, request, String.class);

       	log.info("generate  response:: {}", response);
        
        // 실제로는 JSON 파싱 필요 (Jackson 등 사용 추천)
        // 여기서는 간단히 response에서 "response" 필드 추출 가정
        return extractResponseFromJson(response);
    }
    
    
    public String generate(String prompt) {
    	
    	String OLLAMA_URL = properties.getOllama().getBaseUrl() + "/api/generate";
        // Ollama API 스펙에 맞춘 요청 객체 생성
        Map<String, Object> request = new HashMap<>();
        request.put("model", properties.getOllama().getModel()); // 혹은 사용 중인 모델명 (예: gemma2)
        request.put("prompt", prompt);
        request.put("stream", false);   // 한 번에 응답 받기 위해 false 설정

//        request.put("repeat_penalty", 1.2);// 반복 방지 강도 높임 (기본 1.1)
//        request.put("top_p", 0.9);
        request.put("stop", new String[]{"}"}); // JSON 종료 시 즉시 중단    
        request.put("format", "json");  // JSON 모드 활성화

        Map<String, Object> options = new HashMap<>();
      	options.put("num_predict", 4096); // 생성 토큰 수를 대폭 늘림 (중요!)
      	options.put("num_ctx", 8192);    // 컨텍스트 창 크기 확보
      	options.put("temperature", 0.7); // 창의성과 형식 준수의 균형

      	request.put("options", options);      	

      	log.info("generate  request:: {}", request);
      	
        // POST 요청 실행
        ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_URL, request, Map.class);

      	log.info("generate  response:: {}", response);
        
        if (response.getBody() != null && response.getBody().containsKey("response")) {
            // Ollama는 실제 결과 텍스트를 "response" 필드에 담아 줍니다.
            return (String) response.getBody().get("response");
        }

        throw new RuntimeException("Ollama 응답이 비어있습니다.");
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