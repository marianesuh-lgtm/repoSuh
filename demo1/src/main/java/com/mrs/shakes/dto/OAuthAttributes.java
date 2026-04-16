package com.mrs.shakes.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.mrs.shakes.domain.user.Role;
import com.mrs.shakes.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder // 빌더 패턴 사용
@NoArgsConstructor // JPA 필수: 기본 생성자
@AllArgsConstructor // Builder 필수: 모든 필드 생성자

public class OAuthAttributes {

	private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String provider;
    private String providerId;
    private Role role;

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        }
        return ofKakao("id", attributes);
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        // 네이버는 'response'라는 키 안에 실제 유저 정보가 Map으로 들어있습니다.
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("profile_image"))
                .attributes(attributes) // 전체 attributes를 저장
                .nameAttributeKey(userNameAttributeName)
                .provider("NAVER")
                .providerId((String) response.get("id")) // 네이버의 고유 식별자
                .build();
    }
    
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .picture((String) profile.get("profile_image_url"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .provider("KAKAO")
                .providerId(String.valueOf(attributes.get("id")))
                .build();
    }
    // Naver 생략 (유사한 방식으로 response 맵 파싱)
    
    
    public User toEntity() {
        return User.builder()
                .email(email)
                .nickname(name)
                .profileImage(picture)
                .provider(provider)
                .providerId(providerId)
                .role(role.USER) // 가입 시 기본 권한 설정
                .build();
    }
}