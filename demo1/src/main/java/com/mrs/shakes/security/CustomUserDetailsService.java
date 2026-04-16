package com.mrs.shakes.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mrs.shakes.entity.User;
import com.mrs.shakes.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String compositeKey) throws UsernameNotFoundException {
        // 1. "platform:providerId" 형식으로 들어온 문자열을 분리합니다.
        // 만약 JWT 토큰 생성 시 이 형식으로 저장했다면 여기서 파싱이 필요합니다.
        String[] parts = compositeKey.split(":");
        
        User user;
        if (parts.length == 2) {
            String provider = parts[0];
            String providerId = parts[1];
            user = userRepository.findByProviderAndProviderId(provider, providerId)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        } else {
            // 기존 일반 로그인(이메일) 방식도 지원해야 한다면 fallback 로직
            user = userRepository.findByEmail(compositeKey)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + compositeKey));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail() != null ? user.getEmail() : user.getProviderId()) // 이메일 없으면 ID 사용
                .password("") 
                .roles(user.getRole().name())
                .build();
    }
}