package com.mrs.shakes.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.shakes.dto.UserResponseDto;
import com.mrs.shakes.entity.User;
import com.mrs.shakes.service.UserService;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
//@NoArgsConstructor  

public class UserController {

	private final UserService userService;
	
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        // 실제 운영 환경에서는 CustomOAuth2User 등으로 캐스팅하여 
        // DB에서 조회한 유저 정보를 DTO로 변환해 반환합니다.
//        User user = userService.findByEmail(oAuth2User.getAttribute("email"));
    	if (oAuth2User == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
 
    	// 플랫폼별로 이메일을 추출하는 유틸리티 로직이 필요합니다.
        String email = extractEmail(oAuth2User);
        
        User user = userService.findByEmail(oAuth2User.getAttribute("email"))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));        
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    private String extractEmail(OAuth2User oAuth2User) {
        // 네이버인 경우
        Map<String, Object> response = oAuth2User.getAttribute("response");
        if (response != null) return (String) response.get("email");

        // 카카오인 경우
        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        if (kakaoAccount != null) return (String) kakaoAccount.get("email");

        return oAuth2User.getAttribute("email"); // 기본값
    }

}
