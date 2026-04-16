package com.mrs.shakes.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrs.shakes.client.OllamaClient;
import com.mrs.shakes.config.ShakesProperties;
import com.mrs.shakes.domain.story.StoryPage;
import com.mrs.shakes.dto.CharacterDTO;
import com.mrs.shakes.dto.GenerateBookRequest;
import com.mrs.shakes.dto.RefinedStoryResponse;
import com.mrs.shakes.dto.GenerateBookRequest.StorySelections;
import com.mrs.shakes.infrastructure.prompt.PromptProvider;
import com.mrs.shakes.repository.StoryMasterRepository;
import com.mrs.shakes.service.CharacterService;
import com.mrs.shakes.service.IncrementalStoryService;
import com.mrs.shakes.service.StoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class ServiceUtil {

    private final StoryMasterRepository storyMasterRepository;
    private final OllamaClient ollamaClient; // Ollama와 통신하는 커스텀 클라이언트
    private final PromptProvider promptProvider; // .st 파일을 읽어주는 컴포넌트
    private final ObjectMapper objectMapper;
    private final CharacterService characterService ;
    private final IncrementalStoryService refineService;
    private final String imgUrl = "http://myShakes.ddns.net:8080/images/characters/";
	private final RestTemplate restTemplate = new RestTemplate();

    private final ShakesProperties properties; // 생성자 주입
    @Autowired
    private ResourceLoader resourceLoader;
    
    private final StoryMapper storyMapper;

    @JsonProperty("raw_image_keywords")
    private String rawImageKeywords;
	
    public String generateAndPollImage(RestTemplate restTemplate, StoryPage item, String mood, CharacterDTO characterDto) throws Exception {
        // 1. 워크플로우 JSON 로드 및 프롬프트 치환

       // CharacterDTO character = characterService.getCharacterMapperInfo(charId);
        
        //log.info("item:: {}",item);
    	
    	String charater = JsonUtils.escapeJsonValue(characterDto.getAppearance());
    	//String mood = JsonUtils.escapeJsonValue(character.getPersonalityTraits());
    	String background = "";
    	String style =JsonUtils.escapeJsonValue(characterDto.getArtStyle());
    	String action ="";
    	String negative =JsonUtils.escapeJsonValue(characterDto.getNegative());
    	String image = imgUrl+characterDto.getUrlImg();
    	String subImage = imgUrl+characterDto.getSubUrlImg();
    	//String story22 =JsonUtils.escapeJsonValue(item.getText());
    	
        String workflowJson = new String(loadWorkflow());
        String rawPrompt = item.getImagePrompt();
        if (rawPrompt == null) {
            log.error("LLM으로부터 image_prompt를 받지 못했습니다. 기본 프롬프트를 사용합니다.");
            rawPrompt = "A cute cat and a bird in Pixar style"; // 폴백 프롬프트
        }
        String modifiedJson = workflowJson.replace("{{user_prompt}}" ,  rawPrompt );
//        String modifiedJson = workflowJson.replace("{{user_prompt}}"
//        		 , charater+", "+ JsonUtils.escapeJsonValue(item.getMood())+","+ JsonUtils.escapeJsonValue(item.getBackground())+","+ JsonUtils.escapeJsonValue(item.getAction()) );
        //modifiedJson = modifiedJson.replace("{{user_negative}}", characterDto.getSubNegative()+", "+negative +", grandfather, old man, male, masculine, beard, mustache , extra legs, extra paws, mutated limbs, fused legs, extra arms, extra hands, fused fingers, malformed limbs" );
        //modifiedJson = modifiedJson.replace("{{user_character}}", charater );
//        modifiedJson = modifiedJson.replace("{{user_mood}}", item.getMood() );
//        modifiedJson = modifiedJson.replace("{{user_background}}", item.getBackground() );
//        modifiedJson = modifiedJson.replace("{{user_style}}", style );
        modifiedJson = modifiedJson.replace("{{user_image}}", image );
        //modifiedJson = modifiedJson.replace("{{user_subImage}}", subImage );

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        
        //log.info("prompt::: {}", promptUsed +","+story+ ", children's book illustration style, cute, vibrant colors, soft lighting");
        //log.info("generateAndPollImage modifiedJson::: {}", modifiedJson);

        // 2. ComfyUI 규격에 맞게 페이로드 생성
        Map<String, Object> workflow = mapper.readValue(modifiedJson, Map.class);
        Map<String, Object> finalPayload = new HashMap<>();
        finalPayload.put("prompt", workflow);
        String finalJson = mapper.writeValueAsString(finalPayload);

        // 3. ComfyUI /prompt 엔드포인트 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(finalJson, headers);
        StopWatch stopWatch = new StopWatch("Ollama Response Time Check");
        
        stopWatch.start("Comfy Prompt Generation"); // 측정 시작
        
        //log.info("entity::: {}", entity);
        
        // restTemplate은 Bean으로 등록된 것을 사용 (Thread-safe)
        String response = restTemplate.postForObject(properties.getComfy().getBaseUrl()+"/prompt", entity, String.class);

        //log.info("generateAndPollImage response::: {}", response);
        stopWatch.stop(); // 측정 종료

       // log.info("Ollama 응답 완료! 소요 시간: {}s", stopWatch.getTotalTimeSeconds());
       // log.info(stopWatch.prettyPrint()); // 상세 리포트 출력
        
        // 4. prompt_id 추출
        Map<String, Object> respMap = objectMapper.readValue(response, Map.class);
        String promptId = (String) respMap.get("prompt_id");
        //log.info("generateAndPollImage promptId::: {}", promptId);

        // 5. 이미지 생성이 완료될 때까지 Polling (기존 메서드 활용)
        return pollForImage(restTemplate, promptId); 
    }

    // polling 예시 메서드 (간단 버전)
    public String pollForImage(RestTemplate restTemplate, String promptId) throws Exception {
        int attempts = 0;
        final int maxAttempts = 60;
        final long sleepMs = 50000;
        ObjectMapper mapper = new ObjectMapper();
        
        log.info("pollForImage promptId::: {}", promptId);
        
        while (attempts < maxAttempts) {  // 5분 타임아웃
            Thread.sleep(sleepMs);  // 5초 대기
            String history = restTemplate.getForObject(properties.getComfy().getBaseUrl()+"/history/" + promptId, String.class);
            log.info("pollForImage history::: {}", history);
            
            if (history == null || history.trim().isEmpty()) {
                attempts++;
                continue;
            }	    
            
         // JSON 파싱
            JsonNode root = mapper.readTree(history);
            JsonNode promptNode = root.path(promptId);  // promptId가 키
            
            if (promptNode.isMissingNode() || promptNode.isNull()) {
                attempts++;
                continue;
            }
            
            JsonNode outputs = promptNode.path("outputs");
            if (outputs.isMissingNode() || !outputs.isObject() || outputs.size() == 0) {
                attempts++;
                continue;
            }
            
         // outputs 안의 모든 노드 순회하면서 images 찾기 (보통 Save Image 노드 1개)
            Iterator<Map.Entry<String, JsonNode>> fields = outputs.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode nodeOutput = entry.getValue();
                JsonNode images = nodeOutput.path("images");

                if (images.isArray() && !images.isEmpty()) {
                    // 첫 번째 이미지 (보통 하나만 있음)
                    JsonNode firstImage = images.get(0);
                    String filename = firstImage.path("filename").asText(null);
                    String subfolder = firstImage.path("subfolder").asText("");
                    String type = firstImage.path("type").asText("output");
        	        //log.info("pollForImage filename::: {}", filename);
                    if (filename != null && !filename.isEmpty()) {
                        // ComfyUI /view 엔드포인트로 이미지 URL 구성
                        // 형식: http://127.0.0.1:8188/view?filename=xxx.png&subfolder=&type=output
                        //String viewUrl = "http://172.30.1.38:8188/view" +
        	            String viewUrl = "/view" +
                            "?filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8) +
                            "&subfolder=" + URLEncoder.encode(subfolder, StandardCharsets.UTF_8) +
                            "&type=" + URLEncoder.encode(type, StandardCharsets.UTF_8);

    	    	        log.info("pollForImage viewUrl::: {}", viewUrl);
                        
                        return viewUrl;  // 이 URL을 프론트에 주면 바로 이미지 로드 가능
                    }
                }
            }

            attempts++;
        }
            // history JSON 파싱해서 이미지 URL 추출 (outputs 노드 안 images 배열)
            // 예: outputs → node_id → images[0].filename
            // 실제로는 Jackson으로 파싱해서 ComfyUI/output 폴더 경로 조합
            // 임시: 완성되면 "http://suh.local:8188/view?filename=..." 형식 반환
//            if (history.contains("\"status\": \"success\"")) {
//                return "http://suh.local:8188/output/generated_image.png";  // 실제 구현 필요
//            }
//            attempts++;
//        }
        throw new RuntimeException("Image generation timeout after " + maxAttempts + " attempts");
    }

	public byte[] loadWorkflow() throws IOException {
	    // classpath:를 사용하여 resources 폴더 내의 파일을 찾습니다.
	    Resource resource = resourceLoader.getResource(properties.getComfy().getWorkflowPath());
	    return Files.readAllBytes(Paths.get(resource.getURI()));
	}

    public RefinedStoryResponse parseRefinedResponse(String jsonResponse) {
        try {
            // 1. 방어 로직: Ollama가 앞뒤에 붙인 불필요한 텍스트 제거
            int startIndex = jsonResponse.indexOf("{");
            int endIndex = jsonResponse.lastIndexOf("}");
            
            if (startIndex == -1 || endIndex == -1) {
            	log.error("잘못된 AI 응답: {}", jsonResponse);
            	throw new RuntimeException("유효한 JSON 응답을 찾을 수 없습니다.");
            }
            
            String cleanJson = jsonResponse.substring(startIndex, endIndex + 1).replaceAll("\\r\\n|\\r|\\n", " ");

            // 2. String -> Object 변환
            return objectMapper.readValue(cleanJson, RefinedStoryResponse.class);

        } catch (JsonProcessingException e) {
        	// 만약 끝이 잘린 경우(Unexpected end-of-input)를 위한 예외 처리
            log.error("AI 응답이 불완전합니다. 재시도를 권장합니다.");
            // 여기서 재시도 로직을 태우거나, 기본값을 반환하는 처리가 필요합니다.
            log.error("JSON 파싱 에러 발생: {}", e.getMessage());
            throw new RuntimeException("데이터 변환 중 오류가 발생했습니다.", e);
        }
    }    

    // 페이지 번호에 따라 기승전결(Phase)을 판단하는 간단한 로직
    public String getPhase(int currentPage, int totalPages) {
        float progress = (float) currentPage / totalPages;
        if (progress <= 0.25) return "기 (도입, 배경 및 캐릭터 소개)";
        if (progress <= 0.50) return "승 (전개, 사건의 시작)";
        if (progress <= 0.75) return "전 (위기 및 갈등 심화)";
        return "결 (결말 및 교훈)";
    }


    public String formatUserSelections(StorySelections selections) {
        if (selections == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("[선택된 이야기 요소]\n");

        if (selections.get기() != null) {
            sb.append("- 주인공: ").append(selections.get기().getCharacter().getLabel()).append("\n");
            sb.append("- 장소: ").append(selections.get기().getPlace().getLabel()).append("\n");
            sb.append("- 기분/상황: ").append(selections.get기().getMood().getLabel()).append("\n");
        }
        if (selections.get승() != null) {
            sb.append("- 사건: ").append(selections.get승().getEvent().getLabel()).append("\n");
            sb.append("- 동행: ").append(selections.get승().getCompanion().getLabel()).append("\n");
        }
        if (selections.get전() != null) {
            sb.append("- 문제: ").append(selections.get전().getProblem().getLabel()).append("\n");
            sb.append("- 시도: ").append(selections.get전().getTryAction().getLabel()).append("\n");
        }
        if (selections.get결() != null) {
            sb.append("- 해결: ").append(selections.get결().getSolution().getLabel()).append("\n");
            sb.append("- 엔딩: ").append(selections.get결().getEnding().getLabel()).append("\n");
        }

        return sb.toString();
    }

	public CharacterDTO setCharacterDto(GenerateBookRequest request) {
		
        String charId = request.getSelections().get기().getCharacter().getCode();
        String subId = request.getSelections().get승().getCompanion().getCode();
        String mood = request.getSelections().get기().getMood().getLabel();
        String weakness = request.getSelections().get전().getProblem().getLabel();

        CharacterDTO character = characterService.getCharacterMapperInfo(charId);
        
        CharacterDTO subCharacter = characterService.getSubCharacterMapperInfo(subId);
        character.setSubId(subCharacter.getSubId());
        character.setSubTitle(subCharacter.getSubTitle());
        character.setSubAppearance(subCharacter.getSubAppearance());
        character.setSubNegative(subCharacter.getSubNegative());
        character.setSubUrlImg(subCharacter.getSubUrlImg());
        character.setSubCharacteristic(subCharacter.getSubCharacteristic());

        character.setWeakness(request.getSelections().get전().getProblem().getLabel());
        character.setBackground(request.getSelections().get기().getPlace().getLabel());
        character.setMood(request.getSelections().get기().getMood().getLabel());
        character.setProblem(request.getSelections().get전().getProblem().getLabel());
        character.setSubCharacter(request.getSelections().get승().getCompanion().getLabel());   
        
        log.info("setCharacterDto character:::{}", character);
        
        return character;
		
	}

    public String forceCloseJson(String rawResponse) {
        rawResponse = rawResponse.trim();
        
        // 만약 닫는 괄호가 없으면 강제로 추가
        if (rawResponse.startsWith("{") && !rawResponse.endsWith("}")) {
            // 마지막으로 정상적인 값이 들어있는 위치를 찾음
            int lastQuoteIndex = rawResponse.lastIndexOf("\"");
            if (lastQuoteIndex != -1) {
                // " 뒤에 필드를 강제로 닫아줌
                return rawResponse.substring(0, lastQuoteIndex + 1) + " }";
            }
        }
        return rawResponse;
    }
 
    public String regenerateAndPollImage(RestTemplate restTemplate, String imagePrompt, String mainImage) throws Exception {
        // 1. 워크플로우 JSON 로드 및 프롬프트 치환

       // CharacterDTO character = characterService.getCharacterMapperInfo(charId);
        
        //log.info("item:: {}",item);
    	
    	String image = imgUrl+mainImage;
        String rawPrompt = imagePrompt;
    	
        String workflowJson = new String(loadWorkflow());
        if (rawPrompt == null) {
            log.error("LLM으로부터 image_prompt를 받지 못했습니다. 기본 프롬프트를 사용합니다.");
            rawPrompt = "A cute cat and a bird in Pixar style"; // 폴백 프롬프트
        }
        String modifiedJson = workflowJson.replace("{{user_prompt}}" ,  rawPrompt );
        modifiedJson = modifiedJson.replace("{{user_image}}", image );
        
log.info("modifiedJson::: {}",modifiedJson);
        
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);

        // 2. ComfyUI 규격에 맞게 페이로드 생성
        Map<String, Object> workflow = mapper.readValue(modifiedJson, Map.class);
        Map<String, Object> finalPayload = new HashMap<>();
        finalPayload.put("prompt", workflow);
        String finalJson = mapper.writeValueAsString(finalPayload);

        // 3. ComfyUI /prompt 엔드포인트 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(finalJson, headers);
        //StopWatch stopWatch = new StopWatch("Ollama Response Time Check");
        
        //stopWatch.start("Comfy Prompt Generation"); // 측정 시작
        

        String response = restTemplate.postForObject(properties.getComfy().getBaseUrl()+"/prompt", entity, String.class);

        log.info("generateAndPollImage response::: {}", response);
        //stopWatch.stop(); // 측정 종료


        
        // 4. prompt_id 추출
        Map<String, Object> respMap = objectMapper.readValue(response, Map.class);
        String promptId = (String) respMap.get("prompt_id");
        log.info("generateAndPollImage promptId::: {}", promptId);

        // 5. 이미지 생성이 완료될 때까지 Polling (기존 메서드 활용)
        return rePollForImage(restTemplate, promptId); 
    }
    
    // polling 예시 메서드 (간단 버전)
    public String rePollForImage(RestTemplate restTemplate, String promptId) throws Exception {
        int attempts = 0;
        final int maxAttempts = 60;
        final long sleepMs = 5000;
        ObjectMapper mapper = new ObjectMapper();
        
        log.info("pollForImage promptId::: {}", promptId);
        
        while (attempts < maxAttempts) {
            Thread.sleep(sleepMs);
            String history = restTemplate.getForObject(properties.getComfy().getBaseUrl() + "/history/" + promptId, String.class);
            
            if (history == null || history.trim().equals("{}")) { // 빈 객체 {} 가 오는지 확인
                log.info("Attempt {}: History is empty yet.", attempts + 1);
                attempts++;
                continue;
            }

            JsonNode root = mapper.readTree(history);
            JsonNode promptNode = root.path(promptId);

            if (promptNode.isMissingNode()) {
                log.info("Attempt {}: promptId {} not found in history.", attempts + 1, promptId);
                attempts++;
                continue;
            }

            JsonNode outputs = promptNode.path("outputs");
            if (outputs.isMissingNode() || outputs.size() == 0) {
                log.info("Attempt {}: Task found but no outputs yet.", attempts + 1);
                attempts++;
                continue;
            }
            
           
         // outputs 안의 모든 노드 순회하면서 images 찾기 (보통 Save Image 노드 1개)
            Iterator<Map.Entry<String, JsonNode>> fields = outputs.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode nodeOutput = entry.getValue();
                JsonNode images = nodeOutput.path("images");

                if (images.isArray() && !images.isEmpty()) {
                    // 첫 번째 이미지 (보통 하나만 있음)
                    JsonNode firstImage = images.get(0);
                    String filename = firstImage.path("filename").asText(null);
                    String subfolder = firstImage.path("subfolder").asText("");
                    String type = firstImage.path("type").asText("output");
        	        //log.info("pollForImage filename::: {}", filename);
                    if (filename != null && !filename.isEmpty()) {
                        // ComfyUI /view 엔드포인트로 이미지 URL 구성
                        // 형식: http://127.0.0.1:8188/view?filename=xxx.png&subfolder=&type=output
                        //String viewUrl = "http://172.30.1.38:8188/view" +
        	            String viewUrl = "/view" +
                            "?filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8) +
                            "&subfolder=" + URLEncoder.encode(subfolder, StandardCharsets.UTF_8) +
                            "&type=" + URLEncoder.encode(type, StandardCharsets.UTF_8);

    	    	        log.info("pollForImage viewUrl::: {}", viewUrl);
                        
                        return viewUrl;  // 이 URL을 프론트에 주면 바로 이미지 로드 가능
                    }
                }
            }

            attempts++;
        }
            // history JSON 파싱해서 이미지 URL 추출 (outputs 노드 안 images 배열)
            // 예: outputs → node_id → images[0].filename
            // 실제로는 Jackson으로 파싱해서 ComfyUI/output 폴더 경로 조합
            // 임시: 완성되면 "http://suh.local:8188/view?filename=..." 형식 반환
//            if (history.contains("\"status\": \"success\"")) {
//                return "http://suh.local:8188/output/generated_image.png";  // 실제 구현 필요
//            }
//            attempts++;
//        }
        throw new RuntimeException("Image generation timeout after " + maxAttempts + " attempts");
    }	
	
}
