package com.mrs.shakes.controller;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.shakes.config.ShakesProperties;
import com.mrs.shakes.domain.story.StoryMaster;
import com.mrs.shakes.dto.AdminStoryDTO;
import com.mrs.shakes.dto.GenerateBookRequest;
import com.mrs.shakes.dto.PagedStoryResponse;
import com.mrs.shakes.service.AdminStoryService;
import com.mrs.shakes.service.CharacterService;
import com.mrs.shakes.service.OllamaTestService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://myShakes.ddns.net:5173")
@Slf4j
@RequiredArgsConstructor
//@NoArgsConstructor  

public class AdminStoryController {

	private final AdminStoryService  service ;

	@GetMapping("/stories")
	public ResponseEntity<?>  getAdminStories(GenerateBookRequest request) throws Exception {
	    log.info("동화 생성 요청 수신: {}", request);
	    List<AdminStoryDTO> result  ;

	    // 유효성 검사 (옵션)
	    if (request.getSelections() == null) {
//	        return ResponseEntity.badRequest().body(Map.of("message", "선택 데이터가 없습니다"));
	    }
//        String charId = request.getSelections().get기().getCharacter().getCode();
//        String mood = request.getSelections().get기().getMood().getLabel();
//        String weakness = request.getSelections().get전().getProblem().getLabel();

        //CharacterDTO character = characterService.getCharacterMapperInfo(charId);
//        CharacterDTO character = storyService.setCharacterDto(request);
//        StoryMaster master = storyService.generateStory(request, character);
//        try {
	          result = service.getAdminStories(request);

//          } catch (Exception ex ) {
//
//        	  log.info("ex::::{}", ex);
//        	  log.info("getCause::::{}", ex.getCause());
//       // 	if(ex. == null || result.getStoryId() == null ) {
//       //     }
//        	
//            return ResponseEntity.notFound().build();
//        }
        
        return ResponseEntity.ok(result);
   	}	
	
	
}
