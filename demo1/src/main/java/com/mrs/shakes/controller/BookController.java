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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.mrs.shakes.config.ShakesProperties;
import com.mrs.shakes.domain.story.StoryMaster;
import com.mrs.shakes.domain.story.StoryPage;
import com.mrs.shakes.dto.AdminStoryDTO;
import com.mrs.shakes.dto.BookRequest;
import com.mrs.shakes.dto.CharacterDTO;
import com.mrs.shakes.dto.GenerateBookRequest;
import com.mrs.shakes.dto.PageDTO;
import com.mrs.shakes.dto.PagedStoryResponse;
import com.mrs.shakes.dto.StoryRequestDTO;
import com.mrs.shakes.dto.PagedStoryResponse.Page;
import com.mrs.shakes.security.JwtTokenProvider;
import com.mrs.shakes.service.CharacterService;
import com.mrs.shakes.service.OllamaTestService;
import com.mrs.shakes.service.StoryGenerationService;
import com.mrs.shakes.service.StoryService;
import com.mrs.shakes.util.JsonUtils;
import com.mrs.shakes.util.JwtUtil;
import com.mrs.shakes.util.StoryMapper;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(originPatterns = "https://*.myshakes.cc")
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
    private final String imgUrl = "https://api.myShakes.cc/images/characters/";
    @Autowired
    private ResourceLoader resourceLoader;
    
    private final StoryMapper storyMapper;
	private final StoryGenerationService  service ;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;

    @Value("${comfyui.base-url:https://comfyui.myshakes.cc}")  
    private String comfyUrl;
    
//    @Value("${mrs-shakes.comfy.base-url:http://myshakes.ddns.net:8188}")  String comfyUrl;

   
//	public BookController(ChatClient.Builder builder ) {
//        this.chatClient = builder.build();
//    }

    
	@PostMapping("/gen-book")
	public ResponseEntity<?>  generateBookStory(@RequestHeader("Authorization") String authHeader, @RequestBody GenerateBookRequest request) {
	    log.info("동화 생성 요청 수신: {}", request);
	    PagedStoryResponse result  ;

	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	    	log.error("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	     }
	    

	        String token = authHeader.substring(7);
	        log.info("token : {}", token);
	        Object claimValue = (String)jwtUtil.getClaim(token, "userId");
	        String userId = String.valueOf(claimValue);

	        log.info("인증된 유저 : {}", userId);
	        request.setUserId(userId);

	    
	    // 유효성 검사 (옵션)
	    if (request.getSelections() == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }

	 // 1. "Bearer " 접두어 제거 및 토큰 파싱

        StoryRequestDTO storyDto = new StoryRequestDTO();

	    try {
	          result = service.getGeneratePages(request);

          } catch (NullPointerException e) {
                StoryMaster master = storyService.generateStory(request );
                 result = storyMapper.toResponse(master);
                 
                 for(PagedStoryResponse.Page page : result.getPages()) {
                	 page.setText(page.getRawText());
     	    		 page.setImageUrl(comfyUrl+page.getImageUrl());
     	    		 log.info("page:::{}",page);
                 }
                 
                 storyDto.setUserId(request.getUserId());//사용자 id
                 storyDto.setTitle(master.getTitle());//제목
                 storyDto.setStoryId(master.getId()+"");
                 storyService.createStory(storyDto);

                 
                 return ResponseEntity.ok(result);

          } catch (Exception ex ) {

        	  log.info("ex::::{}", ex);
        	  log.info("getCause::::{}", ex.getCause());

            return ResponseEntity.notFound().build();
        }
        
        storyDto.setUserId(request.getUserId());//사용자 id
        storyDto.setTitle(result.getTitle());//제목
        storyDto.setStoryId(result.getStoryId());
        storyService.createStory(storyDto);

        return ResponseEntity.ok(result);
   	}

	@GetMapping("/myStories")
	public ResponseEntity<?>  getMyStories(@RequestHeader("Authorization") String authHeader, BookRequest request) throws Exception {
	    log.info("동화 생성 요청 수신: {}", request);

	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	    	log.error("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	     }
	    

	        String token = authHeader.substring(7);
	        log.info("token : {}", token);
	        Object claimValue = (String)jwtUtil.getClaim(token, "userId");
	        String userId = String.valueOf(claimValue);

	        log.info("인증된 유저 : {}", userId);
	        request.setUserId(userId);
	    
	    List<AdminStoryDTO> result  ;

	    // 유효성 검사 (옵션)
	    if (request  == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }

	    result = storyService.getMyStories(request);
	    
//	    for(AdminStoryDTO dto: result) {
//	    	for(PageDTO page: dto.getPages()) {
//	    		page.setImageUrl(comfyUrl+page.getImageUrl());
//	    	}
//	    }
       
        return ResponseEntity.ok(result);
   	}	
	

	@GetMapping("/myStory/{storyId}")
	public ResponseEntity<?>  getMyStory(@RequestHeader("Authorization") String authHeader, BookRequest request, @PathVariable("storyId") String storyId) throws Exception {
	    log.info("동화 생성 요청 수신: {}", request);

	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	    	log.error("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	     }
	    

	        String token = authHeader.substring(7);
	        log.info("token : {}", token);
	        Object claimValue = (String)jwtUtil.getClaim(token, "userId");
	        String userId = String.valueOf(claimValue);

	        log.info("인증된 유저 : {}", userId);
	        request.setUserId(userId);
	        request.setStoryId(storyId);
	    
	    List<AdminStoryDTO> result  ;

	    // 유효성 검사 (옵션)
	    if (request  == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }

	    result = storyService.getMyStories(request);
	    for(AdminStoryDTO dto: result) {
	    	for(PageDTO page: dto.getPages()) {
	    		//log.info("comfyUrl:::{}", comfyUrl);
	    		//page.setImageUrl(comfyUrl+page.getImageUrl());
	    		//log.info("page:::{}", page);
	    		log.info("page.getImageUrl:::{}", page.getImageUrl());
	    	}
	    }
        
        return ResponseEntity.ok(result);
   	}	
	
	
	
	@PostMapping("/generate-book")
	public ResponseEntity<?>  generateStory(@RequestBody GenerateBookRequest request) {
	    log.info("동화 생성 요청 수신: {}", request);
	    PagedStoryResponse result  ;

	    // 유효성 검사 (옵션)
	    if (request.getSelections() == null) {
	        //return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }
        StoryMaster master = storyService.generateStory(request );
        
        PagedStoryResponse rslt = storyMapper.toResponse(master);
        
        
	    for(Page dto: rslt.getPages()) {
	    		dto.setImageUrl(comfyUrl+dto.getImageUrl());
	    }
       
	    // 여기서 LLM 호출 로직 실행
        return ResponseEntity.ok(rslt);
   	}
	

	
	public byte[] loadWorkflow() throws IOException {
	    // classpath:를 사용하여 resources 폴더 내의 파일을 찾습니다.
	    Resource resource = resourceLoader.getResource(properties.getComfy().getWorkflowPath());
	    return Files.readAllBytes(Paths.get(resource.getURI()));
	}
	

}
