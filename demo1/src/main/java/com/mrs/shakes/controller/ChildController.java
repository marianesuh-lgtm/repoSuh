package com.mrs.shakes.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.shakes.dto.ChildRequest;
import com.mrs.shakes.dto.ChildResponse;
import com.mrs.shakes.repository.ChildRepository;
import com.mrs.shakes.service.AdminStoryService;
import com.mrs.shakes.service.ChildService;
import com.mrs.shakes.util.ServiceUtil;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/children")
@Slf4j
@RequiredArgsConstructor
//@NoArgsConstructor  
public class ChildController {
	
	private final ChildService childService;
    	
	@GetMapping("/list")
	public ResponseEntity<List<ChildResponse>> getChildren(Authentication authentication) {

		if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
		
		String email = authentication.getName();
	    List<ChildResponse> children = childService.getMyChildren(email);
	    
	    return ResponseEntity.ok(children);
	}

	@PostMapping("/register")
    public ResponseEntity<String> registerChild( @RequestBody ChildRequest request, Authentication authentication) {

	       log.info("request::{}", request.toString());
		
        // "kakao:12345" 또는 "email@naver.com" 형태의 식별자 추출
        String identifier = authentication.getName(); 

        log.info("registerChild getName::{}", request.getName());
        
        childService.registerChild(request, identifier);
        
        return ResponseEntity.ok("자녀 등록이 완료되었습니다.");
    }	
	
}
