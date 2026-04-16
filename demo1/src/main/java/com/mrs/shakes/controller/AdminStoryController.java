package com.mrs.shakes.controller;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.mrs.shakes.dto.PagedStoryResponse;
import com.mrs.shakes.service.AdminStoryService;
import com.mrs.shakes.service.CharacterService;
import com.mrs.shakes.service.OllamaTestService;
import com.mrs.shakes.util.ServiceUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@Slf4j
@RequiredArgsConstructor
//@NoArgsConstructor  

public class AdminStoryController {

	private final AdminStoryService  service ;
	private final RestTemplate restTemplate = new RestTemplate();
	private final ServiceUtil svcUtil;

	@GetMapping("/stories")
	public ResponseEntity<?>  getAdminStories(BookRequest request) throws Exception {
	    log.info("동화 생성 요청 수신: {}", request);
	    List<AdminStoryDTO> result  ;

	    // 유효성 검사 (옵션)
	    if (request  == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }

	    result = service.getAdminStories(request);
        
        return ResponseEntity.ok(result);
   	}	

	@GetMapping("/stories/{storyId}")
	public ResponseEntity<?>  getAdminStory(BookRequest request, @PathVariable("storyId") String storyId) throws Exception {
	    log.info("동화 생성 요청 수신: {}", request);
	    List<AdminStoryDTO> result  ;

	    // 유효성 검사 (옵션)
	    if (request  == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }
	    
	    request.setStoryId(storyId);
        result = service.getAdminStories(request);
        
        return ResponseEntity.ok(result);
   	}	

	@PutMapping("/stories/validate/{storyId}")
	public ResponseEntity<?>  validateAdminStory(@RequestBody BookRequest request, @PathVariable("storyId") String storyId) throws Exception {
	    log.info("동화 생성 요청 수신: {}", request);
	    List<AdminStoryDTO> result  ;

	    // 유효성 검사 (옵션)
	    if (request  == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }
	    
	    request.setStoryId(storyId);
        result = service.getAdminStories(request);
        
        return ResponseEntity.ok(result);
   	}	

	/*
	 * comfyui 에서 동화책 텍스트 변경
	 * 
	 * */
	
	@PutMapping("/stories/content/{storyId}")
	public ResponseEntity<?>  updateAdminStoryContent(@RequestBody BookRequest request, @PathVariable("storyId") String storyId) throws Exception {
	    log.info("동화 생성 요청 수신: {}", request);
	    List<AdminStoryDTO> result  ;

	    // 유효성 검사 (옵션)
	    if (request  == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }
	    
	    request.setStoryId(storyId);
	    request.setContent(request.getContent());
        int iRtn = service.updateAdminStory(request);
        
        return ResponseEntity.ok(iRtn);
   	}	

	/*
	 * comfyui 에서 imagePrompt 로 image 재생성후 해당 url 변경
	 * 
	 * */
	@PutMapping("/stories/imageUrl/{storyId}")
	public ResponseEntity<?>  updateAdminStoryImageUrl(@RequestBody BookRequest request, @PathVariable("storyId") String storyId) throws Exception {
	    log.info("동화 생성 요청 수신: {}", request);
	    List<AdminStoryDTO> result  ;

	    // 유효성 검사 (옵션)
	    if (request  == null) {
	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }
	    
	    log.info("storyId::{}",storyId);
	    log.info("PageId::{}",request.getPageId());
	    log.info("ImagePrompt::{}",request.getImagePrompt());
	    
//String generateAndPollImage(RestTemplate restTemplate, StoryPage item, String mood, CharacterDTO characterDto) 	    
	    String imageUrl = svcUtil.regenerateAndPollImage(restTemplate, request.getImagePrompt(), request.getCharImg() ); 
	    log.info("imageUrl::{}",imageUrl);
	    
	    request.setStoryId(storyId);
	    request.setImageUrl(imageUrl);
        int iRtn = service.updateAdminStory(request);
        
        return ResponseEntity.ok(iRtn);
   	}	
	
	
}
