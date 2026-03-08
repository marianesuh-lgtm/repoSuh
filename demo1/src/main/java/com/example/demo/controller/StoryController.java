package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.NextSceneRequest;
import com.example.demo.dto.NextSceneResponse;
import com.example.demo.service.StoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/story")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@Slf4j
@RequiredArgsConstructor
public class StoryController {

	
	private final StoryService storyService  ;

    @PostMapping("/next")
    public ResponseEntity<NextSceneResponse> getNextScene(@RequestBody NextSceneRequest request) {
        try {
        	log.info("why request:: {}", request);
            NextSceneResponse response = storyService.generateNextScene(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
        	
            NextSceneResponse rtnRes = new NextSceneResponse ();
            rtnRes.setSuccess(false); 
            rtnRes.setMessage(e.getMessage());
            rtnRes.setNextPart(null);
            rtnRes.setImagePrompt(null);
            rtnRes.setImageUrl(null);
            rtnRes.setNextChoices(null);
            rtnRes.setCurrentStep(0);

            return ResponseEntity.badRequest()
                    .body(rtnRes);
        }
    }
    
}
