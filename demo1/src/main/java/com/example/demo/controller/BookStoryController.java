package com.example.demo.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse;
import org.springframework.http.*;
import org.springframework.util.StopWatch;

import com.example.demo.client.OllamaClient;
import com.example.demo.domain.character.CharacterPrompt;
import com.example.demo.domain.character.CharacterRepository;
import com.example.demo.dto.BookRequest;
import com.example.demo.dto.BookResponse;
import com.example.demo.dto.BookResponse.PageItem;
import com.example.demo.dto.CharacterDTO;
import com.example.demo.dto.GenerateBookRequest;
import com.example.demo.dto.PagedStoryResponse;
import com.example.demo.dto.PagedStoryResponse.Page;
import com.example.demo.service.CharacterService;
import com.example.demo.service.OllamaService;
import com.example.demo.service.OllamaTestService;
import com.example.demo.util.JsonUtils;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", })
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
//@RequiredArgsConstructor
//@NoArgsConstructor  
public class BookStoryController {


	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ChatClient chatClient ;
	private final OllamaService ollamaService  ;  
	private final OllamaTestService ollamaTestService  ;  
    private final CharacterService characterService ;

	public BookStoryController(ChatClient.Builder builder, OllamaService ollamaService,OllamaTestService ollamaTestService, CharacterService characterService ) {
        this.chatClient = builder.build();
        this.ollamaService = ollamaService;
        this.ollamaTestService = ollamaTestService;
        this.characterService = characterService;
   }
    
 
	
