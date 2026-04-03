package com.mrs.shakes.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    private String nickname;

    @Column(name = "provider_id", unique = true, nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String provider; // KAKAO, NAVER

    @Column(nullable = false)
    private String role; // KAKAO, NAVER
 
    @Column(nullable = false)
    private String profileImage; // KAKAO, NAVER

    @Column(nullable = false)
    private LocalDateTime createdAt; // KAKAO, NAVER
    
    // Getter, Setter 및 생성자 생략
}
