package com.mrs.shakes.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrs.shakes.client.OllamaClient;
import com.mrs.shakes.domain.story.StoryMaster;
import com.mrs.shakes.domain.story.StoryPage;
import com.mrs.shakes.domain.story.StoryStatus;
import com.mrs.shakes.dto.BookResponse;
import com.mrs.shakes.dto.CharacterDTO;
import com.mrs.shakes.dto.GenerateBookRequest;
import com.mrs.shakes.dto.PagedStoryResponse;
import com.mrs.shakes.dto.RawStoryResponse;
import com.mrs.shakes.dto.RawStoryResponse.RawPageResponse;
import com.mrs.shakes.dto.StoryRequest;
import com.mrs.shakes.infrastructure.prompt.PromptProvider;
import com.mrs.shakes.repository.StoryMasterRepository;
import com.mrs.shakes.util.JsonUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j

public class StoryService {

    private final StoryMasterRepository storyMasterRepository;
    private final OllamaClient ollamaClient; // Ollama와 통신하는 커스텀 클라이언트
    private final PromptProvider promptProvider; // .st 파일을 읽어주는 컴포넌트
    private final ObjectMapper objectMapper;
    private final CharacterService characterService ;
    private final IncrementalStoryService refineService;
    
    @JsonProperty("raw_image_keywords")
    private String rawImageKeywords;


    public void generateStory(GenerateBookRequest request, CharacterDTO character) {
        // 1. AI 응답 받기
        //String aiResponse = ollamaService.ask(request); 
    	int pageCount = 6;
        // 2. 초안 생성 (데이터 가공 및 엔티티 매핑)
    	String systemPrompt = promptProvider.getGeneratorPrompt(character, pageCount);
        
        log.info("systemPrompt::: {}", systemPrompt); 

        String responseJson = ollamaClient.generate(systemPrompt);

        log.info("responseJson::: {}", responseJson); 
        
        //Book storyDraft = 
    	//Long id = this.createStoryDraft(character, pageCount);
        
        // 3. DB 저장
       // Book savedBook = bookRepository.save(storyDraft);
        
        //return BookResponse.from(savedBook);
    }    
    
    @Transactional
    private Long createStoryDraft(CharacterDTO character, int pageCount) {
        // 1. 프롬프트 빌드 (Resource에서 읽어온 템플릿 사용)
        String systemPrompt = promptProvider.getGeneratorPrompt(character, pageCount);
        String responseJson = ollamaClient.generate(systemPrompt);
        
        try {
            // Ollama 응답에서 실제 JSON 부분만 추출하는 방어 로직 (LLM 특성상 설명 문구가 섞일 수 있음)
            String jsonOnly = responseJson.substring(responseJson.indexOf("{"), responseJson.lastIndexOf("}") + 1);
            
            // JSON -> DTO 변환
            RawStoryResponse rawStory = objectMapper.readValue(jsonOnly, RawStoryResponse.class);
            
            // 4. StoryMaster & StoryPage 생성 및 저장
            StoryMaster master = new StoryMaster();
            master.setTitle(rawStory.getTitle());
            master.setTotalPages(pageCount);
            master.setStatus(StoryStatus.RAW_READY);
            // characterSetting 스냅샷 저장 로직 추가...

            for (RawPageResponse p : rawStory.getPages()) {
                StoryPage page = new StoryPage();
                page.setStory(master);
                page.setPageNumber(p.getPageNumber());
                page.setPhase(p.getPhase());
                page.setRawContent(p.getContent());
                //page.setRawImageKeywords(p.getRawImageKeywords());
                master.getPages().add(page);
            }

            return storyMasterRepository.save(master).getId();
            
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 중 오류가 발생했습니다.", e);
        }
    }

