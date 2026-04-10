package com.mrs.shakes.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrs.shakes.client.OllamaClient;
import com.mrs.shakes.config.ShakesProperties;
import com.mrs.shakes.domain.story.StoryContent;
import com.mrs.shakes.domain.story.StoryMaster;
import com.mrs.shakes.domain.story.StoryPage;
import com.mrs.shakes.domain.story.StoryStatus;
import com.mrs.shakes.dto.BookResponse;
import com.mrs.shakes.dto.CharacterDTO;
import com.mrs.shakes.dto.GenerateBookRequest;
import com.mrs.shakes.dto.PagedStoryResponse;
import com.mrs.shakes.dto.RawStoryResponse;
import com.mrs.shakes.dto.RawStoryResponse.RawPageResponse;
import com.mrs.shakes.dto.RefinedStoryResponse;
import com.mrs.shakes.dto.StoryRequest;
import com.mrs.shakes.dto.GenerateBookRequest.StorySelections;
import com.mrs.shakes.dto.PagedStoryResponse.Page;
import com.mrs.shakes.infrastructure.prompt.PromptProvider;
import com.mrs.shakes.repository.StoryMasterRepository;
import com.mrs.shakes.util.JsonUtils;
import com.mrs.shakes.util.StoryMapper;

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
    private final String imgUrl = "http://myShakes.ddns.net:8080/images/characters/";
	private final RestTemplate restTemplate = new RestTemplate();

    private final ShakesProperties properties; // 생성자 주입
    @Autowired
    private ResourceLoader resourceLoader;
    
    private final StoryMapper storyMapper;

    @JsonProperty("raw_image_keywords")
    private String rawImageKeywords;


    public StoryMaster generateStory(GenerateBookRequest request) {
        // 1. AI 응답 받기
        //String aiResponse = ollamaService.ask(request); 
    	CharacterDTO character = this.setCharacterDto(request);
        String mood = request.getSelections().get기().getMood().getLabel();
        String weakness = request.getSelections().get전().getProblem().getLabel();

    	int pageCount = request.getPageCount();
    	String context = "";
    	StoryMaster master = this.createStoryDraft(request, character, pageCount);
    	
    	createInitialStory(request, master) ;
    	
    	log.info("master::: {}", master); 
        log.info("초안 생성 완료: master_id={}", master.getId()); 
       	
        // 2. 각 페이지별 2차 Refining 진행
        this.refineStoryPages(request, master, context, character );

        return master;
    }    

    
    @Transactional
    public Long createInitialStory(GenerateBookRequest request, StoryMaster master) {
        // 1. StoryMaster 생성
        //StoryMaster master = new StoryMaster();
        //master.setTitle(request.getTitle());
        // ... 기타 설정 ...

        // 2. StoryContent 생성 (화면 선택값 세팅)
        StoryContent content = new StoryContent();
        var selections = request.getSelections();
        
        // 구조에 맞게 mapping (예시)
        content.setCharCode(selections.get기().getCharacter().getCode()); 
        content.setPlaCode(selections.get기().getPlace().getCode());
        content.setModCode(selections.get기().getMood().getCode());
        content.setEveCode(selections.get승().getEvent().getCode()); 
        content.setComCode(selections.get승().getCompanion().getCode()); 
        content.setProCode(selections.get전().getProblem().getCode()); 
        content.setActCode(selections.get전().getTryAction().getCode()); 
        content.setSolCode(selections.get결().getSolution().getCode()); 
        content.setEndCode(selections.get결().getEnding().getCode()); 

        // ... 나머지 eve, com, pro, act, sol, end 코드들 세팅 ...
        
        //content.setSummary(request.getSummary()); // 요약본이 있다면

        // 3. 연관 관계 맺기
        master.setContent(content);

        // 4. 저장 (Cascade 설정으로 master만 저장해도 content가 함께 저장됨)
        return storyMasterRepository.save(master).getId();
    }
    
    @Transactional
    private void getImageUrl(GenerateBookRequest request, StoryMaster master, String context, CharacterDTO character) {
    	
        String mood = request.getSelections().get기().getMood().getLabel();

        // 1. 순서 보장을 위해 페이지 번호순으로 정렬해서 순회 (중요)
        List<StoryPage> pages = master.getPages().stream()
                .sorted(Comparator.comparing(StoryPage::getPageNumber))
                .toList();
    	
	    try {

    	    List<Page> imageUrls = new ArrayList<>();
    	    String workflowJson = new String(loadWorkflow());


           // log.info("workflow::: {}", workflowJson);
            ObjectMapper mapper = new ObjectMapper();

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (StoryPage page : pages) {
            	             	
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // ComfyUI 호출 및 Polling 로직을 이 안으로 이동
                        // (주의: RestTemplate은 Thread-Safe 하므로 공유 가능)
                        String imageUrl = this.generateAndPollImage(restTemplate, page , mood, character ); 
                        page.setImageUrl(imageUrl); // 객체에 직접 세팅
                    } catch (Exception e) {
                        log.error("이미지 생성 실패: ", e);
                    }
                });
                futures.add(future);
            }

         // 모든 페이지 생성이 끝날 때까지 대기 (최대 타임아웃 설정 권장)
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();            
            
          } catch (Exception e) {
            log.error("동화 생성 실패", e);
          }        

	    storyMasterRepository.save(master) ;
        // @Transactional 어노테이션 덕분에 루프 종료 시 변경 감지(Dirty Checking)로 자동 업데이트 됩니다.
    }    
    
    
    @Transactional
    private void refineStoryPages(GenerateBookRequest request, StoryMaster master, String context, CharacterDTO character) {
    	
      	String previousContext = "";
        String mood = request.getSelections().get기().getMood().getLabel();
   	
    	// 1. 순서 보장을 위해 페이지 번호순으로 정렬해서 순회 (중요)
        List<StoryPage> pages = master.getPages().stream()
                .sorted(Comparator.comparing(StoryPage::getPageNumber))
                .toList();
        int row = 0;
        
        for (StoryPage page : pages) {
        	page.setPreviousContext(previousContext);
        	previousContext =  page.getRawContent();
        }
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        //String refinePrompt = "";
        
       for (StoryPage page : pages) {
            // 1. 리파이닝을 위한 프롬프트 구성 (예: 문장 다듬기)
      	    String refinePrompt = promptProvider.getRefinerPrompt(page, page.getPreviousContext(), character, master.getSideCharacterAppearance() );

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // ComfyUI 호출 및 Polling 로직을 이 안으로 이동
                    // (주의: RestTemplate은 Thread-Safe 하므로 공유 가능)
                	// 1. Ollama 정제
                    //String rawResponse = this.forceCloseJson(ollamaClient.generate(refinePrompt));
                    String rawResponse = ollamaClient.generate(refinePrompt);
                    
                 // 빈 값 방어 로직 추가
                    if (rawResponse == null || rawResponse.trim().isEmpty()) {
                        log.error("Page {}: Ollama response is empty", page.getPageNumber());
                        return;
                    }
                    
                    // JSON 변환 수행
                    String closedJson = this.forceCloseJson(rawResponse);
                    RefinedStoryResponse refined = this.parseRefinedResponse(closedJson);
 //                   RefinedStoryResponse refined = this.parseRefinedResponse(rawResponse);

                    log.info("closedJson:::{}", closedJson);
//                    log.info("refined:::{}", refined.toString());
                    log.info("setRefinedContent:::{}", refined.getRefined_content());
                    //log.info("setRefinedContent:::{}", refined.getImage_prompt());
                    
                    // 엔티티에 값 세팅
                    page.setRefinedContent(refined.getRefined_content());
                    page.setRefinedImagePrompt(refined.getImage_prompt()); // DB 필드명에 맞게 조절            
                    page.setImagePrompt(refined.getImage_prompt()); // DB 필드명에 맞게 조절            
                    // 3. 결과 업데이트 (StoryPage 엔티티에 refinedContent 필드가 있다고 가정)
                    // 만약 단순 텍스트만 온다면 그대로 저장, JSON이라면 파싱 로직 추가 필요
                    //page.setRefinedContent(rawResponse);
                    //master.getPages().add(page);
                    String imageUrl = this.generateAndPollImage(restTemplate, page , mood, character ); 
                    page.setImageUrl(imageUrl); // 객체에 직접 세팅
                    
                    log.info("페이지 {}번 정제 완료", page.getPageNumber());
                } catch (Exception e) {
                    log.error("이미지 생성 실패: ", e);
                }
            });
            futures.add(future);
            
            // 2. Ollama 호출
 
        }
    // ⭐ 중요: 모든 비동기 작업이 완료될 때까지 메인 스레드를 대기시킵니다.
       CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
       
       storyMasterRepository.save(master) ;
        // @Transactional 어노테이션 덕분에 루프 종료 시 변경 감지(Dirty Checking)로 자동 업데이트 됩니다.
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
    
    @Transactional
    private StoryMaster createStoryDraft(GenerateBookRequest request, CharacterDTO character, int pageCount) {
        // 1. 프롬프트 빌드 (Resource에서 읽어온 템플릿 사용)
        String userChoices =  formatUserSelections(request.getSelections());
        String systemPrompt = promptProvider.getGeneratorPrompt(character, pageCount);
        String responseJson = ollamaClient.generate(systemPrompt + "\n\n" + userChoices );
        //String responseJson = ollamaClient.generate(systemPrompt  );
        
        try {
            // Ollama 응답에서 실제 JSON 부분만 추출하는 방어 로직 (LLM 특성상 설명 문구가 섞일 수 있음)
            String jsonOnly = responseJson.substring(responseJson.indexOf("{"), responseJson.lastIndexOf("}") + 1);
            
            log.info("jsonOnly::: {}", jsonOnly);
            // JSON -> DTO 변환
            RawStoryResponse rawStory = objectMapper.readValue(jsonOnly, RawStoryResponse.class);
            
            // 4. StoryMaster & StoryPage 생성 및 저장
            StoryMaster master = new StoryMaster();
            master.setTitle(rawStory.getTitle());
            master.setTotalPages(pageCount);
            master.setStatus(StoryStatus.RAW_READY);
            master.setSideCharacterAppearance(rawStory.getSide_character_description());
            
            // characterSetting 스냅샷 저장 로직 추가...
            List<StoryPage> pages = new ArrayList();  
            
            for (RawPageResponse p : rawStory.getPages()) {
                StoryPage page = new StoryPage();
                page.setStory(master);
                page.setPageNumber(p.getPageNumber()); 
                page.setPhase(p.getPhase()); 
                page.setRawContent(p.getContent()); 
                page.setRefinedContent(p.getContent()); 
                page.setRawImageKeywords(p.getRaw_image_keywords());
                page.setImagePrompt(p.getImage_prompt());
                pages.add(page);
            }

            master.setPages(pages);
            
            storyMasterRepository.save(master) ;
            
            return master;
            
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 중 오류가 발생했습니다.", e);
        }
    }
    
    
    private String generateAndPollImage(RestTemplate restTemplate, StoryPage item, String mood, CharacterDTO characterDto) throws Exception {
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
        
      //  log.info("pollForImage promptId::: {}", promptId);
        
        while (attempts < maxAttempts) {  // 5분 타임아웃
            Thread.sleep(sleepMs);  // 5초 대기
            String history = restTemplate.getForObject(properties.getComfy().getBaseUrl()+"/history/" + promptId, String.class);
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
//            if (history.contains("\"status\": \"success\"")) {
//                return "http://suh.local:8188/output/generated_image.png";  // 실제 구현 필요
//            }
//            attempts++;
//        }
        throw new RuntimeException("Image generation timeout after " + maxAttempts + " attempts");
    }

	public String generateAndPollImage2222(RestTemplate restTemplate, PagedStoryResponse.Page item, String mood, CharacterDTO characterDto) throws Exception {
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
		String story22 =JsonUtils.escapeJsonValue(item.getText());
		
	    String workflowJson = new String(Files.readAllBytes(Paths.get(properties.getComfy().getWorkflowPath())));
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
        
	    stopWatch.start("Ollama Prompt Generation"); // 측정 시작
	    
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
	public String pollForImage2222(RestTemplate restTemplate, String promptId) throws Exception {
	    int attempts = 0;
	    final int maxAttempts = 60;
	    final long sleepMs = 50000;
	    ObjectMapper mapper = new ObjectMapper();
	    
      //  log.info("pollForImage promptId::: {}", promptId);
	    
	    while (attempts < maxAttempts) {  // 5분 타임아웃
	        Thread.sleep(sleepMs);  // 5초 대기
	        String history = restTemplate.getForObject(properties.getComfy().getBaseUrl()+"/history/" + promptId, String.class);
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
        String weakness = request.getSelections().get전().getProblem().getLabel();

        CharacterDTO character = characterService.getCharacterMapperInfo(charId);
        
        CharacterDTO subCharacter = characterService.getSubCharacterMapperInfo(subId);
        character.setSubId(subCharacter.getSubId());
        character.setSubTitle(subCharacter.getSubTitle());
        character.setSubAppearance(subCharacter.getSubAppearance());
        character.setSubNegative(subCharacter.getSubNegative());
        character.setSubUrlImg(subCharacter.getSubUrlImg());

        character.setWeakness(request.getSelections().get전().getProblem().getLabel());
        character.setBackground(request.getSelections().get기().getPlace().getLabel());
        character.setMood(request.getSelections().get기().getMood().getLabel());
        character.setProblem(request.getSelections().get전().getProblem().getLabel());
        character.setSubCharacter(request.getSelections().get승().getCompanion().getLabel());   
        
        log.info("setCharacterDto character:::{}", character);
        
        return character;
		
	}

    private String formatUserSelections(StorySelections selections) {
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
	
	
    // 페이지 번호에 따라 기승전결(Phase)을 판단하는 간단한 로직
    private String getPhase(int currentPage, int totalPages) {
        float progress = (float) currentPage / totalPages;
        if (progress <= 0.25) return "기 (도입, 배경 및 캐릭터 소개)";
        if (progress <= 0.50) return "승 (전개, 사건의 시작)";
        if (progress <= 0.75) return "전 (위기 및 갈등 심화)";
        return "결 (결말 및 교훈)";
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

	public byte[] loadWorkflow() throws IOException {
	    // classpath:를 사용하여 resources 폴더 내의 파일을 찾습니다.
	    Resource resource = resourceLoader.getResource(properties.getComfy().getWorkflowPath());
	    return Files.readAllBytes(Paths.get(resource.getURI()));
	}
    
    
}
