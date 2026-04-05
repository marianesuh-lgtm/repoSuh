package com.mrs.shakes.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.mrs.shakes.dto.BookRequest;
import com.mrs.shakes.dto.CharacterDTO;
import com.mrs.shakes.dto.GenerateBookRequest;
import com.mrs.shakes.dto.PagedStoryResponse;
import com.mrs.shakes.dto.StoryRequest;
import com.mrs.shakes.dto.PagedStoryResponse.Page;
import com.mrs.shakes.service.CharacterService;
import com.mrs.shakes.service.OllamaTestService;
import com.mrs.shakes.service.StoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173")
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

//	public BookController(ChatClient.Builder builder ) {
//        this.chatClient = builder.build();
//    }


	@PostMapping("/generate-book")
	public void generateStory(@RequestBody GenerateBookRequest request) {
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
        storyService.generateStory(request, character);
	    // 여기서 LLM 호출 로직 실행
//	    try {
               //result = ollamaTestService.generatePagedStory(request, character);
//
//             log.info("result:: {}", result);
//             log.info("result.getPages():: {}", result.getPages());
//    	    List<Page> imageUrls = new ArrayList<>();
//    	    String workflowJson = new String(Files.readAllBytes(Paths.get("src/main/resources/workflows/florenceWF.json")));
//
//
//           // log.info("workflow::: {}", workflowJson);
//            ObjectMapper mapper = new ObjectMapper();
//
//            List<CompletableFuture<Void>> futures = new ArrayList<>();
//            //String charId = request.getSelections().get기().getCharacter().getCode();
//
//            for ( PagedStoryResponse.Page  item : result.getPages()) {
//            	
//                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                    try {
//                        // ComfyUI 호출 및 Polling 로직을 이 안으로 이동
//                        // (주의: RestTemplate은 Thread-Safe 하므로 공유 가능)
//                        String imageUrl = storyService.generateAndPollImage(restTemplate, item , mood, character ); 
//                         item.setImageUrl(imageUrl); // 객체에 직접 세팅
//                    } catch (Exception e) {
//                        log.error("이미지 생성 실패: ", e);
//                    }
//                });
//                futures.add(future);
//            }
//
//         // 모든 페이지 생성이 끝날 때까지 대기 (최대 타임아웃 설정 권장)
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();            
            
//        } catch (Exception e) {
//            log.error("동화 생성 실패", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(PagedStoryResponse.builder()
//                            .title("오류 발생")
//                            .pages(List.of())
//                            .build());
//        }
        //return ResponseEntity.ok(result);
   	}
	
	@PostMapping("/generate-book2222")
	public ResponseEntity<?> generateStory22222(@RequestBody GenerateBookRequest request) {
	    log.info("동화 생성 요청 수신: {}", request);
	    PagedStoryResponse result  ;

	    // 유효성 검사 (옵션)
	    if (request.getSelections() == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }
//        String charId = request.getSelections().get기().getCharacter().getCode();
        String mood = request.getSelections().get기().getMood().getLabel();

        //CharacterDTO character = characterService.getCharacterMapperInfo(charId);
        CharacterDTO character = storyService.setCharacterDto(request);
        storyService.generateStory(request, character);
	    // 여기서 LLM 호출 로직 실행
//	    try {
             result = ollamaTestService.generatePagedStory(request, character);
//
//             log.info("result:: {}", result);
//             log.info("result.getPages():: {}", result.getPages());
//    	    List<Page> imageUrls = new ArrayList<>();
//    	    String workflowJson = new String(Files.readAllBytes(Paths.get("src/main/resources/workflows/florenceWF.json")));
//
//
//           // log.info("workflow::: {}", workflowJson);
//            ObjectMapper mapper = new ObjectMapper();
//
//            List<CompletableFuture<Void>> futures = new ArrayList<>();
//            //String charId = request.getSelections().get기().getCharacter().getCode();
//
//            for ( PagedStoryResponse.Page  item : result.getPages()) {
//            	
//                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                    try {
//                        // ComfyUI 호출 및 Polling 로직을 이 안으로 이동
//                        // (주의: RestTemplate은 Thread-Safe 하므로 공유 가능)
//                        String imageUrl = storyService.generateAndPollImage(restTemplate, item , mood, character ); 
//                         item.setImageUrl(imageUrl); // 객체에 직접 세팅
//                    } catch (Exception e) {
//                        log.error("이미지 생성 실패: ", e);
//                    }
//                });
//                futures.add(future);
//            }
//
//         // 모든 페이지 생성이 끝날 때까지 대기 (최대 타임아웃 설정 권장)
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();            
            
//        } catch (Exception e) {
//            log.error("동화 생성 실패", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(PagedStoryResponse.builder()
//                            .title("오류 발생")
//                            .pages(List.of())
//                            .build());
//        }
        return ResponseEntity.ok(result);
   	}

	
	@PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateStory(@RequestBody StoryRequest request) {
        // 1. 프론트에서 받은 데이터를 바탕으로 프롬프트 조립
        String prompt = String.format(
            "Children's storybook illustration of a 5-year-old girl named %s, " +
            "wearing %s, in %s. Her face has a sweet, innocent look of wonder. " +
            "Warm colored pencil texture, soft whimsical lines, cozy pastels.",
            request.getName(), request.getDress(), request.getPlace()
        );

        // 2. AI 모델 호출 (예: Ollama 또는 ComfyUI 연동)
        // 여기서는 예시로 성공 메시지와 조립된 프롬프트만 반환합니다.
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("finalPrompt", prompt);
        
        return ResponseEntity.ok(response);
    }
	
    @PostMapping("/generate-story")
    public ResponseEntity<?> generateStory(@RequestBody BookRequest request) {
        String prompt = """
        	You are a professional English prompt writer for image generation.	
        	사용자가 한국어로 설명해도 당신은 반드시 다음 규칙을 철저히 지켜야 해:
            모든 이야기의 주인공은 반드시 '%s'라는 이름의 %d살 아이야.
             항상 '%s'라고 이름으로 부르고 %d살 나이에 맞게 테마를 만들어줘.
            이야기 전체에서 '%s'를 주인공 이름으로 사용해.

            좋아하는 것: %s
            테마: %s
            
            8페이지 분량의 귀여운 한국어 동화 스토리를 작성해줘.
            형식: 페이지1: [짧은 문장 1~2개] [영어로 된 간단한 이미지 설명 프롬프트]
            페이지2: ...
            """.formatted(request.getName(), request.getAge(), request.getName(), request.getAge(), request.getName(),  request.getLikes(), request.getTheme());

        String story = chatClient.prompt(prompt).call().content();

        // story를 파싱해서 List<Page>로 만들기 (페이지 번호, 텍스트, imagePrompt)
        // 간단하게는 그냥 String 전체 반환해도 OK (Vue에서 파싱)
        return ResponseEntity.ok(story);
    }

    @PostMapping("/generate-image")
    public ResponseEntity<?> generateImage(@RequestBody Map<String, String> request) throws IOException {
        String userPrompt = request.get("prompt");  // Vue에서 보낸 프롬프트

        log.info("userPrompt::: {}", userPrompt);
        //log.error("프롬프트가 null입니다! Map의 Key를 확인하세요.");
        if (userPrompt == null) {
            log.error("프롬프트가 null입니다! Map의 Key를 확인하세요.");
            userPrompt = "default prompt"; // 임시 방편
        }        
        // 1. 저장한 API JSON 파일 로드
        String workflowJsonStr = new String(Files.readAllBytes(
            Paths.get("src/main/resources/workflows/florenceWF.json")
        ));

        // 2. 프롬프트 동적 치환 (간단 replace 예시)
        String modifiedJson = workflowJsonStr.replace("{{user_prompt}}", userPrompt);
        log.info("modifiedJson::: {}", modifiedJson);
        
        // 또는 Jackson으로 파싱해서 정확한 노드 수정 (추천)
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> workflow = mapper.readValue(modifiedJson, Map.class);
        // 예: 프롬프트 노드 ID가 "3"이라고 가정
        Map<String, Object> promptNode = (Map<String, Object>) ((Map<String, Object>) workflow.get("6")).get("inputs");
        promptNode.put("text", userPrompt + ", children's book style, cute illustration");
        log.info("promptNode::: {}", promptNode);
        log.info("workflow::: {}", workflow);

     // 1. ComfyUI 규격에 맞게 "prompt" 키로 감싸기
        Map<String, Object> finalPayload = new HashMap<>();
        finalPayload.put("prompt", workflow); // 여기서 workflow는 가공된 노드 Map
        
        String finalJson = mapper.writeValueAsString(finalPayload);
        log.info("finalJson::: {}", finalJson);

        // 3. ComfyUI /prompt 엔드포인트로 POST
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(finalJson, headers);
        String response = restTemplate.postForObject(
            "http://suh.local:8188/prompt",
            entity,
            String.class
        );

        // response: {"prompt_id": "xxx-xxx-xxx"}
        // 이후 polling (/history/{prompt_id})으로 결과 기다림 (별도 구현)

        return ResponseEntity.ok(response);
    }  
    
    

}
