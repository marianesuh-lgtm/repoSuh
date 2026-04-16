package com.mrs.shakes.dto;

import com.mrs.shakes.domain.user.Role;
import com.mrs.shakes.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long userId;          // 내부 식별자
    private String email;         // 사용자 이메일
    private String nickname;      // 닉네임 (화면 표시용)
    private String profileImage;  // 프로필 이미지 URL
    private Role role;          // 권한 (USER, ADMIN 등)
    private String provider;      // 인증 제공자 (KAKAO, NAVER)
    private LocalDateTime createdAt; // 가입일

    /**
     * Entity를 Dto로 변환하는 정적 팩토리 메서드
     * 서비스 계층에서 유저 정보를 반환할 때 사용합니다.
     */
    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }
}