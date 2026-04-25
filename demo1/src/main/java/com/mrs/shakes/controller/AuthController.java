package com.mrs.shakes.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.shakes.dto.LoginRequest;
import com.mrs.shakes.dto.LoginResponse;
import com.mrs.shakes.dto.SignupRequest;
import com.mrs.shakes.entity.User;
import com.mrs.shakes.repository.UserRepository;
import com.mrs.shakes.security.JwtTokenProvider;
import com.mrs.shakes.service.AdminStoryService;
import com.mrs.shakes.service.AuthService;
import com.mrs.shakes.service.KakaoService;
import com.mrs.shakes.service.NaverService;
import com.mrs.shakes.util.ServiceUtil;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
//@NoArgsConstructor  

public class AuthController {

	private final KakaoService kakaoService; // 플랫폼별 서비스 분리 추천
    private final NaverService naverService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @GetMapping("/login/{platform}")
    public void socialLogin(@PathVariable String platform, HttpServletResponse response) throws IOException {
        String redirectUrl = "";

        if ("kakao".equalsIgnoreCase(platform)) {
            redirectUrl = kakaoService.getAuthorizationUrl();
        } else if ("naver".equalsIgnoreCase(platform)) {
            redirectUrl = naverService.getAuthorizationUrl();
        }

        log.info("{} 로그인 페이지로 리다이렉트: {}", platform, redirectUrl);
        response.sendRedirect(redirectUrl);
    }



    @GetMapping("/callback/{platform}")
    public void socialCallback(
            @PathVariable String platform,
            @RequestParam String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response) throws IOException, Exception {
        
        log.info("{} 콜백 수신. Code: {}, State: {}", platform, code, state);

        String accessToken = "";
        Map<String, Object> userInfo = null;

        if ("naver".equalsIgnoreCase(platform)) {
            // 1. 코드를 이용해 네이버 Access Token 받기
            accessToken = naverService.getAccessToken(code, state);
            
            // 2. 받은 토큰으로 사용자 정보 조회
            userInfo = naverService.getUserProfile(accessToken);
        } else if ("kakao".equalsIgnoreCase(platform)) {
            // KakaoService에 구현하신 메서드 호출
            accessToken = kakaoService.getAccessToken(code); // 카카오는 state가 선택사항
            userInfo = kakaoService.getUserInfoByToken(accessToken);
        } 
        log.info("userInfo :::: {}  ", userInfo);

        if (userInfo == null) {
            log.error("{} 사용자 정보를 가져오는데 실패했습니다.", platform);
            response.sendRedirect("https://www.	myShakes.cc/login-fail");
            return;
        }
        
     // 3. 로그용 데이터 추출 (플랫폼별 구조 차이 대응)
        String email = "";
        String nickname = "";
        String profileImageUrl = "";
        String profile_image = "";
        String provider_id = "";
        String gender = "";

        if ("naver".equals(platform)) {
            // 1. 우선 response 키가 있는지 확인
            Map<String, Object> naverAccount = (Map<String, Object>) userInfo.get("response");
            
            // 2. 만약 response 키가 없다면 userInfo 자체가 데이터 맵인 상황임
            Map<String, Object> targetMap = (naverAccount != null) ? naverAccount : userInfo;

            email = (String) targetMap.get("email");
            nickname = (String) targetMap.get("nickname");
            profile_image = (String) targetMap.get("profile_image");
            provider_id = String.valueOf(targetMap.get("id")); // 로그의 id 값을 가져옴
            gender = (String) targetMap.get("gender");
            
        }    else if ("kakao".equalsIgnoreCase(platform)) {
            // 카카오 데이터 추출 (구조가 다름!)
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
            
            if (kakaoAccount != null) {
            	
            	Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                
                if (profile != null) {
                    // 1. 닉네임 꺼내기
                    nickname = (String) profile.get("nickname");
                    
                    // 2. 프로필 이미지 URL 꺼내기
                    // 주의: 키값이 'profile_image_url' 입니다. 
                    // properties 안에는 'profile_image'로 되어있지만 profile 안에는 풀네임으로 들어있네요.
                    profileImageUrl = (String) profile.get("profile_image_url");
                }
                
                // 이메일 처리 (현재 데이터에는 없으므로 null이 반환됨)
                email = (String) kakaoAccount.get("email");                
            }
        }

        // 추출된 데이터 확인 (null일 경우 기본값 설정)
        email = (email != null) ? email : "no-email";
        nickname = (nickname != null) ? nickname : "Unknown";

        log.info("{} 로그인 성공 - Email: {}, Nickname: {}, provider_id: {}", platform, email, nickname, provider_id);
        log.info("  userInfo: {}", userInfo);
        //log.info("{} 로그인 성공 - 이메일: {}, 닉네임: {}", platform, email, nickname);        
        
     // 4. 공통 비즈니스 로직 처리 (DB 저장 및 JWT 발행)
        String myJwtToken = authService.processSocialLogin(userInfo, platform);      
        
        // 4. 우리 서버 전용 JWT 토큰 발행 (임시 생성)
        //String myJwtToken = "my-secret-jwt-token-for-" + email;

        // 5. 프론트엔드로 토큰과 함께 리다이렉트
        // (주의: 운영 환경에서는 보안을 위해 쿠키를 쓰거나 더 안전한 방식을 사용하세요)
        String frontendUrl = "https://www.myShakes.cc/login-success?token=" + myJwtToken;
        response.sendRedirect(frontendUrl);
    }  

    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            authService.registerMember(request);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }    
    
 // 로그인 API 추가
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 서비스에서 로그인 검증 후 토큰 생성
            String myJwtToken = authService.login(request);
            
            // 토큰을 JSON 형태로 반환 (예: { "token": "ey..." })
            return ResponseEntity.ok(new LoginResponse(myJwtToken));
        } catch (RuntimeException e) {
            // 아이디가 없거나 비번이 틀린 경우 401(Unauthorized) 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }



}
