package com.mrs.shakes.service;

import com.mrs.shakes.dto.UserResponseDto;
import com.mrs.shakes.entity.User;
import com.mrs.shakes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 소셜 로그인 성공 시 호출되어 사용자 정보를 저장하거나 업데이트합니다.
     */
    @Transactional
    public User saveOrUpdate(String email, String nickname, String profileImage, String provider, String providerId) {
        User user = userRepository.findByProviderId(providerId)
                .map(entity -> {
                    // 기존 유저라면 닉네임과 프로필 사진 업데이트 (선택 사항)
                    entity.setNickname(nickname);
                    entity.setProfileImage(profileImage);
                    return entity;
                })
                .orElseGet(() -> User.builder() // 신규 유저라면 빌더로 생성
                        .email(email)
                        .nickname(nickname)
                        .profileImage(profileImage)
                        .provider(provider)
                        .providerId(providerId)
                        .role("USER") // 기본 권한 설정
                        .build());

        return userRepository.save(user);
    }

    /**
     * 특정 사용자의 정보를 DTO 형태로 반환합니다. (프론트엔드 API용)
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. id=" + userId));
        
        return UserResponseDto.fromEntity(user);
    }

    /**
     * 이메일로 사용자 찾기
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}