	public String generateAndPollImage(RestTemplate restTemplate, PagedStoryResponse.Page item, String mood, CharacterDTO characterDto) throws Exception {
	    // 1. 워크플로우 JSON 로드 및 프롬프트 치환

       // CharacterDTO character = characterService.getCharacterMapperInfo(charId);
        
        //log.info("item:: {}",item);
		
		String charater = JsonUtils.escapeJsonValue(characterDto.getAppearance());
		//String mood = JsonUtils.escapeJsonValue(character.getPersonalityTraits());
		String background = "";
		String style =JsonUtils.escapeJsonValue(characterDto.getArtStyle());
		String action ="";
		String negative =JsonUtils.escapeJsonValue(characterDto.getNegative());
		String image = "http://myShakes.ddns.net:8080/images/characters/"+characterDto.getUrlImg();
		String subImage = "http://myShakes.ddns.net:8080/images/characters/"+characterDto.getSubUrlImg();
		String story22 =JsonUtils.escapeJsonValue(item.getText());
		
	    String workflowJson = new String(Files.readAllBytes(Paths.get("src/main/resources/workflows/florenceWF.json")));
	    String modifiedJson = workflowJson.replace("{{user_prompt}}"
	    		       ,  item.getAction()   );
//	    String modifiedJson = workflowJson.replace("{{user_prompt}}"
//	    		 , charater+", "+ JsonUtils.escapeJsonValue(item.getMood())+","+ JsonUtils.escapeJsonValue(item.getBackground())+","+ JsonUtils.escapeJsonValue(item.getAction()) );
	    //modifiedJson = modifiedJson.replace("{{user_negative}}", characterDto.getSubNegative()+", "+negative +", grandfather, old man, male, masculine, beard, mustache , extra legs, extra paws, mutated limbs, fused legs, extra arms, extra hands, fused fingers, malformed limbs" );
	    //modifiedJson = modifiedJson.replace("{{user_character}}", charater );
	    modifiedJson = modifiedJson.replace("{{user_mood}}", item.getMood() );
	    modifiedJson = modifiedJson.replace("{{user_background}}", item.getBackground() );
//	    modifiedJson = modifiedJson.replace("{{user_style}}", style );
	    modifiedJson = modifiedJson.replace("{{user_image}}", image );
	    //modifiedJson = modifiedJson.replace("{{user_subImage}}", subImage );

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        
        //log.info("prompt::: {}", promptUsed +","+story+ ", children's book illustration style, cute, vibrant colors, soft lighting");
        log.info("generateAndPollImage modifiedJson::: {}", modifiedJson);

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
        
	    stopWatch.start("Ollama Prompt Generation"); // 측정 시작
	    
        //log.info("entity::: {}", entity);
	    
	    // restTemplate은 Bean으로 등록된 것을 사용 (Thread-safe)
	    String response = restTemplate.postForObject("http://suh.local:8188/prompt", entity, String.class);

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
	    
      //  log.info("pollForImage promptId::: {}", promptId);
	    
	    while (attempts < maxAttempts) {  // 5분 타임아웃
	        Thread.sleep(sleepMs);  // 5초 대기
	        String history = restTemplate.getForObject("http://suh.local:8188/history/" + promptId, String.class);
	        //log.info("pollForImage history::: {}", history);
	        
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

		    	       // log.info("pollForImage viewUrl::: {}", viewUrl);
	                    
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
//	        if (history.contains("\"status\": \"success\"")) {
//	            return "http://suh.local:8188/output/generated_image.png";  // 실제 구현 필요
//	        }
//	        attempts++;
//	    }
	    throw new RuntimeException("Image generation timeout after " + maxAttempts + " attempts");
	}	

	public CharacterDTO setCharacterDto(GenerateBookRequest request) {
		
        String charId = request.getSelections().get기().getCharacter().getCode();
        String subId = request.getSelections().get승().getCompanion().getCode();
        String mood = request.getSelections().get기().getMood().getLabel();

        CharacterDTO character = characterService.getCharacterMapperInfo(charId);
        
        CharacterDTO subCharacter = characterService.getSubCharacterMapperInfo(subId);
        character.setSubId(subCharacter.getSubId());
        character.setSubTitle(subCharacter.getSubTitle());
        character.setSubAppearance(subCharacter.getSubAppearance());
        character.setSubNegative(subCharacter.getSubNegative());
        character.setSubUrlImg(subCharacter.getSubUrlImg());

        character.setBackground(request.getSelections().get기().getPlace().getLabel());
        character.setMood(request.getSelections().get기().getMood().getLabel());
        character.setProblem(request.getSelections().get전().getProblem().getLabel());
        character.setSubCharacter(request.getSelections().get승().getCompanion().getLabel());   
        
        log.info("setCharacterDto character:::{}", character);
        
        return character;
		
	}

	

}
