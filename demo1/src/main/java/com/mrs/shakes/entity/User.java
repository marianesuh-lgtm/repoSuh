package com.mrs.shakes.entity;


import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.mrs.shakes.domain.user.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Data
@Getter
@Setter
@Builder // 빌더 패턴 사용
@NoArgsConstructor // JPA 필수: 기본 생성자
@AllArgsConstructor // Builder 필수: 모든 필드 생성자
@EntityListeners(AuditingEntityListener.class) // 추가
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = true)
    private String email;

    private String nickname;

    @Column(name = "provider_id", unique = true, nullable = true)
    private String providerId;

    @Column(nullable = false)
    private String provider; // KAKAO, NAVER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // KAKAO, NAVER
 
    @Column(nullable = true)
    private String profileImage; // KAKAO, NAVER

    @Column(nullable = true)
    private String password; // KAKAO, NAVER
    
    @CreatedDate // 생성 시 자동 날짜 주입
    @Column(name = "created_at", nullable = false, updatable = false)    private LocalDateTime createdAt; // KAKAO, NAVER
    
    @LastModifiedDate // 수정 시 자동 날짜 주입
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder
    public User(String email, String nickname, Role role, String provider,String providerId, String profileImage, String password) {
        this.email = email;
        this.nickname = nickname; // 추가
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImage = profileImage; // 추가
        this.password = password; // 추가
    }    
    
    
}