	@PostMapping("/generate-book-BACK")
	public ResponseEntity<?> generateBook(@RequestBody GenerateBookRequest request) {
	    log.info("동화 생성 요청 수신: {}", request);
	    PagedStoryResponse result  ;

	    // 유효성 검사 (옵션)
	    if (request.getSelections() == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }

        String charId = request.getSelections().get기().getCharacter().getCode();
        String mood = request.getSelections().get기().getMood().getLabel();

        CharacterDTO character = characterService.getCharacterMapperInfo(charId);
        
	    // 여기서 LLM 호출 로직 실행
	    try {
             result = ollamaService.generatePagedStory(request);

             log.info("result:: {}", result);
    	    List<Page> imageUrls = new ArrayList<>();
    	    String workflowJson = new String(Files.readAllBytes(Paths.get("src/main/resources/workflows/WF0310.json")));


            //log.info("workflow::: {}", workflowJson);
            ObjectMapper mapper = new ObjectMapper();

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for ( PagedStoryResponse.Page  item : result.getPages()) {
            	
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // ComfyUI 호출 및 Polling 로직을 이 안으로 이동
                        // (주의: RestTemplate은 Thread-Safe 하므로 공유 가능)
                        String imageUrl = generateAndPollImage(item, mood, character); 
                         item.setImageUrl(imageUrl); // 객체에 직접 세팅
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
        return ResponseEntity.ok(result);
   	}

	
	@PostMapping("/generate-book")
	public ResponseEntity<?> generateStory(@RequestBody GenerateBookRequest request) {
	    log.info("동화 생성 요청 수신: {}", request);
	    PagedStoryResponse result  ;

	    // 유효성 검사 (옵션)
	    if (request.getSelections() == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }
        String charId = request.getSelections().get기().getCharacter().getCode();
        String mood = request.getSelections().get기().getMood().getLabel();

        CharacterDTO character = characterService.getCharacterMapperInfo(charId);

	    // 여기서 LLM 호출 로직 실행
	    try {
             result = ollamaTestService.generatePagedStory(request);

             log.info("result:: {}", result);
             log.info("result.getPages():: {}", result.getPages());
    	    List<Page> imageUrls = new ArrayList<>();
    	    String workflowJson = new String(Files.readAllBytes(Paths.get("src/main/resources/workflows/WF0310.json")));


           // log.info("workflow::: {}", workflowJson);
            ObjectMapper mapper = new ObjectMapper();

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            //String charId = request.getSelections().get기().getCharacter().getCode();

            for ( PagedStoryResponse.Page  item : result.getPages()) {
            	
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // ComfyUI 호출 및 Polling 로직을 이 안으로 이동
                        // (주의: RestTemplate은 Thread-Safe 하므로 공유 가능)
                        String imageUrl = generateAndPollImage(item , mood, character ); 
                         item.setImageUrl(imageUrl); // 객체에 직접 세팅
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
        return ResponseEntity.ok(result);
   	}
	
	@PostMapping("/generate-book2222")
	public ResponseEntity<?> generateBook2(@RequestBody GenerateBookRequest request) {
	    log.info("동화 생성 요청 수신: {}", request);
	    PagedStoryResponse result  ;

	    // 유효성 검사 (옵션)
	    if (request.getSelections() == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }

        String charId = request.getSelections().get기().getCharacter().getCode();
        String mood = request.getSelections().get기().getMood().getLabel();

        CharacterDTO character = characterService.getCharacterMapperInfo(charId);
	    
	    // 여기서 LLM 호출 로직 실행
	    try {
             result = ollamaService.generatePagedStory(request);

    	    List<Page> imageUrls = new ArrayList<>();
    	    String workflowJson = new String(Files.readAllBytes(Paths.get("src/main/resources/workflows/WF0310.json")));


            log.info("workflow::: {}", workflowJson);
            ObjectMapper mapper = new ObjectMapper();

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            //String charId = request.getSelections().get기().getCharacter().getCode();

            for ( PagedStoryResponse.Page  item : result.getPages()) {
            	
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // ComfyUI 호출 및 Polling 로직을 이 안으로 이동
                        // (주의: RestTemplate은 Thread-Safe 하므로 공유 가능)
                        String imageUrl = generateAndPollImage(item, mood, character ); 
                         item.setImageUrl(imageUrl); // 객체에 직접 세팅
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
        return ResponseEntity.ok(result);
   	}
	
	
	
	private String generateAndPollImage(PagedStoryResponse.Page item, String mood, CharacterDTO character) throws Exception {
	    // 1. 워크플로우 JSON 로드 및 프롬프트 치환

       // CharacterDTO character = characterService.getCharacterMapperInfo(charId);
        
        //log.info("item:: {}",item);
		
		String charater = JsonUtils.escapeJsonValue(character.getAppearance());
		//String mood = JsonUtils.escapeJsonValue(character.getPersonalityTraits());
		String background = "";
		String style =JsonUtils.escapeJsonValue(character.getArtStyle());
		String action ="";
		String negative =JsonUtils.escapeJsonValue(character.getNegative());
		String image = "http://172.30.1.99:8080/images/characters/"+character.getUrlImg();
		String story22 =JsonUtils.escapeJsonValue(item.getText());
		
	    String workflowJson = new String(Files.readAllBytes(Paths.get("src/main/resources/workflows/WF0310.json")));
	    String modifiedJson = workflowJson.replace("{{user_prompt}}"
	    		       , item.getImagePrompt() + "," + item.getAction() + "," + item.getBackground() );
//	    String modifiedJson = workflowJson.replace("{{user_prompt}}"
//	    		 , charater+", "+ JsonUtils.escapeJsonValue(item.getMood())+","+ JsonUtils.escapeJsonValue(item.getBackground())+","+ JsonUtils.escapeJsonValue(item.getAction()) );
	    modifiedJson = modifiedJson.replace("{{user_negative}}", negative +", extra arms, extra hands, fused fingers, malformed limbs" );
//	    modifiedJson = modifiedJson.replace("{{user_character}}", charater );
//	    modifiedJson = modifiedJson.replace("{{user_mood}}", JsonUtils.escapeJsonValue(item.getMood()) );
//	    modifiedJson = modifiedJson.replace("{{user_background}}", JsonUtils.escapeJsonValue(item.getBackground()) );
//	    modifiedJson = modifiedJson.replace("{{user_style}}", style );
	    modifiedJson = modifiedJson.replace("{{user_image}}", image );

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
	    return pollForImage(promptId); 
	}
	
	// polling 예시 메서드 (간단 버전)
	private String pollForImage(String promptId) throws Exception {
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
	
	// 실제 스토리 생성 메서드
	private String generateStoryWithOllama(BookRequest request) {
	    // 프롬프트 템플릿 (한글로 자연스럽게 작성)
//		String prompt = """
//	        	You are a professional English prompt writer for image generation.	
//	        	사용자가 한국어로 설명해도 당신은 반드시 다음 규칙을 철저히 지켜야 해:
//	            모든 이야기의 주인공은 반드시 '%s'라는 이름의 %d살 아이야.
//	             항상 '%s'라고 이름으로 부르고 %d살 나이에 맞게 테마를 만들어줘.
//	            이야기 전체에서 '%s'를 주인공 이름으로 사용해.
//
//	            좋아하는 것: %s
//	            테마: %s
//	            
//	            8페이지 분량의 귀여운 한국어 동화 스토리를 작성해줘.
//	            형식: 페이지1: [짧은 문장 1~2개] [영어로 된 간단한 이미지 설명 프롬프트]
//	            페이지2: ...
//	            """.formatted(request.getName(), request.getAge(), request.getName(), request.getAge(), request.getName(),  request.getLikes(), request.getTheme());
	    String template22 = """
	      너는 이제부터 동화 작가이자 AI 이미지 프롬프트 전문가야. 내가 주제를 주면 아래 형식에 맞춰서 답변해줘. '스토리'는 아이들이 읽기 좋은 따뜻한 한국어로 쓰고, '프롬프트'는 Flux 모델이 동화책 수채화 스타일로 그릴 수 있도록 상세한 영어 묘사로 작성해줘.
          주제: 마법의 신데렐라 공주가 되는 꿈을 꾸는 소녀 은서와 요정 친구들
 
           	사용자가 한국어로 설명해도 당신은 반드시 다음 규칙을 철저히 지켜야 해:
	    	절대 더블 쿼테이션(" ")으로 텍스트를 감싸지 마.
	    	모든 이야기의 주인공은 반드시 {name} 라는 이름의 {age}살 아이야.
	   	    항상 {name} 라고 이름으로 부르고 {age}살 나이에 맞게 테마를 만들어줘.
	    	이야기 전체에서 {name} 를 주인공 이름으로 사용해.
	    	 좋아하는 것: {likes}
	    	 테마:{theme}
	        8페이지 분량의 귀여운 한국어 동화 스토리를 3~8세 아이들을 위해 작성해줘.
	        아래 조건으로 8페이지 분량의 짧고 재미있는 동화를 써줘.
	        각 페이지는 1~3문장 정도로 간결하게.
	        형식은 정확히 아래처럼 지켜줘:

	        형식: 페이지1: [짧은 문장 1~2개] [영어로 된 간단한 이미지 설명 프롬프트]
	        페이지2: ...
	        ... (총 8페이지까지)

	        조건:
	        - 아이 이름: {name} ({age}살)
	        - 좋아하는 것: {likes}
	        - 테마: {theme}
	        - 전체 분위기: 따뜻하고 긍정적이며, 교훈이 살짝 들어가도록
	        - 한국어로 자연스럽게 작성
	        - 마지막 페이지에는 행복한 결말

	        이제 시작해!
	        """;

	    String template = """
				너는 30년 경력의 베스트셀러 동화 작가야.
				지금부터 4~7살 아이들이 잠들기 전에 읽어줄 수 있는 따뜻하고 포근한 동화를 써줘.
				문장은 간결하고 리듬감 있게, 감정을 세밀하게 표현해줘.
				폭력적이거나 무서운 요소는 절대 넣지 마.
				해피엔딩이거나 따뜻한 열린 결말로 마무리해줘.
				
	  	        8페이지 분량의 귀여운 한국어 동화 스토리를 3~8세 아이들을 위해 작성해줘.
	  	        아래 조건으로 8페이지 분량의 짧고 재미있는 동화를 써줘.
	  	        각 페이지는 1~3문장 정도로 간결하게.
	  	        형식은 정확히 아래처럼 지켜줘:

	  	        형식: 페이지1: [짧은 문장 1~2개] [영어로 된 간단한 이미지 설명 프롬프트]
	  	        페이지2: ...
	  	        ... (총 8페이지까지)

	  	        조건:
	  	        - 아이 이름: {name} ({age}살)
	  	        - 좋아하는 것: {likes}
	  	        - 테마: {theme}
	  	        - 전체 분위기: 따뜻하고 긍정적이며, 교훈이 살짝 들어가도록
	  	        - 한국어로 자연스럽게 작성
	  	        - 마지막 페이지에는 행복한 결말

	  	        이제 시작해!
	  	        """;

	    
	    // PromptTemplate으로 동적 값 치환
	    PromptTemplate promptTemplate = new PromptTemplate(template);
	    Prompt prompt = promptTemplate.create(Map.of(
	        "name", request.getName(),
	        "age", request.getAge(),
	        "likes", request.getLikes(),
	        "theme", request.getTheme()
	    ));

	    // Ollama 호출 (ChatClient 사용)
	    //ChatResponse response = chatClient.prompt(prompt).call();

	    // 생성된 스토리 텍스트 반환
	    //String generatedStory = response.getResult().getOutput().getContent();
	    String generatedStory = chatClient.prompt(prompt).call().content();
	    // 필요 시 후처리 (예: 불필요한 서론/결론 제거)
	    // generatedStory = generatedStory.replaceAll("(?s)^.*페이지1:", "페이지1:");
log.info("generatedStory:: {}",generatedStory);
	    return generatedStory.trim();
	}

	private List<BookResponse.PageItem> parseImagePromptsFromStory(String fullStory) {
	    List<BookResponse.PageItem> pages = new ArrayList<>();

	    // 정규표현식으로 "페이지N: [텍스트] [프롬프트]" 패턴 매칭
	    // 예시 패턴: 페이지1: 지민이가 우주선에 탑승했다. [cute child boarding spaceship, cartoon style, vibrant colors]
	    //String regex = "페이지(\\d+):\\s*(.+?)\\s*\\[([^\\]]+)\\]";
	    //String regex = "페이지\\s*(\\d+):\\s*(.*?)\\s*\\[(.*?)\\]";
	    String regex = "(?:###\\s*)?페이지\\s*(\\d+)\\s*(.*?)\\s*\\[(.*?)\\]";
	 // Pattern.DOTALL을 사용해야 마침표(.)가 줄바꿈 문자를 포함하여 매칭합니다.
	 //Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
	 Pattern pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE);
	    Matcher matcher = pattern.matcher(fullStory);

	    int pageNumber = 1;  // 만약 페이지 번호가 누락되면 순차적으로 부여

	   // log.info("matcher::: {}",matcher);
	    
	    while (matcher.find()) {
	        String pageNumStr = matcher.group(1);
	        String rawText = matcher.group(2).trim();
	        String imagePrompt = matcher.group(3).trim();

	        String text = rawText.replace(":", " ").trim();
	        
		    log.info("pageNumStr::: {}",pageNumStr);
		    log.info("text::: {}",text);
		    log.info("imagePrompt::: {}",imagePrompt);
	        
	        try {
	            pageNumber = Integer.parseInt(pageNumStr);
	        } catch (NumberFormatException e) {
	            // 페이지 번호 파싱 실패 시 순차 증가
	        }

	        BookResponse.PageItem page = BookResponse.PageItem.builder()
	                .pageNumber(pageNumber)
	                .text(text)
	                .promptUsed(imagePrompt)  // ComfyUI에 넘길 영어 프롬프트
	                .build();

	        pages.add(page);
	        pageNumber++;
	    }

	    // 만약 정규식 매칭이 안 되는 경우 (포맷이 조금 다를 때) fallback
	    if (pages.isEmpty()) {
	        // 간단 fallback: 줄 단위로 나누어 처리 (더 유연하게)
	        String[] lines = fullStory.split("\n");
	        for (String line : lines) {
	            line = line.trim();
	            if (line.isEmpty()) continue;

	            // "페이지N:"으로 시작하면
	            if (line.startsWith("페이지")) {
	                // 페이지 번호 추출
	                String numPart = line.replaceAll("페이지(\\d+):.*", "$1");
	                int num = 1;
	                try {
	                    num = Integer.parseInt(numPart);
	                } catch (Exception ignored) {}

	                // [ ] 안 프롬프트 추출
	                String prompt = "";
	                String textPart = line;
	                if (line.contains("[")) {
	                    int start = line.indexOf("[");
	                    int end = line.lastIndexOf("]");
	                    if (start > -1 && end > start) {
	                        prompt = line.substring(start + 1, end).trim();
	                        textPart = line.substring(0, start).trim();
	                    }
	                }

	                pages.add(BookResponse.PageItem.builder()
	                        .pageNumber(num)
	                        .text(textPart.replaceAll("페이지\\d+:\\s*", ""))
	                        .promptUsed(prompt)
	                        .build());
	            }
	        }
	    }

	    // 최소 1페이지 이상 확보
	    if (pages.isEmpty() && !fullStory.isBlank()) {
	        pages.add(BookResponse.PageItem.builder()
	                .pageNumber(1)
	                .text(fullStory)
	                .promptUsed("cute children's book illustration of " + fullStory.substring(0, Math.min(100, fullStory.length())) + ", vibrant colors")
	                .build());
	    }

	    //log.info("pages::: {}",pages);
	    return pages;
	}	
}
