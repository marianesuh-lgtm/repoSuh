package com.mrs.shakes.service;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mrs.shakes.domain.user.Role;
import com.mrs.shakes.dto.SignupRequest;
import com.mrs.shakes.entity.User;
import com.mrs.shakes.repository.UserRepository;
import com.mrs.shakes.security.JwtTokenProvider;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor

public class AuthService {
	
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder; // SecurityConfig에 Bean 등록 필요
    private   String provider_id  ;
    private   String email ; // "" 대신 null로 초기화하는 것이 DB 처리에 유리합니다.
    private   String nickname  ;
    private   String profile_image  ;
    private   String gender  ;
    
    @Transactional
    public String processSocialLogin(Map<String, Object> userInfo, String platform) throws Exception {
    	

    	if ("naver".equals(platform)) {
            Map<String, Object> naverAccount = (Map<String, Object>) userInfo.get("response");
            
            // 2. 만약 response 키가 없다면 userInfo 자체가 데이터 맵인 상황임
            Map<String, Object> targetMap = (naverAccount != null) ? naverAccount : userInfo;

            email = (String) targetMap.get("email");
            nickname = (String) targetMap.get("nickname");
            profile_image = (String) targetMap.get("profile_image");
            provider_id = String.valueOf(targetMap.get("id")); // 로그의 id 값을 가져옴
            gender = (String) targetMap.get("gender");
    	} else if ("kakao".equals(platform)) {
    	    // ⭐️ 중요: 카카오 고유 ID는 최상위 userInfo에 있습니다.
    	    provider_id = String.valueOf(userInfo.get("id"));

    	    Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
    	    if (kakaoAccount != null) {
    	        email = (String) kakaoAccount.get("email");
    	        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
    	        if (profile != null) {
    	            nickname = (String) profile.get("nickname");
    	            profile_image = (String) profile.get("profile_image_url");
    	        }
    	    }
    	}
        log.info("{}  61 - Email: {}, Nickname: {}, provider_id: {}", platform, email, nickname, provider_id);

    	// 2. DB에서 사용자 조회 (findByProviderAndProviderId 사용 권장)
    	User user = userRepository.findByProviderAndProviderId(platform, provider_id)
    	        .orElseGet(() -> {
    	            Role initialRole = Role.USER;
    	            
      	            return userRepository.save(User.builder()
    	                    .email(email) 
    	                    .nickname(nickname)
    	                    .role(initialRole)
    	                    .provider(platform)
    	                    .providerId(provider_id)
    	                    .profileImage(profile_image)
    	                    .build());
    	        });
    	// 3. 관리자 여부 로그 확인
        log.info("사용자 {} 로그인 (권한: {})", user.getNickname(), user.getRole());

        // 4. JWT 생성 시 권한 정보를 포함시킵니다.
        return jwtTokenProvider.createToken(user);
    }

    @Transactional
    public void registerMember(SignupRequest request) {
    	//Role initialRole = Role.ADMIN;
        // 1. 중복 이메일 체크
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 2. 비밀번호 암호화 및 유저 생성
        User member = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 암호화!
                .nickname(request.getName())
                .role(Role.USER) // 기본 권한 부여
                .provider("LOCAL") // 일반 가입 구분
                .build();

        memberRepository.save(member);
    }    
    
}
