package com.mrs.shakes.repository;

import com.mrs.shakes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 소셜 로그인 시 중복 가입 확인을 위해 providerId로 사용자를 찾습니다.
    Optional<User> findByProviderId(String providerId);
    
    // 이메일로 사용자 존재 여부 확인
    Optional<User> findByEmail(String email);

    // 닉네임 중복 체크 등 필요한 쿼리 메서드를 자유롭게 추가하세요.
    boolean existsByNickname(String nickname);
}