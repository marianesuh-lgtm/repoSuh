package com.mrs.shakes.domain.prompt;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prompt_template") // DB에 있는 테이블 이름과 정확히 맞춰야 함
@Getter @Setter
@NoArgsConstructor
public class PromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String templateName; // 예: "story-generator-v1"

    @Column(columnDefinition = "TEXT")
    private String content; // 실제 프롬프트 내용 ({appearance} 등이 포함된 문자열)

 // 2. 쿼리의 p.isActive와 매칭 (에러의 직접적인 원인!)
    private boolean isActive = true;
    
    private String version;

    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
}
