package com.mrs.shakes.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.mrs.shakes.config.ShakesProperties;
import com.mrs.shakes.domain.story.StoryMaster;
import com.mrs.shakes.domain.story.StoryPage;
import com.mrs.shakes.dto.BookRequest;
import com.mrs.shakes.dto.CharacterDTO;
import com.mrs.shakes.dto.GenerateBookRequest;
import com.mrs.shakes.dto.PagedStoryResponse;
import com.mrs.shakes.dto.StoryRequest;
import com.mrs.shakes.dto.PagedStoryResponse.Page;
import com.mrs.shakes.service.CharacterService;
import com.mrs.shakes.service.OllamaTestService;
import com.mrs.shakes.service.StoryService;
import com.mrs.shakes.util.JsonUtils;
import com.mrs.shakes.util.StoryMapper;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://myShakes.ddns.net:5173")
@Slf4j
@RequiredArgsConstructor
//@NoArgsConstructor  

public class BookController {

	private final ChatClient chatClient;  // Spring AI에서 주입
	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();
    private final CharacterService characterService ;
	private final OllamaTestService ollamaTestService  ; 
	private final StoryService  storyService ;
    private final ShakesProperties properties; // 생성자 주입
    private final String imgUrl = "http://myShakes.ddns.net:8080/images/characters/";
    @Autowired
    private ResourceLoader resourceLoader;
    
    private final StoryMapper storyMapper;
    
//	public BookController(ChatClient.Builder builder ) {
//        this.chatClient = builder.build();
//    }


	@PostMapping("/generate-book")
	public ResponseEntity<?>  generateStory(@RequestBody GenerateBookRequest request) {
	    log.info("동화 생성 요청 수신: {}", request);
	    PagedStoryResponse result  ;

	    // 유효성 검사 (옵션)
	    if (request.getSelections() == null) {
	        //return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }
//        String charId = request.getSelections().get기().getCharacter().getCode();
        String mood = request.getSelections().get기().getMood().getLabel();
        String weakness = request.getSelections().get전().getProblem().getLabel();

        //CharacterDTO character = characterService.getCharacterMapperInfo(charId);
        CharacterDTO character = storyService.setCharacterDto(request);
        StoryMaster master = storyService.generateStory(request, character);
        
        PagedStoryResponse rslt = storyMapper.toResponse(master);
        
        
        
	    // 여기서 LLM 호출 로직 실행
	    try {

            
	        log.info("rslt:: {}", rslt);
             log.info("result.getPages():: {}", rslt.getPages());
             
             log.info("master::: {}", rslt.getTotalPages());
             log.info("master::: {}", rslt.getTitle());
              
//             List<PagedStoryResponse.Page> pageList = new ArrayList<>(rslt.getPages());
             
             for (PagedStoryResponse.Page page : rslt.getPages()) {
                 log.info("getImagePrompt::: {}", page.getImagePrompt() );
//                 log.info("getPageNumber::: {}", page.getPageNumber());
//                 log.info("getRefinedContent::: {}", page.getRefinedContent());
//                 //rslt.getPages().add(storyMapper.toPageResponse(page));
//                 pageList.add(storyMapper.toPageResponse(page));
             }
//             rslt.setPages(pageList) ;
             
    	    List<Page> imageUrls = new ArrayList<>();
    	    String workflowJson = new String(loadWorkflow());


           // log.info("workflow::: {}", workflowJson);
            ObjectMapper mapper = new ObjectMapper();

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            //String charId = request.getSelections().get기().getCharacter().getCode();

            for (PagedStoryResponse.Page page : rslt.getPages()) {
            	             	
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PagedStoryResponse.builder()
                            .title("오류 발생")
                            .pages(List.of())
                            .build());
        }
        return ResponseEntity.ok(rslt);
   	}
	

	
	public byte[] loadWorkflow() throws IOException {
	    // classpath:를 사용하여 resources 폴더 내의 파일을 찾습니다.
	    Resource resource = resourceLoader.getResource(properties.getComfy().getWorkflowPath());
	    return Files.readAllBytes(Paths.get(resource.getURI()));
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
	String image = imgUrl+characterDto.getUrlImg();
	String subImage = imgUrl+characterDto.getSubUrlImg();
	//String story22 =JsonUtils.escapeJsonValue(item.getText());
	
    String workflowJson = new String(loadWorkflow());
    String modifiedJson = workflowJson.replace("{{user_prompt}}" ,  item.getImagePrompt()  );
//    String modifiedJson = workflowJson.replace("{{user_prompt}}"
//    		 , charater+", "+ JsonUtils.escapeJsonValue(item.getMood())+","+ JsonUtils.escapeJsonValue(item.getBackground())+","+ JsonUtils.escapeJsonValue(item.getAction()) );
    //modifiedJson = modifiedJson.replace("{{user_negative}}", characterDto.getSubNegative()+", "+negative +", grandfather, old man, male, masculine, beard, mustache , extra legs, extra paws, mutated limbs, fused legs, extra arms, extra hands, fused fingers, malformed limbs" );
    //modifiedJson = modifiedJson.replace("{{user_character}}", charater );
//    modifiedJson = modifiedJson.replace("{{user_mood}}", item.getMood() );
//    modifiedJson = modifiedJson.replace("{{user_background}}", item.getBackground() );
//    modifiedJson = modifiedJson.replace("{{user_style}}", style );
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
//        if (history.contains("\"status\": \"success\"")) {
//            return "http://suh.local:8188/output/generated_image.png";  // 실제 구현 필요
//        }
//        attempts++;
//    }
    throw new RuntimeException("Image generation timeout after " + maxAttempts + " attempts");
}
	
   
    
    

}
